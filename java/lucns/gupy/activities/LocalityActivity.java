package lucns.gupy.activities;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import lucns.gupy.R;
import lucns.gupy.rh.GupyUtils;
import lucns.gupy.rh.api.LocalityProvider;
import lucns.gupy.rh.api.ResponseCallback;
import lucns.gupy.rh.models.Locality;
import lucns.gupy.utils.Annotator;
import lucns.gupy.utils.Notify;
import lucns.gupy.utils.Utils;
import lucns.gupy.views.CustomSpinner;
import lucns.gupy.views.IndeterminateThreeBalls;

public class LocalityActivity extends Activity {

    private CustomSpinner spinner;
    private IndeterminateThreeBalls progressBar;
    private LocalityProvider localityProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);

        setContentView(R.layout.activity_locality);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        spinner = findViewById(R.id.spinner);
        spinner.setText(getString(R.string.select_a_state));
        spinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        progressBar = findViewById(R.id.progressBar);

        localityProvider = new LocalityProvider(new ResponseCallback() {
            @Override
            public void onError(String message, int code) {
                Utils.vibrate();
                Notify.showToast("Error: " + code + "\n" + message);
            }

            @Override
            public void onLocalityAvailable(Locality[] localities) {
                Utils.vibrate();
                updateItems(localities);
            }
        });
    }

    private void updateItems() {
        Annotator annotator = new Annotator("States.json");
        if (!annotator.exists()) {
            if (!Utils.hasInternetConnection()) {
                Notify.showToast(R.string.error_no_connection);
                return;
            }
            progressBar.setVisibility(View.VISIBLE);
            localityProvider.requestStates();
            return;
        }
        Locality[] states = GupyUtils.jsonToStates(annotator.getContent());
        String[] titles = new String[states.length];
        for (int i = 0; i < states.length; i++) titles[i] = states[i].state;
        spinner.setTitles(titles, -1);
    }

    private void updateItems(Locality[] localities) {

        /*
        ShapeDrawable shapeDrawable = new ShapeDrawable();
        shapeDrawable.setIntrinsicWidth(1);
        shapeDrawable.getPaint().setColor(getColor(R.color.main));
        spinner.setVerticalScrollbarThumbDrawable(shapeDrawable);
        spinner.setSelector
        spinner.setAdapter(new ArrayAdapter<Locality>(this, 0, localities) {

            View[] views = new View[localities.length + 1];
            int padding = (int) getResources().getDimension(R.dimen.margin_3);

            @Override
            public int getCount() {
                return localities.length + 1;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (views[position] == null) {
                    TextView textView = new TextView(LocalityActivity.this);
                    textView.setPadding(padding, padding, padding, padding);
                    textView.setText(position == 0 ? getString(R.string.select_a_state) : localities[position - 1].state);
                    textView.setTypeface(Typeface.DEFAULT_BOLD);
                    textView.setSingleLine(true);
                    RelativeLayout relativeLayout = new RelativeLayout(LocalityActivity.this);
                    relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    relativeLayout.addView(textView);
                    relativeLayout.setBackground(getDrawable(R.drawable.item_background));
                    views[position] = relativeLayout;
                    if (position == 0) relativeLayout.setAlpha(0.5f);
                }
                return views[position];
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                if (views[position] == null) {
                    TextView textView = new TextView(LocalityActivity.this);
                    textView.setPadding(padding, padding, padding, padding);
                    textView.setText(position == 0 ? getString(R.string.select_a_state) : localities[position - 1].state);
                    textView.setTypeface(Typeface.DEFAULT_BOLD);
                    textView.setSingleLine(true);
                    RelativeLayout relativeLayout = new RelativeLayout(LocalityActivity.this);
                    relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    relativeLayout.addView(textView);
                    relativeLayout.setBackground(getDrawable(R.drawable.item_background));
                    views[position] = relativeLayout;
                    if (position == 0) {
                        relativeLayout.setOnClickListener(null);
                        relativeLayout.setEnabled(false);
                        relativeLayout.setAlpha(0.5f);
                    }
                }
                return views[position];
            }
        });
*/
        progressBar.setVisibility(View.INVISIBLE);
        spinner.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateItems();
    }

    @Override
    public void onBackPressed() {
        if (spinner.dismiss()) return;
        if (isFinishing()) return;
        finish();
    }
}
