package lucns.gupy.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import lucns.gupy.R;
import lucns.gupy.views.popup.PopupListView;
import lucns.gupy.views.popup.PopupView;

public class CustomSpinner extends RelativeLayout {

    private TextView textView;

    private PopupListView popupView;
    private AdapterView.OnItemClickListener onItemClickListener;

    public CustomSpinner(Context context) {
        super(context);
        initialize();
    }

    public CustomSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    private void initialize() {
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()); // 8dp
        setBackground(getContext().getDrawable(R.drawable.border_background));
        setPadding(padding, 0, padding, 0);
        setClickable(true);
        setFocusable(false);
        setFocusableInTouchMode(false);

        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, getResources().getDisplayMetrics()); // 36dp
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, height);
        params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        params.setMarginStart(padding);
        textView = new TextView(getContext());
        textView.setId(View.generateViewId());
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setLayoutParams(params);
        textView.setSingleLine(true);
        textView.setText(getContext().getString(R.string.select_a_state));

        params = new LayoutParams((int) (height / 1.5f), (int) (height / 1.5f));
        params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        //params.addRule(RelativeLayout.END_OF, textView.getId());
        params.setMarginStart(padding / 2);

        Drawable drawable = getContext().getDrawable(R.drawable.icon_expand);
        View arrow = new View(getContext());
        arrow.setLayoutParams(params);
        arrow.setBackground(drawable);
        addView(arrow);
        addView(textView);

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                arrow.setX(getWidth() - drawable.getIntrinsicWidth() - padding);
            }
        });

        popupView = new PopupListView(getContext());
        popupView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                textView.setText((String) parent.getItemAtPosition(position));
                textView.setTextColor(getContext().getColor(R.color.text_primary));
                if (onItemClickListener != null) onItemClickListener.onItemClick(parent, view, position, id);
            }
        });
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isEnabled()) return;
                popupView.showOverlay(CustomSpinner.this);
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setEnabled(isEnabled());
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            setAlpha(1f);
        } else {
            setAlpha(0.75f);
        }
    }

    public void setText(String text) {
        textView.setText(text);
        textView.setTextColor(getContext().getColor(R.color.text_secondary));
    }

    public boolean dismiss() {
        if (popupView.isShowing()) {
            popupView.dismiss();
            return true;
        }
        return false;
    }

    public void setTitles(String[] titles, int selectedIndex) {
        popupView.setTitles(titles, selectedIndex);
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
