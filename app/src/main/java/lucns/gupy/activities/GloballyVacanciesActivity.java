package lucns.gupy.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import lucns.gupy.R;
import lucns.gupy.rh.GupyUtils;
import lucns.gupy.rh.models.Enterprise;
import lucns.gupy.rh.models.Vacancy;
import lucns.gupy.utils.Annotator;
import lucns.gupy.utils.Notify;

public class GloballyVacanciesActivity extends Activity {
    private ListView listView;
    private ProgressBar progressBar;
    private TextView textStatus;
    private String[] cities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        textStatus = findViewById(R.id.textStatus);
        listView = findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Vacancy vacancy = (Vacancy) adapterView.getItemAtPosition(i);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(vacancy.url));
                startActivity(browserIntent);
            }
        });
        progressBar = findViewById(R.id.progressBar);

        List<String> listCities = new ArrayList<>();
        Annotator annotator = new Annotator("VacanciesRegistered.json");
        if (!annotator.exists()) return;
        try {
            JSONObject jsonObject = new JSONObject(annotator.getContent());
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonVacancy = jsonArray.getJSONObject(i);
                if (jsonVacancy.has("city")) listCities.add(jsonVacancy.getString("city"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        cities = listCities.toArray(new String[0]);
        search();
    }

    private List<Vacancy> getVacanciesList(Enterprise[] enterprises) {
        List<Vacancy> list = new ArrayList<>();
        for (Enterprise enterprise : enterprises) {
            if (enterprise.vacancies != null) {
                for (Vacancy vacancy : enterprise.vacancies) {
                    for (String city : cities) {
                        if (vacancy.locality.city.toLowerCase().contains(city.toLowerCase())) {
                            vacancy.enterpriseName = enterprise.name;
                            list.add(vacancy);
                            break;
                        }
                    }
                }
            }
        }
        return list;
    }

    private List<Vacancy> compare(Enterprise[] newer, Enterprise[] older) {
        List<Vacancy> n = getVacanciesList(newer);
        List<Vacancy> o = getVacanciesList(older);
        List<Vacancy> v = new ArrayList<>();
        boolean founded;
        for (Vacancy vacancy : n) {
            founded = false;
            for (Vacancy vacancy2 : o) {
                if (vacancy.id == vacancy2.id) {
                    founded = true;
                    break;
                }
            }
            if (!founded) v.add(vacancy);
        }
        return v;
    }

    private void search() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Enterprise[] newer = GupyUtils.getAllEnterprisesWithVacancies("GlobalEnterprisesDataNewer.json");
                Enterprise[] older = GupyUtils.getAllEnterprisesWithVacancies("GlobalEnterprisesDataOlder.json");
                List<Vacancy> list = compare(newer, older);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (newer == null) {
                            progressBar.setVisibility(View.INVISIBLE);
                            return;
                        }
                        if (list.isEmpty()) {
                            progressBar.setVisibility(View.INVISIBLE);
                            Notify.showToast(R.string.no_news);
                            return;
                        }

                        Vacancy[] vacancies = list.toArray(new Vacancy[0]);
                        Arrays.sort(vacancies, new Comparator<Vacancy>() {

                            @Override
                            public int compare(Vacancy v, Vacancy v2) {
                                return Integer.compare(v2.id, v.id);
                            }
                        });
                        textStatus.setText(vacancies.length + "/" + getString(R.string.vacancies));

                        LayoutInflater inflater = LayoutInflater.from(GloballyVacanciesActivity.this);
                        View[] views = new View[vacancies.length];
                        listView.setAdapter(new ArrayAdapter<Vacancy>(GloballyVacanciesActivity.this, 0, vacancies) {
                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                if (views[position] == null) {
                                    View view = inflater.inflate(R.layout.item_list_two, null, false);
                                    if (position == vacancies.length - 1) view.setBackgroundResource(R.drawable.item_background_end);
                                    else if (position == 0) view.setBackgroundResource(R.drawable.item_background_start);
                                    else view.setBackgroundResource(R.drawable.item_background);

                                    TextView textTopStart = view.findViewById(R.id.textTopStart);
                                    TextView textCenterStart = view.findViewById(R.id.textCenterStart);
                                    TextView textCenterEnd = view.findViewById(R.id.textCenterEnd);
                                    TextView textBottomStart = view.findViewById(R.id.textBottomStart);
                                    textTopStart.setText((position + 1) + " - " + vacancies[position].name);
                                    textBottomStart.setText(vacancies[position].enterpriseName);
                                    if (vacancies[position].locality.state != null)
                                        textCenterStart.setText(vacancies[position].locality.state);
                                    if (vacancies[position].locality.city != null)
                                        textCenterEnd.setText(vacancies[position].locality.city);
                                    views[position] = view;
                                }
                                return views[position];
                            }
                        });
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }).start();
    }
}
