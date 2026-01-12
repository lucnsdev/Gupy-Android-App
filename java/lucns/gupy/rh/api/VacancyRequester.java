package lucns.gupy.rh.api;

import android.os.Handler;
import android.os.Looper;

import lucns.gupy.rh.models.Vacancy;

public class VacancyRequester extends BaseProvider {

    public VacancyRequester(ResponseCallback responseCallback) {
        super(responseCallback);
    }

    public void requester(Vacancy vacancy) {
        if (isRunning) return;
        isRunning = true;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                request = requestGet(vacancy.url);
                if (responseCode == 200) {
                    GupyPageRetriever gupy = new GupyPageRetriever(vacancy);
                    Vacancy v = gupy.vacancyFromHtml(request);
                    if (v == null) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                isRunning = false;
                                responseCallback.onError("HTML retriever failure!", responseCode);
                                responseCallback.onFinish();
                            }
                        });
                        return;
                    }
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            isRunning = false;
                            responseCallback.onVacancyRetrieved(v);
                            responseCallback.onFinish();
                        }
                    });
                    return;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        isRunning = false;
                        responseCallback.onError(responseMessage, responseCode);
                        responseCallback.onFinish();
                    }
                });
            }
        });
        thread.start();
    }
}
