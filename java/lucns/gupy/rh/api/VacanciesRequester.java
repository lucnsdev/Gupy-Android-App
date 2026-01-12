package lucns.gupy.rh.api;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

import lucns.gupy.rh.GupyUtils;
import lucns.gupy.rh.models.Vacancy;

public class VacanciesRequester {

    private final Queue<Requester> queue;
    private final ResponseCallback callback;

    public VacanciesRequester(ResponseCallback callback) {
        this.callback = callback;
        queue = new LinkedList<>();
    }

    public void cancel() {
        for (Requester requester : queue) requester.cancel();
        queue.clear();
    }

    public void request(Vacancy[] vacancies) {
        if (vacancies.length == 0) return;
        ResponseCallback rc = new ResponseCallback() {
            @Override
            public void onError(String message, int code) {
                for (Requester requester : queue) requester.cancel();
                queue.clear();
                callback.onError(message, code);
            }

            @Override
            public void onVacanciesAvailable(Vacancy[] vacancies) {
                if (queue.isEmpty()) {
                    callback.onVacanciesAvailable(vacancies);
                    callback.onFinish();
                    return;
                }
                callback.onVacanciesAvailable(vacancies);
                Requester requester = queue.remove();
                requester.request();
            }
        };
        for (Vacancy vacancy : vacancies) {
            queue.add(new Requester(vacancy, rc));
        }
        Requester requester = queue.remove();
        requester.request();
    }

    private class Requester extends BaseProvider {

        private final Vacancy vacancy;

        public Requester(Vacancy vacancy, ResponseCallback responseCallback) {
            super(responseCallback);
            this.vacancy = vacancy;
        }

        public void request() {
            if (isRunning) return;
            isRunning = true;
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    String url;
                    String filters = "&jobName=" + URLEncoder.encode(vacancy.name, StandardCharsets.UTF_8);
                    if (vacancy.workplaceType != null) {
                        filters += "&workplaceType=" + URLEncoder.encode(vacancy.workplaceType, StandardCharsets.UTF_8);
                    }
                    if (vacancy.locality.state != null) {
                        filters += "&state=" + URLEncoder.encode(vacancy.locality.state, StandardCharsets.UTF_8);
                    }
                    if (vacancy.locality.city != null) {
                        filters += "&city=" + URLEncoder.encode(vacancy.locality.city, StandardCharsets.UTF_8);
                    }
                    url = "https://employability-portal.gupy.io/api/v1/jobs?limit=100&offset=0&sortBy=publishedDate&sortOrder=desc" + filters;
                    request = requestGet(url);
                    if (responseCode == 200) {
                        Vacancy[] vacancies = GupyUtils.jsonToVacancies(request);
                        if (vacancies == null) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    isRunning = false;
                                    responseCallback.onError("JSONException occurred!", responseCode);
                                }
                            });
                            return;
                        }
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                isRunning = false;
                                responseCallback.onVacanciesAvailable(vacancies);
                            }
                        });
                        return;
                    }
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            isRunning = false;
                            responseCallback.onError(responseMessage, responseCode);
                        }
                    });
                }
            });
            thread.start();
        }
    }
}
