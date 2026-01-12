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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;

import lucns.gupy.R;
import lucns.gupy.utils.Annotator;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ExecutionsActivity extends Activity {

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_executions);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        listView = findViewById(R.id.listView);
        updateList();
    }

    private void updateList() {
        Annotator annotator = new Annotator("JobData.json");
        if (!annotator.exists()) {
            listView.setVisibility(View.INVISIBLE);
            return;
        }
        long[] timeRegisters;
        try {
            JSONArray jsonArray = new JSONArray(annotator.getContent());
            timeRegisters = new long[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                timeRegisters[i] = jsonArray.getLong(i);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        View[] views = new View[timeRegisters.length];
        LayoutInflater inflater = LayoutInflater.from(this);
        listView.setAdapter(new ArrayAdapter<Long>(this, R.layout.item_list) {

            @Override
            public int getCount() {
                return timeRegisters.length;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (views[position] == null) {
                    views[position] = inflater.inflate(R.layout.item_list, parent, false);
                    ((TextView) views[position].findViewById(R.id.textTopStart)).setText(getDateTime(timeRegisters[position]));
                }
                return views[position];
            }
        });
        listView.setVisibility(View.VISIBLE);
    }

    private String getDateTime(long milliseconds) {
        SimpleDateFormat date = new SimpleDateFormat("HH:mm ss - dd/MM/yyyy", Locale.getDefault());
        return date.format(milliseconds);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                startActivity(new Intent(ExecutionsActivity.this, VacanciesSearchActivity.class), ActivityOptions.makeSceneTransitionAnimation(ExecutionsActivity.this).toBundle());
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
