package lucns.gupy.activities;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import lucns.gupy.R;
import lucns.gupy.rh.models.Locality;
import lucns.gupy.rh.models.Vacancy;
import lucns.gupy.utils.Annotator;
import lucns.gupy.utils.Utils;

public class VacanciesRegisteredActivity extends Activity {

    private ListView listView;
    private TextView textStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        setContentView(R.layout.activity_registered_vacancies);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(VacanciesRegisteredActivity.this, VacancyRegisterActivity.class), ActivityOptions.makeSceneTransitionAnimation(VacanciesRegisteredActivity.this).toBundle());
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 500);
            }
        });

        listView = findViewById(R.id.listView);
        textStatus = findViewById(R.id.textStatus);
    }

    private void updateList() {
        Annotator annotator = new Annotator("VacanciesRegistered.json");
        if (!annotator.exists()) return;
        Vacancy[] v;
        try {
            JSONObject jsonObject = new JSONObject(annotator.getContent());
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            v = new Vacancy[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonVacancy = jsonArray.getJSONObject(i);
                Vacancy vacancy = new Vacancy();
                vacancy.name = jsonVacancy.getString("name");
                vacancy.workplaceType = jsonVacancy.optString("workplaceType", null);
                vacancy.locality = new Locality(jsonVacancy.optString("country", null), jsonVacancy.optString("state", null), jsonVacancy.optString("city", null));
                v[i] = vacancy;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        Vacancy[] vacancies = new Vacancy[v.length];
        for (int i = 0; i < vacancies.length; i++) vacancies[i] = v[v.length - i - 1];

        textStatus.setVisibility(View.INVISIBLE);
        listView.setVisibility(View.VISIBLE);
        LayoutInflater inflater = LayoutInflater.from(this);
        View[] views = new View[vacancies.length];
        listView.setAdapter(new ArrayAdapter<Vacancy>(this, 0, vacancies) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (views[position] == null) {
                    View view = inflater.inflate(R.layout.item_vacancy, null, false);
                    view.setOnClickListener(null);
                    view.setEnabled(false);
                    /*
                    if (position == vacancies.length - 1) view.setBackgroundResource(R.drawable.item_background_end);
                    else if (position == 0) view.setBackgroundResource(R.drawable.item_background_start);
                    else view.setBackgroundResource(R.drawable.item_background);
                     */
                    ImageButton imageButton = view.findViewById(R.id.buttonTrash);
                    imageButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Utils.vibrate();
                            deleteItem(vacancies[position]);
                            updateList();
                            if (listView.getAdapter().getCount() == 0) {
                                new Annotator("VacanciesNews.json").delete();
                            }
                        }
                    });
                    TextView textTitle = view.findViewById(R.id.textTitle);
                    CheckBox checkBoxOnSite = view.findViewById(R.id.checkBoxOnSite);
                    CheckBox checkBoxHybrid = view.findViewById(R.id.checkBoxHybrid);
                    CheckBox checkBoxRemote = view.findViewById(R.id.checkBoxRemote);
                    TextView textState = view.findViewById(R.id.textStateValue);
                    TextView textCity = view.findViewById(R.id.textCityValue);
                    textTitle.setText(vacancies[position].name);
                    checkBoxOnSite.setChecked(vacancies[position].workplaceType != null && vacancies[position].workplaceType.contains(Vacancy.TYPE_ON_SITE));
                    checkBoxHybrid.setChecked(vacancies[position].workplaceType != null && vacancies[position].workplaceType.contains(Vacancy.TYPE_HYBRID));
                    checkBoxRemote.setChecked(vacancies[position].workplaceType != null && vacancies[position].workplaceType.contains(Vacancy.TYPE_REMOTE));
                    if (vacancies[position].locality.state != null)
                        textState.setText(vacancies[position].locality.state);
                    if (vacancies[position].locality.city != null)
                        textCity.setText(vacancies[position].locality.city);
                    views[position] = view;
                }
                return views[position];
            }
        });
    }

    private void deleteItem(Vacancy vacancy) {
        Annotator annotator = new Annotator("VacanciesRegistered.json");
        if (!annotator.exists()) return;
        JSONArray jsonArray2 = new JSONArray();
        try {
            JSONObject jsonObject = new JSONObject(annotator.getContent());
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonVacancy = jsonArray.getJSONObject(i);
                if (jsonVacancy.getString("name").equals(vacancy.name)) continue;
                jsonArray2.put(jsonVacancy);
            }
            jsonObject.put("data", jsonArray2);
            annotator.setContent(jsonObject.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateList();
        getOnBackInvokedDispatcher().registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, onBackPressed);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(onBackPressed);
    }

    private OnBackInvokedCallback onBackPressed = new OnBackInvokedCallback() {
        @Override
        public void onBackInvoked() {
            if (isFinishing()) return;
            if (new Annotator("VacanciesRegistered.json").exists()) {
                startActivity(new Intent(VacanciesRegisteredActivity.this, VacanciesSearchActivity.class), ActivityOptions.makeSceneTransitionAnimation(VacanciesRegisteredActivity.this).toBundle());
            }
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 500);
        }
    };
}
