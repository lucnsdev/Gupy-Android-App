package lucns.gupy.views.popup;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import lucns.gupy.R;

public class PopupView extends PopupWindow {

    private final Context context;

    public PopupView(Context context) {
        this.context = context;
        setAnimationStyle(R.style.PopupAnimationTheme);
        setOutsideTouchable(true);

        float radius = 48;
        RoundRectShape roundRectShape = new RoundRectShape(new float[]{radius, radius, radius, radius, radius, radius, radius, radius}, null, null);
        ShapeDrawable shapeDrawable = new ShapeDrawable(roundRectShape);
        shapeDrawable.getPaint().setColor(context.getColor(R.color.background_popup));
        setBackgroundDrawable(shapeDrawable);
    }

    protected Context getContext() {
        return context;
    }

    public void showOverlay(View view) {
        setWidth(view.getWidth());
        int[] coordinates = new int[2];
        view.getLocationOnScreen(coordinates);
        showAtLocation(view, Gravity.NO_GRAVITY, coordinates[0], coordinates[1]);
    }
}
