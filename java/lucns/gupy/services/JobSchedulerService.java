package lucns.gupy.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import lucns.gupy.R;
import lucns.gupy.activities.FragmentVacancy;
import lucns.gupy.activities.VacanciesSearchActivity;
import lucns.gupy.rh.GupyUtils;
import lucns.gupy.rh.api.ResponseCallback;
import lucns.gupy.rh.api.VacanciesRequester;
import lucns.gupy.rh.models.Vacancy;
import lucns.gupy.utils.Annotator;
import lucns.gupy.utils.Notify;
import lucns.gupy.utils.TimeRegister;
import lucns.gupy.utils.Utils;

public class JobSchedulerService extends JobService {

    public static final int MY_JOB_ID = 1234;
    public static final String ACTION_NEWS = "news";
    private JobParameters jobParameters;
    private NotificationProvider notificationProvider;
    private VacanciesRequester vacanciesRequester;
    private int count, target;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationProvider = new NotificationProvider(this, new NotificationProvider.Callback() {

            @Override
            public void onButtonClick() {
                jobFinished(jobParameters, true);
            }
        });

        vacanciesRequester = new VacanciesRequester(new ResponseCallback() {

            private List<Vacancy> listNewVacancies = new ArrayList<>();

            @Override
            public void onError(String message, int code) {
                //notificationProvider.showAlert(String.format(Locale.getDefault(), getString(R.string.error_code), code), message, null);
                Notify.showToast(String.format(Locale.getDefault(), getString(R.string.error_code), code));
                notificationProvider.hide();
                jobFinished(jobParameters, true);
            }

            @Override
            public void onVacanciesAvailable(Vacancy[] vacancies) {
                count++;
                listNewVacancies.addAll(Arrays.asList(vacancies));
                notificationProvider.updateProgress(count, target);
            }

            @Override
            public void onFinish() {
                new TimeRegister("last_updated").setLastUpdate();
                Vacancy[] newsVacancies = listNewVacancies.toArray(new Vacancy[0]);
                Vacancy[] previousNewsVacancies = GupyUtils.getNewVacancies();
                Vacancy[] news = getAllNoRepeat(newsVacancies, previousNewsVacancies);
                Vacancy[] viewedVacancies = GupyUtils.getViewedVacancies();
                if (news.length == 0) {
                    notificationProvider.hide();
                    return;
                }
                if (viewedVacancies == null || viewedVacancies.length == 0) {
                    GupyUtils.setNewsVacancies(news);
                    sendBroadcast(new Intent(ACTION_NEWS));
                    notificationProvider.showAlert(getString(R.string.vacancies_news), String.format(Locale.getDefault(), getString(R.string.vacancies_founded), news.length, getString(news.length == 1 ? R.string.vacancy : R.string.vacancies)), null);
                    return;
                }
                news = getIfNotContains(news, viewedVacancies);
                if (news.length == 0) {
                    notificationProvider.hide();
                    return;
                }
                GupyUtils.setNewsVacancies(news);
                sendBroadcast(new Intent(ACTION_NEWS));
                notificationProvider.showAlert(getString(R.string.vacancies_news), String.format(Locale.getDefault(), getString(R.string.vacancies_founded), news.length, getString(news.length == 1 ? R.string.vacancy : R.string.vacancies)), null);
                jobFinished(jobParameters, true);
            }

            private Vacancy[] getAllNoRepeat(Vacancy[] a, Vacancy[] b) {
                if (b == null) return a;
                List<Vacancy> list = new ArrayList<>(Arrays.asList(b));
                for (Vacancy v : a) {
                    boolean f = false;
                    for (Vacancy v2 : b) {
                        if (v.id == v2.id) {
                            f = true;
                            break;
                        }
                    }
                    if (!f) list.add(v);
                }
                return list.toArray(new Vacancy[0]);
            }

            private Vacancy[] getIfNotContains(Vacancy[] a, Vacancy[] b) {
                if (b == null) return a;
                List<Vacancy> list = new ArrayList<>();
                for (Vacancy v : a) {
                    boolean f = false;
                    for (Vacancy v2 : b) {
                        if (v.id == v2.id) {
                            f = true;
                            break;
                        }
                    }
                    if (!f) list.add(v);
                }
                return list.toArray(new Vacancy[0]);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //notificationProvider.hide();
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        jobParameters = params;
/*
        Annotator annotator = new Annotator("JobData.json");
        try {
            JSONArray jsonArray;
            if (annotator.exists()) jsonArray = new JSONArray(annotator.getContent());
            else jsonArray = new JSONArray();
            jsonArray.put(System.currentTimeMillis());
            annotator.setContent(jsonArray.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
 */

        count = 0;
        Vacancy[] v = GupyUtils.getRegisteredVacancies();
        if (v == null) {
            notificationProvider.showAlert(getString(R.string.error_retrieve), getString(R.string.error_registered_vacancies), null);
            jobFinished(params, false);
            return true;
        }
        target = v.length;
        String s = String.format(Locale.getDefault(), getString(R.string.notification_text), 0, getString(R.string.vacancies));
        notificationProvider.showProgress(getString(R.string.notification_title), s, null, v.length);
        vacanciesRequester.request(v);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        jobParameters = params;
        return true;
    }
}
