package lucns.gupy.activities;

import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import lucns.gupy.R;
import lucns.gupy.services.foreground.MainService;
import lucns.gupy.services.foreground.ServiceInstantiator;
import lucns.gupy.services.scheduled.JobSchedulerService;
import lucns.gupy.utils.Notify;

import java.util.List;

public class ExecutionVacanciesSearchActivity extends Activity {

    private TextView textStatus, textUpdate;
    private MainService mainService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_execution_search);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        PowerManager pm = (PowerManager) this.getSystemService(POWER_SERVICE);
        textStatus = findViewById(R.id.textStatus);
        textUpdate = findViewById(R.id.textUpdate);
        CheckBox checkBox = findViewById(R.id.checkBox);
        Button buttonVacancies = findViewById(R.id.buttonVacancies);
        Button buttonEnterprises = findViewById(R.id.buttonEnterprises);
        buttonVacancies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ExecutionVacanciesSearchActivity.this, GloballyVacanciesActivity.class));
            }
        });
        buttonEnterprises.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ExecutionVacanciesSearchActivity.this, EnterprisesActivity.class));
            }
        });
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mainService.isRunning()) {
                    mainService.cancel();
                    textStatus.setText(R.string.stopped);
                    button.setText(R.string.run);
                    startSchedule(false);
                } else {
                    String packageName = getPackageName();
                    if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        intent.setData(Uri.parse("package:" + packageName));
                        startActivity(intent);
                        return;
                    }
                    mainService.run(checkBox.isChecked());
                    textStatus.setText(R.string.running);
                    button.setText(android.R.string.cancel);
                    stopSchedule();
                }
            }
        });
        ServiceInstantiator.getInstance(this, new ServiceInstantiator.OnServiceAvailableListener() {
            @Override
            public void onAvailable(MainService mainService) {
                ExecutionVacanciesSearchActivity.this.mainService = mainService;
                textStatus.setText(mainService.isRunning() ? R.string.running : R.string.stopped);
                mainService.setCallback(new MainService.Callback() {
                    @Override
                    public void onUpdate(String enterpriseName, int index, int target) {
                        textStatus.setText(enterpriseName);
                        textUpdate.setText(index + "/" + target);
                    }

                    @Override
                    public void onFinish() {
                        textStatus.setText(R.string.stopped);
                        button.setText(R.string.run);
                    }

                    @Override
                    public void onError() {
                        button.setText(R.string.run);
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void stopSchedule() {
        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(JobSchedulerService.MY_JOB_ID);
    }

    private void startSchedule(boolean immediately) {
        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        //if (isJobServiceRunning()) return;
        if (isJobServiceRunning()) jobScheduler.cancelAll();
        JobInfo.Builder builder = new JobInfo.Builder(JobSchedulerService.MY_JOB_ID, new ComponentName(getPackageName(), JobSchedulerService.class.getName()));
        if (immediately) {
            builder.setPeriodic(JobInfo.getMinPeriodMillis()); // start job immediately
        } else {
            builder.setPeriodic(JobInfo.getMinPeriodMillis(), JobInfo.getMinFlexMillis()); // delay on first trigger
        }
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        NetworkRequest.Builder networkRequestBuilder = new NetworkRequest.Builder();
        networkRequestBuilder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        networkRequestBuilder.addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        builder.setRequiredNetwork(networkRequestBuilder.build());
        //builder.setPeriodic(TimeUnit.DAYS.toMillis(1));
        builder.setRequiresDeviceIdle(false);
        builder.setPersisted(true);
        int result = jobScheduler.schedule(builder.build());
        if (result == JobScheduler.RESULT_FAILURE) {
            Notify.showToast(getString(R.string.app_name) + " Schedule failure");
        }
    }

    private boolean isJobServiceRunning() {
        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        List<JobInfo> allPendingJobs = jobScheduler.getAllPendingJobs();
        boolean jobFound = false;
        if (!allPendingJobs.isEmpty()) {
            for (JobInfo job : allPendingJobs) {
                if (job.getId() == JobSchedulerService.MY_JOB_ID) {
                    jobFound = true;
                    break;
                }
            }
        }
        return jobFound;
    }
}
