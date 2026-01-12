package lucns.gupy.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;

public class SmoothRelativeLayout extends RelativeLayout {

    private ValueAnimator fade;
    private final long DURATION = 1000;

    public SmoothRelativeLayout(Context context) {
        super(context);
    }

    public SmoothRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SmoothRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SmoothRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setVisibility(int visibility) {
        cancelAnimation();
        if (visibility == View.VISIBLE) {
            setAlpha(0);
            super.setVisibility(View.VISIBLE);
        }
        final int maxAlpha = 1000;
        int value = (int) (getAlpha() * maxAlpha);
        fade = new ValueAnimator();
        fade.setDuration(DURATION);
        fade.setIntValues(value, visibility == View.VISIBLE ? maxAlpha : 0);
        fade.setInterpolator(new AccelerateDecelerateInterpolator());
        fade.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                float alpha = (float) value / (float) maxAlpha;
                setAlpha(alpha);
            }
        });
        if (visibility == View.INVISIBLE) {
            fade.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    SmoothRelativeLayout.super.setVisibility(View.INVISIBLE);
                }
            });
        }
        fade.start();
    }

    public void cancelAnimation() {
        if (fade != null && fade.isRunning()) fade.cancel();
    }
}
