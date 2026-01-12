package lucns.gupy.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;

import java.util.List;
import java.util.Locale;

import lucns.gupy.R;
import lucns.gupy.rh.GupyUtils;
import lucns.gupy.rh.models.Vacancy;
import lucns.gupy.services.JobSchedulerService;
import lucns.gupy.utils.Notify;
import lucns.gupy.utils.TimeRegister;
import lucns.gupy.utils.Utils;
import lucns.gupy.views.slider.SliderView;

public class VacanciesViewedActivity extends Activity {

    private SliderView sliderView;
    private TextView textStatus, textLastUpdate, textCounter;
    private ProgressBar progressBar;
    private Vacancy[] vacancies;
    private boolean markedAsViewed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vacancies);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gupy_logo, o);
        ImageView imageLogo = findViewById(R.id.imageLogo);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageLogo.getLayoutParams();
        params.width = bitmap.getWidth();
        params.height = bitmap.getHeight();
        imageLogo.setLayoutParams(params);
        imageLogo.setImageBitmap(bitmap);

        float padding = getResources().getDimension(R.dimen.margin_4);

        ObjectAnimator.ofFloat(imageLogo, View.SCALE_Y, 1f, 0.5f).setDuration(600).start();
        ObjectAnimator animator = ObjectAnimator.ofFloat(imageLogo, View.SCALE_X, 1f, 0.5f);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(600);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                RelativeLayout content = findViewById(R.id.content);
                content.setAlpha(0f);
                content.setVisibility(View.VISIBLE);
                ObjectAnimator.ofFloat(imageLogo, View.TRANSLATION_Y, 0, (-1) * (imageLogo.getY() - padding)).setDuration(500).start();
                ObjectAnimator alpha = ObjectAnimator.ofFloat(content, View.ALPHA, 0, 1).setDuration(500);
                alpha.setInterpolator(new LinearInterpolator());
                alpha.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {

                    }
                });
                alpha.start();
            }
        });
        animator.start();

        View.OnClickListener onClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.fab) {
                    startActivity(new Intent(VacanciesViewedActivity.this, VacanciesSearchActivity.class), ActivityOptions.makeSceneTransitionAnimation(VacanciesViewedActivity.this).toBundle());
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 500);
                }
            }
        };
        findViewById(R.id.fab2).setVisibility(View.INVISIBLE);
        ImageButton fab = (ImageButton) findViewById(R.id.fab);
        fab.setImageResource(R.drawable.icon_search_18);
        fab.setOnClickListener(onClick);

        sliderView = findViewById(R.id.sliderView);
        sliderView.disableScroll(false);
        sliderView.setSpaceBetweenViews((int) getResources().getDimension(R.dimen.margin_3));
        sliderView.setWidthViewsPercentage(80);
        sliderView.setOnSliderChangedListener(new SliderView.OnSliderChangedListener() {
            @Override
            public void onSlidePositionChanged(int index) {
                textCounter.setText(String.format(Locale.getDefault(), getString(R.string.format_counter), index + 1, vacancies == null ? 0 : vacancies.length));
            }
        });

        textStatus = findViewById(R.id.textStatus);
        textStatus.setText(R.string.vacancies_viewed);
        textCounter = findViewById(R.id.textCounter);
        textLastUpdate = findViewById(R.id.textLastUpdate);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setProgress(progressBar.getMax());
    }

    private void readViewedVacancies() {
        progressBar.setIndeterminate(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                vacancies = GupyUtils.getViewedVacancies();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setIndeterminate(false);
                        if (vacancies == null || vacancies.length == 0) {
                            textStatus.setText(R.string.no_news);
                            return;
                        }
                        textCounter.setText(String.format(Locale.getDefault(), getString(R.string.format_counter), sliderView.getCurrentIndex() + 1, vacancies.length));

                        sliderView.setVisibility(View.VISIBLE);
                        for (Vacancy vacancy : vacancies) {
                            sliderView.addFragment(new FragmentVacancy(VacanciesViewedActivity.this, vacancy));
                        }
                    }
                });
            }
        }).start();
    }

    private void updateTimeText() {
        TimeRegister timeRegister = new TimeRegister("last_updated");
        String time;
        if (timeRegister.lastUpdateWasToday()) {
            time = getString(R.string.today) + " " + timeRegister.getUpdateHour();
        } else if (timeRegister.lastUpdateWasYesterday()) {
            time = getString(R.string.yesterday) + " " + timeRegister.getUpdateHour();
        } else if (timeRegister.hasRegister()) {
            time = getString(R.string.updated_in) + " " + timeRegister.getLastUpdate();
        } else {
            time = getString(R.string.never_updated);
        }
        textLastUpdate.setText(time);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sliderView.onResume();
        updateTimeText();
        if (sliderView.getChildCount() == 0) {
            if (!Utils.hasInternetConnection()) {
                textStatus.setText(R.string.error_no_connection);
                progressBar.setProgress(0);
                return;
            }
            readViewedVacancies();
        }
        getOnBackInvokedDispatcher().registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, onBackPressed);
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

    private void startSchedule(boolean immediately) {
        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        if (isJobServiceRunning()) return;
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

    private boolean hasRegisteredVacancies() {
        Vacancy[] v = GupyUtils.getRegisteredVacancies();
        return v != null && v.length > 0;
    }

    @Override
    protected void onPause() {
        super.onPause();
        sliderView.onResume();
        getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(onBackPressed);
        if (hasRegisteredVacancies() && !isJobServiceRunning()) startSchedule(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private OnBackInvokedCallback onBackPressed = new OnBackInvokedCallback() {
        @Override
        public void onBackInvoked() {
            if (isFinishing()) return;
            if (sliderView.onBackPressed()) {
                startActivity(new Intent(VacanciesViewedActivity.this, VacanciesNewsActivity.class), ActivityOptions.makeSceneTransitionAnimation(VacanciesViewedActivity.this).toBundle());
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 500);
            }
        }
    };
}
