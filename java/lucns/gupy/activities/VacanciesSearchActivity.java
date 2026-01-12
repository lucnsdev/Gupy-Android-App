package lucns.gupy.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;

import java.util.Locale;

import lucns.gupy.R;
import lucns.gupy.rh.GupyUtils;
import lucns.gupy.rh.api.LocalityProvider;
import lucns.gupy.rh.api.ResponseCallback;
import lucns.gupy.rh.api.VacanciesRequester;
import lucns.gupy.rh.models.Locality;
import lucns.gupy.rh.models.Vacancy;
import lucns.gupy.utils.Annotator;
import lucns.gupy.utils.Notify;
import lucns.gupy.utils.TimeRegister;
import lucns.gupy.utils.Utils;
import lucns.gupy.views.CustomSpinner;
import lucns.gupy.views.IndeterminateThreeBalls;
import lucns.gupy.views.slider.SliderView;

public class VacanciesSearchActivity extends Activity {

    private SliderView sliderView;
    private TextView textStatus, textLastUpdate, textCounter;
    private ProgressBar progressBar;
    private VacanciesRequester vacanciesRequester;
    private int counter = 0;
    private int vacanciesCounter;
    private Dialog dialog;
    private Locality[] cities;

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
                    startActivity(new Intent(VacanciesSearchActivity.this, VacancyRegisterActivity.class), ActivityOptions.makeSceneTransitionAnimation(VacanciesSearchActivity.this).toBundle());
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 500);
                } else if (v.getId() == R.id.fab2) {
                    showDialogOptions();
                }
            }
        };
        ImageButton fab = (ImageButton) findViewById(R.id.fab);
        fab.setImageResource(R.drawable.icon_add_18);
        ImageButton fab2 = (ImageButton) findViewById(R.id.fab2);
        fab2.setImageResource(R.drawable.icon_search_18);
        fab.setOnClickListener(onClick);
        fab2.setOnClickListener(onClick);

        sliderView = findViewById(R.id.sliderView);
        sliderView.disableScroll(false);
        sliderView.setSpaceBetweenViews((int) getResources().getDimension(R.dimen.margin_3));
        sliderView.setWidthViewsPercentage(80);
        sliderView.setOnSliderChangedListener(new SliderView.OnSliderChangedListener() {
            @Override
            public void onSlidePositionChanged(int index) {
                textCounter.setText(String.format(Locale.getDefault(), getString(R.string.format_counter), index + 1, counter));
            }
        });

        textStatus = findViewById(R.id.textStatus);
        textCounter = findViewById(R.id.textCounter);
        textLastUpdate = findViewById(R.id.textLastUpdate);
        progressBar = findViewById(R.id.progressBar);

        vacanciesRequester = new VacanciesRequester(new ResponseCallback() {

            @Override
            public void onError(String message, int code) {
                Utils.vibrate();
                textStatus.setText("Error: " + code + ", " + message);
            }

            @Override
            public void onVacanciesAvailable(Vacancy[] vacancies) {
                Utils.vibrate();
                counter += vacancies.length;
                progressBar.setProgress(progressBar.getProgress() + 1);
                textCounter.setText(String.format(Locale.getDefault(), getString(R.string.format_counter), sliderView.getCurrentIndex() + 1, counter));
                textStatus.setText(String.format(Locale.getDefault(), getString(R.string.format_requesting), counter, vacanciesCounter));
                sliderView.setVisibility(View.VISIBLE);
                for (Vacancy vacancy : vacancies) {
                    sliderView.addFragment(new FragmentVacancy(VacanciesSearchActivity.this, vacancy));
                }
            }

            @Override
            public void onFinish() {
                new TimeRegister("last_updated").setLastUpdate();
                updateTimeText();
                progressBar.setProgress(progressBar.getMax());
                textStatus.setText(R.string.complete);
            }
        });
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

    private void showDialogVacancySearch() {
        dialog = new Dialog(this, R.style.DialogTheme);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_vacancy_detail);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                showDialogOptions();
            }
        });

        IndeterminateThreeBalls progressBarCities = dialog.findViewById(R.id.progressBarCities);
        CustomSpinner spinnerStates = dialog.findViewById(R.id.spinnerStates);
        CustomSpinner spinnerCities = dialog.findViewById(R.id.spinnerCities);

        LocalityProvider localityProvider = new LocalityProvider(new ResponseCallback() {
            @Override
            public void onError(String message, int code) {
                Utils.vibrate();
                Notify.showToast("Error: " + code + "\n" + message);
            }

            @Override
            public void onLocalityAvailable(Locality[] localities) {
                cities = localities;
                Utils.vibrate();
                String[] titles = new String[localities.length];
                for (int i = 0; i < localities.length; i++) titles[i] = localities[i].city;
                spinnerCities.setTitles(titles, -1);
                spinnerCities.setEnabled(true);
                spinnerCities.setVisibility(View.VISIBLE);
                progressBarCities.setVisibility(View.INVISIBLE);
            }
        });

        Vacancy vacancy = new Vacancy();
        Locality locality = new Locality();
        Annotator annotator = new Annotator("States.json");
        Locality[] states = GupyUtils.jsonToStates(annotator.getContent());
        String[] titles = new String[states.length];
        for (int i = 0; i < states.length; i++) titles[i] = states[i].state;

        spinnerStates.setTitles(titles, -1);
        spinnerStates.setText(getString(R.string.select_a_state));
        spinnerCities.setText(getString(R.string.select_a_city));
        spinnerCities.setEnabled(false);
        spinnerStates.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                locality.state = states[position].state;
                spinnerCities.setText(getString(R.string.select_a_city));
                if (!Utils.hasInternetConnection()) {
                    Notify.showToast(R.string.error_no_connection);
                    return;
                }
                spinnerCities.setEnabled(false);
                spinnerCities.setVisibility(View.INVISIBLE);
                progressBarCities.setVisibility(View.VISIBLE);
                localityProvider.requestCities(states[position].id);
            }
        });
        spinnerCities.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                locality.city = cities[position].city;
            }
        });

        CheckBox checkBoxOnSite = dialog.findViewById(R.id.checkBoxOnSite);
        CheckBox checkBoxHybrid = dialog.findViewById(R.id.checkBoxHybrid);
        CheckBox checkBoxRemote = dialog.findViewById(R.id.checkBoxRemote);

        EditText editText = dialog.findViewById(R.id.editText);
        Button button = dialog.findViewById(R.id.buttonRegister);
        button.setEnabled(false);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String type = null;
                if (checkBoxOnSite.isChecked()) type = Vacancy.TYPE_ON_SITE;
                if (checkBoxHybrid.isChecked()) type += "," + Vacancy.TYPE_HYBRID;
                if (checkBoxRemote.isChecked()) type += "," + Vacancy.TYPE_REMOTE;
                vacancy.name = editText.getText().toString();
                vacancy.workplaceType = type;
                vacancy.locality = locality;

                sliderView.removeAllFragments();
                Notify.showToast(String.format(Locale.getDefault(), getString(R.string.searching_for), vacancy.name));
                textStatus.setText(R.string.searching_for_vacancy);
                vacanciesRequester.request(new Vacancy[]{vacancy});
                dialog.dismiss();
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                button.setEnabled(button.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        dialog.show();
    }

    private void showDialogOptions() {
        Vacancy[] v = GupyUtils.getRegisteredVacancies();
        if (v == null) {
            showDialogVacancySearch();
            return;
        }
        counter = 0;
        vacanciesCounter = v.length;
        progressBar.setProgress(0);
        progressBar.setMax(vacanciesCounter);

        dialog = new Dialog(this, R.style.DialogTheme);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_options);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                onBackPressed.onBackInvoked();
            }
        });

        LayoutInflater inflater = LayoutInflater.from(this);
        ListView listView = dialog.findViewById(R.id.listView);
        listView.setAdapter(new ArrayAdapter<>(this, R.layout.item_list) {

            @Override
            public int getCount() {
                return v.length + 2;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View root;
                if (position < v.length) {
                    if (v[position].workplaceType != null || v[position].locality.state != null) {
                        root = inflater.inflate(R.layout.item_list_two, null, false);
                        ((TextView) root.findViewById(R.id.textTopStart)).setText(v[position].name);
                        if (v[position].workplaceType != null && v[position].locality.state != null) {
                            String type = "";
                            if (v[position].workplaceType.contains(Vacancy.TYPE_ON_SITE) && v[position].workplaceType.contains(Vacancy.TYPE_HYBRID) && v[position].workplaceType.contains(Vacancy.TYPE_REMOTE)) {
                                type += getString(R.string.in_person);
                                type += ", ";
                                type += getString(R.string.hybrid);
                                type += " " + getString(R.string.and) + " ";
                                type += getString(R.string.remote);
                            } else if (v[position].workplaceType.contains(Vacancy.TYPE_ON_SITE) && v[position].workplaceType.contains(Vacancy.TYPE_HYBRID)) {
                                type += getString(R.string.in_person);
                                type += " " + getString(R.string.and) + " ";
                                type += getString(R.string.hybrid);
                            } else if (v[position].workplaceType.contains(Vacancy.TYPE_HYBRID) && v[position].workplaceType.contains(Vacancy.TYPE_REMOTE)) {
                                type += getString(R.string.hybrid);
                                type += " " + getString(R.string.and) + " ";
                                type += getString(R.string.remote);
                            } else if (v[position].workplaceType.contains(Vacancy.TYPE_ON_SITE) && v[position].workplaceType.contains(Vacancy.TYPE_REMOTE)) {
                                type += getString(R.string.in_person);
                                type += " " + getString(R.string.and) + " ";
                                type += getString(R.string.remote);
                            } else if (v[position].workplaceType.contains(Vacancy.TYPE_ON_SITE)) {
                                type = getString(R.string.in_person);
                            } else if (v[position].workplaceType.contains(Vacancy.TYPE_HYBRID)) {
                                type = getString(R.string.hybrid);
                            } else if (v[position].workplaceType.contains(Vacancy.TYPE_REMOTE)) {
                                type = getString(R.string.remote);
                            }
                            ((TextView) root.findViewById(R.id.textCenterEnd)).setText(type);
                            String locality = "";
                            if (v[position].locality.city != null) {
                                locality = v[position].locality.city;
                                locality += ", ";
                                locality += v[position].locality.state;
                            } else {
                                locality = v[position].locality.state;
                            }
                            ((TextView) root.findViewById(R.id.textCenterStart)).setText(locality);
                        } else if (v[position].workplaceType != null) {
                            String type = "";
                            if (v[position].workplaceType.contains(Vacancy.TYPE_ON_SITE) && v[position].workplaceType.contains(Vacancy.TYPE_HYBRID) && v[position].workplaceType.contains(Vacancy.TYPE_REMOTE)) {
                                type += getString(R.string.in_person);
                                type += ", ";
                                type += getString(R.string.hybrid);
                                type += " " + getString(R.string.and) + " ";
                                type += getString(R.string.remote);
                            } else if (v[position].workplaceType.contains(Vacancy.TYPE_ON_SITE) && v[position].workplaceType.contains(Vacancy.TYPE_HYBRID)) {
                                type += getString(R.string.in_person);
                                type += " " + getString(R.string.and) + " ";
                                type += getString(R.string.hybrid);
                            } else if (v[position].workplaceType.contains(Vacancy.TYPE_HYBRID) && v[position].workplaceType.contains(Vacancy.TYPE_REMOTE)) {
                                type += getString(R.string.hybrid);
                                type += " " + getString(R.string.and) + " ";
                                type += getString(R.string.remote);
                            } else if (v[position].workplaceType.contains(Vacancy.TYPE_ON_SITE) && v[position].workplaceType.contains(Vacancy.TYPE_REMOTE)) {
                                type += getString(R.string.in_person);
                                type += " " + getString(R.string.and) + " ";
                                type += getString(R.string.remote);
                            } else if (v[position].workplaceType.contains(Vacancy.TYPE_ON_SITE)) {
                                type = getString(R.string.in_person);
                            } else if (v[position].workplaceType.contains(Vacancy.TYPE_HYBRID)) {
                                type = getString(R.string.hybrid);
                            } else if (v[position].workplaceType.contains(Vacancy.TYPE_REMOTE)) {
                                type = getString(R.string.remote);
                            }
                            ((TextView) root.findViewById(R.id.textCenterStart)).setText(type);
                        } else {
                            String locality = "";
                            if (!v[position].locality.city.isEmpty())
                                locality = v[position].locality.city;
                            if (!v[position].locality.state.isEmpty()) {
                                if (!locality.isEmpty()) locality += ", ";
                                locality += v[position].locality.state;
                            }
                            if (!v[position].locality.country.isEmpty()) {
                                if (!locality.isEmpty()) locality += " - ";
                                locality += v[position].locality.country;
                            }
                            ((TextView) root.findViewById(R.id.textCenterStart)).setText(locality);
                        }
                    } else {
                        root = inflater.inflate(R.layout.item_list, null, false);
                        ((TextView) root.findViewById(R.id.textTopStart)).setText(v[position].name);
                    }
                } else {
                    root = inflater.inflate(R.layout.item_list, null, false);
                    String s;
                    if (position == v.length) s = getString(R.string.all_vacancies);
                    else s = getString(R.string.another);
                    ((TextView) root.findViewById(R.id.textTopStart)).setText(s);
                }
                return root;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.dismiss();
                if (position == v.length) {
                    sliderView.removeAllFragments();
                    Notify.showToast(String.format(Locale.getDefault(), getString(R.string.searching_for), getString(R.string.all_vacancies)));
                    dialog.dismiss();
                    textCounter.setText(String.format(Locale.getDefault(), getString(R.string.format_requesting), 0, vacanciesCounter));
                    textStatus.setText(R.string.searching_for_vacancy);
                    vacanciesRequester.request(v);
                    return;
                } else if (position == v.length + 1) {
                    dialog.dismiss();
                    showDialogVacancySearch();
                    return;
                }
                sliderView.removeAllFragments();
                Notify.showToast(String.format(Locale.getDefault(), getString(R.string.searching_for), v[position].name));
                textCounter.setText(String.format(Locale.getDefault(), getString(R.string.format_requesting), 0, 1));
                textStatus.setText(R.string.searching_for_vacancy);
                vacanciesRequester.request(new Vacancy[]{v[position]});
            }
        });
        dialog.show();
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
            showDialogOptions();
        }
        getOnBackInvokedDispatcher().registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, onBackPressed);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (dialog != null && dialog.isShowing()) dialog.dismiss();
        sliderView.onResume();
        getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(onBackPressed);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        vacanciesRequester.cancel();
    }

    private OnBackInvokedCallback onBackPressed = new OnBackInvokedCallback() {
        @Override
        public void onBackInvoked() {
            if (isFinishing()) return;
            if (sliderView.onBackPressed()) {
                startActivity(new Intent(VacanciesSearchActivity.this, VacanciesNewsActivity.class), ActivityOptions.makeSceneTransitionAnimation(VacanciesSearchActivity.this).toBundle());
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
