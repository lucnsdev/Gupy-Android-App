package lucns.gupy.activities;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import lucns.gupy.R;
import lucns.gupy.rh.GupyUtils;
import lucns.gupy.rh.api.LocalityProvider;
import lucns.gupy.rh.api.ResponseCallback;
import lucns.gupy.rh.models.Locality;
import lucns.gupy.rh.models.Vacancy;
import lucns.gupy.utils.Annotator;
import lucns.gupy.utils.Notify;
import lucns.gupy.utils.Utils;
import lucns.gupy.views.CustomSpinner;
import lucns.gupy.views.IndeterminateThreeBalls;

public class VacancyRegisterActivity extends Activity {

    private CustomSpinner spinnerStates, spinnerCities;
    private IndeterminateThreeBalls progressBarStates, progressBarCities;
    private LocalityProvider localityProvider;
    private Locality[] states, cities;
    private Locality locality;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        setContentView(R.layout.activity_register_vacancy);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        locality = new Locality();

        spinnerStates = findViewById(R.id.spinnerStates);
        spinnerCities = findViewById(R.id.spinnerCities);
        progressBarStates = findViewById(R.id.progressBarStates);
        progressBarCities = findViewById(R.id.progressBarCities);
        spinnerStates.setText(getString(R.string.select_a_state));
        spinnerCities.setText(getString(R.string.select_a_city));
        spinnerStates.setEnabled(false);
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

        CheckBox checkBoxOnSite = findViewById(R.id.checkBoxOnSite);
        CheckBox checkBoxHybrid = findViewById(R.id.checkBoxHybrid);
        CheckBox checkBoxRemote = findViewById(R.id.checkBoxRemote);

        EditText editText = findViewById(R.id.editText);
        Button button = findViewById(R.id.buttonRegister);
        button.setEnabled(false);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String type = null;
                if (checkBoxOnSite.isChecked()) type = Vacancy.TYPE_ON_SITE;
                if (checkBoxHybrid.isChecked()) type += "," + Vacancy.TYPE_HYBRID;
                if (checkBoxRemote.isChecked()) type += "," + Vacancy.TYPE_REMOTE;
                try {
                    Annotator annotator = new Annotator("VacanciesRegistered.json");
                    JSONObject jsonObject;
                    if (annotator.exists()) {
                        jsonObject = new JSONObject(annotator.getContent());
                    } else {
                        jsonObject = new JSONObject();
                        jsonObject.put("data", new JSONArray());
                    }
                    JSONObject jsonVacancy = new JSONObject();
                    jsonVacancy.put("name", editText.getText().toString());
                    if (type != null) jsonVacancy.put("workplaceType", type);
                    if (locality.state != null) jsonVacancy.put("state", locality.state);
                    if (locality.city != null) jsonVacancy.put("city", locality.city);
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    jsonArray.put(jsonVacancy);
                    annotator.setContent(jsonObject.toString(4));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                locality.state = null;
                locality.city = null;
                button.setEnabled(false);
                editText.getText().clear();
                spinnerStates.setText(getString(R.string.select_a_state));
                spinnerCities.setText(getString(R.string.select_a_city));
                checkBoxOnSite.setChecked(false);
                checkBoxHybrid.setChecked(false);
                checkBoxRemote.setChecked(false);

                Notify.showToast(R.string.registered);
                finishAction();
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

        localityProvider = new LocalityProvider(new ResponseCallback() {
            @Override
            public void onError(String message, int code) {
                Utils.vibrate();
                Notify.showToast("Error: " + code + "\n" + message);
            }

            @Override
            public void onLocalityAvailable(Locality[] localities) {
                Utils.vibrate();
                if (localities[0].city == null) {
                    states = localities;
                    updateStates(localities);
                    return;
                }
                cities = localities;
                updateCities(localities);
            }
        });
    }

    private void updateStates() {
        Annotator annotator = new Annotator("States.json");
        if (!annotator.exists()) {
            if (!Utils.hasInternetConnection()) {
                Notify.showToast(R.string.error_no_connection);
                return;
            }
            spinnerStates.setVisibility(View.INVISIBLE);
            progressBarStates.setVisibility(View.VISIBLE);
            spinnerCities.setEnabled(false);
            spinnerCities.setVisibility(View.VISIBLE);
            localityProvider.requestStates();
            return;
        }
        states = GupyUtils.jsonToStates(annotator.getContent());
        updateStates(states);
    }

    private void updateStates(Locality[] states) {
        String[] titles = new String[states.length];
        for (int i = 0; i < states.length; i++) titles[i] = states[i].state;
        spinnerStates.setTitles(titles, -1);
        spinnerStates.setEnabled(true);
        spinnerStates.setVisibility(View.VISIBLE);
        progressBarStates.setVisibility(View.INVISIBLE);
    }

    private void updateCities(Locality[] cities) {
        String[] titles = new String[cities.length];
        for (int i = 0; i < cities.length; i++) titles[i] = cities[i].city;
        spinnerCities.setTitles(titles, -1);
        spinnerCities.setEnabled(true);
        spinnerCities.setVisibility(View.VISIBLE);
        progressBarCities.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStates();
        getOnBackInvokedDispatcher().registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, onBackPressed);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(onBackPressed);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        localityProvider.cancel();
    }

    private void finishAction() {
        startActivity(new Intent(VacancyRegisterActivity.this, VacanciesRegisteredActivity.class), ActivityOptions.makeSceneTransitionAnimation(VacancyRegisterActivity.this).toBundle());
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 500);
    }

    private final OnBackInvokedCallback onBackPressed = new OnBackInvokedCallback() {
        @Override
        public void onBackInvoked() {
            finishAction();
        }
    };
}
