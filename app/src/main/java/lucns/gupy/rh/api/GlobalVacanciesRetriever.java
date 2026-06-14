package lucns.gupy.rh.api;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import lucns.gupy.rh.GupyUtils;
import lucns.gupy.rh.models.Enterprise;
import lucns.gupy.utils.Annotator;
import lucns.gupy.utils.Utils;

public class GlobalVacanciesRetriever extends BaseProvider {

    private boolean canceled;

    public GlobalVacanciesRetriever(ResponseCallback responseCallback) {
        super(responseCallback);
    }

    @Override
    public boolean isRunning() {
        return super.isRunning() && !canceled;
    }

    @Override
    public void cancel() {
        super.cancel();
        canceled = true;
    }

    public void requester(boolean fromZero) {
        if (isRunning) return;
        isRunning = true;
        canceled = false;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Enterprise[] enterprises = getAllEnterprisesData();
                if (enterprises == null) {
                    isRunning = false;
                    return;
                }
                String temp = "GlobalEnterprisesDataTemp.json";
                String older = "GlobalEnterprisesDataOlder.json";
                String newer = "GlobalEnterprisesDataNewer.json";
                if (fromZero) new Annotator(temp).delete();
                Annotator annotator = new Annotator("EnterprisePosition.txt");
                int position = 0;
                if (!fromZero && annotator.exists()) position = Integer.parseInt(annotator.getContent());
                for (int i = position; i < enterprises.length; i++) {
                    if (canceled) {
                        isRunning = false;
                        return;
                    }
                    attemptRequest(enterprises[i]);
                    if (!Utils.hasInternetConnection()) {
                        isRunning = false;
                        sendError("No internet connection!");
                        return;
                    }
                    GupyUtils.putEnterpriseIntoGlobalData(enterprises[i]);
                    annotator.setContent(String.valueOf(i));
                    int count = i;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            responseCallback.onEnterpriseRetrieved(enterprises[count], count + 1, enterprises.length);
                        }
                    });
                }
                new Annotator(newer).rename(older);
                new Annotator(temp).rename(newer);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        isRunning = false;
                        responseCallback.onFinish();
                    }
                });
            }
        });
        thread.start();
    }

    private void attemptRequest(Enterprise enterprise) {
        String applicationJson = "application/json";
        String tag = "</script>";
        int attempts = 0;
        while (attempts < 10) {
            attempts++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!Utils.hasInternetConnection()) return;
            request = requestGet(enterprise.url);
            if (responseCode == 200) {
                String json = request.substring(request.indexOf(applicationJson) + applicationJson.length() + 2);
                json = json.substring(0, json.indexOf(tag));
                enterprise.vacancies = GupyUtils.extractVacanciesFromJson(json);
                break;
            } else if (responseCode == 404 || responseCode == 0) {
                return;
            }
        }
    }

    private Enterprise[] getAllEnterprisesData() {
        int offset = 0;
        List<Enterprise> list = new ArrayList<>();
        while (!canceled) {
            request = requestGet("https://employability-portal.gupy.io/api/v1/jobs/companies?limit=1000&offset=" + offset + "&sortBy=company&sortOrder=asc");
            if (responseCode == 404) {
                sendError("Initial enterprises host not found!");
                return null;
            }
            offset += 1000;
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Enterprise[] enterprises = GupyUtils.jsonToEnterprises(request);
            if (enterprises == null) {
                sendError("Failure on enterprise retrieve process!");
                return null;
            } else if (enterprises.length == 0) {
                break;
            }
            list.addAll(Arrays.asList(enterprises));
        }

        list.sort(new Comparator<Enterprise>() {

            @Override
            public int compare(Enterprise v, Enterprise v2) {
                return Integer.compare(v.id, v2.id);
            }
        });
        Enterprise[] enterprises = list.toArray(new Enterprise[0]);
        GupyUtils.setAllEnterprises(enterprises);
        return enterprises;
    }

    private void sendError(String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                isRunning = false;
                responseCallback.onError(message, responseCode);
                responseCallback.onFinish();
            }
        });
    }
}
