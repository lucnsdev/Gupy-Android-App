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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lucns.gupy.R;
import lucns.gupy.rh.GupyUtils;
import lucns.gupy.rh.models.Enterprise;
import lucns.gupy.rh.models.Vacancy;
import lucns.gupy.utils.Annotator;
import lucns.gupy.utils.Notify;

public class EnterprisesActivity extends Activity {
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
                Enterprise enterprise = (Enterprise) adapterView.getItemAtPosition(i);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(enterprise.url));
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

    private void search() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Enterprise[] all = GupyUtils.getAllEnterprisesWithVacancies("GlobalEnterprisesDataNewer");
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (all == null) {
                            progressBar.setVisibility(View.INVISIBLE);
                            return;
                        }
                        Map<String, Enterprise> map = new HashMap<>();
                        for (Enterprise enterprise : all) {
                            if (enterprise.vacancies != null) {
                                for (Vacancy vacancy : enterprise.vacancies) {
                                    for (String city : cities) {
                                        if (vacancy.locality.city.toLowerCase().contains(city.toLowerCase())) {
                                            vacancy.enterpriseName = enterprise.name;
                                            vacancy.url = enterprise.url;
                                            map.put(enterprise.url, enterprise);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        if (map.isEmpty()) {
                            progressBar.setVisibility(View.INVISIBLE);
                            Notify.showToast(R.string.no_news);
                            return;
                        }

                        List<Enterprise> valueList = new ArrayList<>(map.values());
                        Enterprise[] enterprises = valueList.toArray(new Enterprise[0]);
                        Arrays.sort(enterprises, new Comparator<Enterprise>() {

                            @Override
                            public int compare(Enterprise v, Enterprise v2) {
                                return v.name.compareTo(v2.name);
                            }
                        });
                        textStatus.setText(enterprises.length + "/" + getString(R.string.enterprises));

                        LayoutInflater inflater = LayoutInflater.from(EnterprisesActivity.this);
                        View[] views = new View[enterprises.length];
                        listView.setAdapter(new ArrayAdapter<Enterprise>(EnterprisesActivity.this, 0, enterprises) {
                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                if (views[position] == null) {
                                    View view = inflater.inflate(R.layout.item_list_two, null, false);
                                    if (position == enterprises.length - 1) view.setBackgroundResource(R.drawable.item_background_end);
                                    else if (position == 0) view.setBackgroundResource(R.drawable.item_background_start);
                                    else view.setBackgroundResource(R.drawable.item_background);

                                    TextView textTopStart = view.findViewById(R.id.textTopStart);
                                    TextView textCenterStart = view.findViewById(R.id.textCenterStart);
                                    TextView textCenterEnd = view.findViewById(R.id.textCenterEnd);
                                    TextView textBottomStart = view.findViewById(R.id.textBottomStart);
                                    textTopStart.setText((position + 1) + " - " + enterprises[position].name);
                                    textBottomStart.setText(enterprises[position].vacancies.length + getString(R.string.vacancies));
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
