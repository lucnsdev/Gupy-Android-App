package lucns.gupy.services.foreground;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class ServiceInstantiator {

    public interface OnServiceAvailableListener {
        void onAvailable(MainService mainService);
    }

    private static ServiceInstantiator serviceInstantiator;
    private MainService mainService;

    public static ServiceInstantiator getInstance(Context context, OnServiceAvailableListener onServiceAvailableListener) {
        if (serviceInstantiator == null) {
            synchronized (ServiceInstantiator.class) {
                serviceInstantiator = new ServiceInstantiator(context, onServiceAvailableListener);
            }
        }
        if (serviceInstantiator.getService() != null) {
            onServiceAvailableListener.onAvailable(serviceInstantiator.mainService);
        }
        return serviceInstantiator;
    }

    private ServiceInstantiator(Context context, OnServiceAvailableListener onServiceAvailableListener) {

        Intent service = new Intent(context, MainService.class);
        context.startService(service);
        context.bindService(service, new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                MainService.LocalBinder binder = (MainService.LocalBinder) service;
                mainService = (MainService) binder.getServiceInstance();
                onServiceAvailableListener.onAvailable(mainService);
                context.unbindService(this);
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
            }
        }, Context.BIND_AUTO_CREATE);
    }

    public MainService getService() {
        return mainService;
    }
}
