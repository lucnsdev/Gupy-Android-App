package lucns.gupy.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import lucns.gupy.R;
import lucns.gupy.activities.VacanciesNewsActivity;

public class NotificationProvider {

    public interface Callback {
        void onButtonClick();
    }

    private final int NOTIFICATION_CODE = 1234;
    private final String silent = "Silent";
    private final String alert = "Alert";
    private final String BUTTON_CLICK = "button_click";
    private final String NOTIFICATION_CLICK = "notification_click";
    private final Context context;
    private final Callback callback;
    private final NotificationManager notificationManager;
    private Notification.Builder builderProgress;
    private boolean isShowing;

    public NotificationProvider(Context context, Callback callback) {
        this.context = context;
        this.callback = callback;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createChannels();
    }

    private void createChannels() {
        NotificationChannel builderChannel = new NotificationChannel(silent, context.getString(R.string.notification_silent), NotificationManager.IMPORTANCE_DEFAULT);
        builderChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        builderChannel.enableLights(false);
        builderChannel.enableVibration(false);
        builderChannel.setSound(null, null);
        notificationManager.createNotificationChannel(builderChannel);

        AudioAttributes.Builder audioAttributes = new AudioAttributes.Builder();
        audioAttributes.setAllowedCapturePolicy(AudioAttributes.ALLOW_CAPTURE_BY_ALL);
        audioAttributes.setLegacyStreamType(AudioManager.STREAM_NOTIFICATION);
        audioAttributes.setUsage(AudioAttributes.USAGE_NOTIFICATION);

        builderChannel = new NotificationChannel(alert, context.getString(R.string.notification_alert), NotificationManager.IMPORTANCE_HIGH);
        builderChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        builderChannel.enableLights(true);
        builderChannel.enableVibration(true);
        builderChannel.setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.getPackageName() + "/" + R.raw.mixkit), audioAttributes.build());
        builderChannel.setLightColor(Color.argb(255, 255, 255, 255));
        builderChannel.setVibrationPattern(new long[]{250, 250, 250, 250});
        notificationManager.createNotificationChannel(builderChannel);
    }

    public void showProgress(String title, String text, String sub, int maximum) { // action button only works if app is running
        isShowing = true;
        builderProgress = new Notification.Builder(context, silent);
        builderProgress.setAutoCancel(false);
        builderProgress.setOngoing(true);
        builderProgress.setShowWhen(true);
        builderProgress.setColorized(true);
        builderProgress.setColor(context.getColor(R.color.accent));
        builderProgress.setTicker(title);
        builderProgress.setContentTitle(title);
        if (text != null) builderProgress.setContentText(text);
        if (sub != null) builderProgress.setSubText(sub);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
            pendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_CODE, new Intent(BUTTON_CLICK), PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_CODE, new Intent(BUTTON_CLICK), PendingIntent.FLAG_UPDATE_CURRENT);
        }
        builderProgress.addAction(new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.icon_close_18), context.getString(android.R.string.cancel), pendingIntent).build());
        builderProgress.setProgress(maximum, 0, false);

        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BUTTON_CLICK);
            context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
        } catch (Exception ignore) {
        }
        builderProgress.setSmallIcon(Icon.createWithResource(context, R.drawable.g));
        builderProgress.setCategory(Notification.CATEGORY_SERVICE);
        notificationManager.notify(NOTIFICATION_CODE, builderProgress.build());
    }

    public void updateProgress(int progress, int maximum) {
        builderProgress.setProgress(maximum, progress, false);
        notificationManager.notify(NOTIFICATION_CODE, builderProgress.build());
    }

    public void showAlert(String title, String text, String sub) {
        isShowing = true;
        Intent resultIntent = new Intent(context, VacanciesNewsActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        stackBuilder.addNextIntent(new Intent(NOTIFICATION_CLICK));

        Notification.Builder builder = new Notification.Builder(context, alert);
        builder.setAutoCancel(true);
        builder.setOngoing(false);
        builder.setShowWhen(true);
        builder.setColorized(true);
        builder.setColor(context.getColor(R.color.accent));
        builder.setTicker(title);
        builder.setContentTitle(title);
        if (text != null) builder.setContentText(text);
        if (sub != null) builder.setSubText(sub);
        builder.setSmallIcon(Icon.createWithResource(context, R.drawable.g));
        builder.setCategory(Notification.CATEGORY_SERVICE);
        builder.setContentIntent(stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
        notificationManager.notify(NOTIFICATION_CODE, builder.build());
    }

    public void show(String title, String text, String sub, String action) { // action button only works if app is running
        isShowing = true;
        Notification.Builder builder = new Notification.Builder(context, silent);
        builder.setAutoCancel(false);
        builder.setOngoing(false);
        builder.setShowWhen(true);
        builder.setColorized(true);
        builder.setColor(context.getColor(R.color.accent));
        builder.setTicker(title);
        builder.setContentTitle(title);
        if (text != null) builder.setContentText(text);
        if (sub != null) builder.setSubText(sub);
        if (action != null) {
            PendingIntent pendingIntent;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
                pendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_CODE, new Intent(BUTTON_CLICK), PendingIntent.FLAG_IMMUTABLE);
            } else {
                pendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_CODE, new Intent(BUTTON_CLICK), PendingIntent.FLAG_UPDATE_CURRENT);
            }
            builder.addAction(new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.icon_close_18), action, pendingIntent).build());

            try {
                IntentFilter filter = new IntentFilter();
                filter.addAction(BUTTON_CLICK);
                context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
            } catch (Exception ignore) {
            }
        }
        builder.setSmallIcon(Icon.createWithResource(context, R.drawable.g));
        builder.setCategory(Notification.CATEGORY_SERVICE);
        notificationManager.notify(NOTIFICATION_CODE, builder.build());
    }

    public void hide() {
        isShowing = false;
        notificationManager.cancelAll();
        try {
            context.unregisterReceiver(receiver);
        } catch (Exception ignore) {
        }
    }

    public boolean isShowing() {
        return isShowing;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent == null || intent.getAction() == null ? "" : intent.getAction();
            hide();
            if (action.equals(BUTTON_CLICK)) callback.onButtonClick();
        }
    };
}
