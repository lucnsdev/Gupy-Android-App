package lucns.gupy.views.popup;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import lucns.gupy.R;

public class PopupListView extends PopupView {

    private ListView listView;
    private AdapterView.OnItemClickListener onItemClickListener;


    public PopupListView(Context context) {
        super(context);

        ColorDrawable divider = new ColorDrawable();
        divider.setColor(Color.argb(28, 255, 255, 255));
        ColorDrawable selector = new ColorDrawable();
        selector.setColor(Color.TRANSPARENT);
        ShapeDrawable scrollDrawable = new ShapeDrawable();
        scrollDrawable.setIntrinsicWidth(2);
        scrollDrawable.getPaint().setColor(context.getColor(R.color.accent));
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getContext().getResources().getDisplayMetrics()); // 8dp
        listView = new ListView(context);
        listView.setPadding(padding, padding, padding, padding);
        listView.setDivider(divider);
        listView.setDividerHeight(2);
        listView.setSelector(selector);
        listView.setVerticalScrollbarThumbDrawable(scrollDrawable);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dismiss();
                if (onItemClickListener != null) onItemClickListener.onItemClick(parent, view, position, id);
            }
        });
        setContentView(listView);
    }

    public void setTitles(String[] titles, int selectedIndex) {
        int padding12 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getContext().getResources().getDisplayMetrics());
        int padding16 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getContext().getResources().getDisplayMetrics());
        View[] views = new View[titles.length];
        listView.setAdapter(new ArrayAdapter<String>(getContext(), 0, titles) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (views[position] == null) {
                    TextView tv = new TextView(getContext());
                    tv.setPadding(padding16, padding12, padding16, padding12);
                    tv.setText(titles[position]);
                    tv.setTypeface(Typeface.DEFAULT_BOLD);
                    tv.setMaxLines(1);
                    tv.setTextColor(getContext().getColor(selectedIndex == position ? R.color.main : R.color.text_primary));
                    tv.setBackground(getContext().getDrawable(R.drawable.item_background));
                    views[position] = tv;
                } else {
                    ((TextView) views[position]).setTextColor(getContext().getColor(selectedIndex == position ? R.color.main : R.color.text_primary));
                }
                return views[position];
            }
        });
        int textHeight = 16;
        setHeight(Math.min(Resources.getSystem().getDisplayMetrics().heightPixels / 3, (titles.length * ((padding12 * 2) + textHeight)) + (padding12 * 2) + textHeight));
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
