package lucns.gupy.services.foreground;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import lucns.gupy.R;
import lucns.gupy.activities.Stabilizer;
import lucns.gupy.activities.VacanciesNewsActivity;
import lucns.gupy.rh.api.GlobalVacanciesRetriever;
import lucns.gupy.rh.api.ResponseCallback;
import lucns.gupy.rh.models.Enterprise;
import lucns.gupy.services.NotificationProvider;

public class MainService extends BaseService {

    public interface Callback {
        void onUpdate(String enterpriseName, int index, int target);

        void onFinish();

        void onError();
    }

    private NotificationProvider notificationProvider;
    private Callback callback;
    private GlobalVacanciesRetriever retriever;
    private int enterprisesCount;
    private long timeDataReceived;
    private Stabilizer stabilizer;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        stabilizer = new Stabilizer();
        notificationProvider = new NotificationProvider(this, new NotificationProvider.Callback() {
            @Override
            public void onButtonClick() {
                stopForeground();
                notificationProvider.hide();
            }
        });
        notificationProvider.setActivityClass(VacanciesNewsActivity.class);

        retriever = new GlobalVacanciesRetriever(new ResponseCallback() {

            @Override
            public void onError(String message, int code) {
                if (!retriever.isRunning()) return;
                notificationProvider.showAlert(message, String.valueOf(code), null, null);
                if (callback != null) callback.onError();
            }

            @Override
            public void onEnterpriseRetrieved(Enterprise enterprise, int index, int size) {
                if (!retriever.isRunning()) return;
                if (enterprise.vacancies != null && enterprise.vacancies.length > 0) enterprisesCount++;
                long now = System.currentTimeMillis();
                long time = stabilizer.put((now - timeDataReceived) * (long) (size - index));
                String passedTime = getPassedTime(time / 1000);
                String futureTime = getFutureTime(time);
                timeDataReceived = now;
                notificationProvider.updateProgress(String.format(Locale.getDefault(), getString(R.string.format_estimative), passedTime, futureTime), index + "/" + size, index, size);
                if (callback != null) callback.onUpdate(enterprise.name, index, size);
            }

            @Override
            public void onFinish() {
                notificationProvider.showAlert(getString(R.string.complete), String.valueOf(enterprisesCount), null, null);
                if (callback != null) callback.onFinish();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public NotificationProvider onForegroundRequested() {
        if (!notificationProvider.isShowing()) {
            notificationProvider.show(R.string.running, R.string.touch_to_open, R.string.close);
        }
        return notificationProvider;
    }

    @Override
    public void onForegroundStarted() {
    }

    @Override
    public void onForegroundStopped() {
        notificationProvider.hide();
    }

    public boolean isRunning() {
        return retriever.isRunning();
    }

    public void run(boolean fromZero) {
        stabilizer.clear();
        timeDataReceived = System.currentTimeMillis();
        notificationProvider.showProgress(R.string.running, R.string.counter_zero);
        startForeground(MainService.class);
        retriever.requester(fromZero);
    }

    public void cancel() {
        retriever.cancel();
        stopForeground();
        notificationProvider.hide();
    }

    public String getPassedTime(long timestamp) { // in seconds
        long m = 0;
        long h = 0;
        long d = 0;
        long s = timestamp;
        while (s >= 86400) {
            d++;
            s -= 86400;
        }
        while (s >= 3600) {
            h++;
            s -= 3600;
        }
        while (s >= 60) {
            m++;
            s -= 60;
        }
        if (d > 0 && h > 0) return d + "d " + h + "h";
        else if (d > 0) return d + "d";
        else if (h > 0 && m > 0) return h + "h " + m + "m";
        else if (h > 0) return h + "h";
        else if (m > 0 && s > 0) return m + "m " + s + "s";
        else if (m > 0) return m + "m";
        else return s + "s";
    }

    private String getFutureTime(long time) {
        Instant futureInstant = Instant.now().plusMillis(time);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
        return formatter.format(futureInstant);
    }
}
