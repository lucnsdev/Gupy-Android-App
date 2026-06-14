package lucns.gupy.views.slider;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;

public class CircleIndicator extends LinearLayout {

    private final static int DEFAULT_INDICATOR_WIDTH = 5;

    protected int mIndicatorMargin = -1;
    protected int mIndicatorWidth = -1;
    protected int mIndicatorHeight = -1;

    protected ColorStateList mIndicatorTintColor;
    protected ColorStateList mIndicatorTintUnselectedColor;

    protected Animator mAnimatorOut;
    protected Animator mAnimatorIn;
    protected Animator mImmediateAnimatorOut;
    protected Animator mImmediateAnimatorIn;

    protected int mLastPosition = -1;

    public CircleIndicator(Context context) {
        super(context);
        initialize();
    }

    public CircleIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public void initialize() {
        int miniSize = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_INDICATOR_WIDTH, getResources().getDisplayMetrics()) + 0.5f);
        mIndicatorWidth = miniSize;
        mIndicatorHeight = miniSize;
        mIndicatorMargin = (int) (miniSize / 1.5f);

        mAnimatorOut = createAnimatorOut();
        mImmediateAnimatorOut = createAnimatorOut();
        mImmediateAnimatorOut.setDuration(0);

        mAnimatorIn = createAnimatorIn();
        mImmediateAnimatorIn = createAnimatorIn();
        mImmediateAnimatorIn.setDuration(0);

        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);
    }

    public void tintIndicator(int indicatorColor) {
        tintIndicator(indicatorColor, indicatorColor);
    }

    public void tintIndicator(int indicatorColor, int unselectedIndicatorColor) {
        mIndicatorTintColor = ColorStateList.valueOf(indicatorColor);
        mIndicatorTintUnselectedColor = ColorStateList.valueOf(unselectedIndicatorColor);
    }

    protected Animator createAnimatorOut() {
        long duration = 100;
        LinearInterpolator intertpolator = new LinearInterpolator();

        ObjectAnimator alpha = ObjectAnimator.ofFloat(null, "alpha", 0.5f, 1f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(null, "scaleX", 1f, 1.8f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(null, "scaleY", 1f, 1.8f);

        AnimatorSet set = new AnimatorSet();
        set.setDuration(duration);
        set.setInterpolator(intertpolator);
        set.playTogether(alpha, scaleX, scaleY);
        return set;
    }

    protected Animator createAnimatorIn() {
        long duration = 100;
        LinearInterpolator intertpolator = new LinearInterpolator();

        ObjectAnimator alpha = ObjectAnimator.ofFloat(null, "alpha", 0.5f, 1f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(null, "scaleX", 1f, 1.8f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(null, "scaleY", 1f, 1.8f);

        AnimatorSet set = new AnimatorSet();
        set.setDuration(duration);
        set.setInterpolator(intertpolator);
        set.playTogether(alpha, scaleX, scaleY);

        Animator animator = (Animator) set;
        animator.setInterpolator(new Interpolator() {
            @Override
            public float getInterpolation(float value) {
                return Math.abs(1.0f - value);
            }
        });
        return animator;
    }

    public void createIndicators(int count, int currentPosition) {
        if (mImmediateAnimatorOut.isRunning()) {
            mImmediateAnimatorOut.end();
            mImmediateAnimatorOut.cancel();
        }

        if (mImmediateAnimatorIn.isRunning()) {
            mImmediateAnimatorIn.end();
            mImmediateAnimatorIn.cancel();
        }

        // Diff View
        int childViewCount = getChildCount();
        if (count < childViewCount) {
            removeViews(count, childViewCount - count);
        } else if (count > childViewCount) {
            int addCount = count - childViewCount;
            int orientation = getOrientation();
            for (int i = 0; i < addCount; i++) {
                addIndicator(orientation);
            }
        }

        for (int i = 0; i < count; i++) {
            CircleView v = (CircleView) getChildAt(i);
            if (currentPosition == i) {
                v.setActivated(true);
                mImmediateAnimatorOut.setTarget(v);
                mImmediateAnimatorOut.start();
                mImmediateAnimatorOut.end();
            } else {
                v.setActivated(false);
                mImmediateAnimatorIn.setTarget(v);
                mImmediateAnimatorIn.start();
                mImmediateAnimatorIn.end();
            }
        }

        mLastPosition = currentPosition;
    }

    protected void addIndicator(int orientation) {
        CircleView indicator = new CircleView(getContext());
        LayoutParams params = generateDefaultLayoutParams();
        params.width = mIndicatorWidth;
        params.height = mIndicatorHeight;
        if (orientation == HORIZONTAL) {
            params.leftMargin = mIndicatorMargin;
            params.rightMargin = mIndicatorMargin;
        } else {
            params.topMargin = mIndicatorMargin;
            params.bottomMargin = mIndicatorMargin;
        }
        addView(indicator, params);
    }

    public void setPosition(int position) {
        if (mLastPosition == position) {
            return;
        }

        if (mAnimatorIn.isRunning()) {
            mAnimatorIn.end();
            mAnimatorIn.cancel();
        }

        if (mAnimatorOut.isRunning()) {
            mAnimatorOut.end();
            mAnimatorOut.cancel();
        }

        CircleView v = (CircleView) getChildAt(mLastPosition);
        if (mLastPosition >= 0 && v != null) {
            v.setActivated(false);
            mAnimatorIn.setTarget(v);
            mAnimatorIn.start();
        }

        v = (CircleView) getChildAt(position);
        if (v != null) {
            v.setActivated(true);
            mAnimatorOut.setTarget(v);
            mAnimatorOut.start();
        }
        mLastPosition = position;
    }

    protected void changeIndicatorColor() {
        int count = getChildCount();
        if (count <= 0) return;
        for (int i = 0; i < count; i++) {
            CircleView v = (CircleView) getChildAt(i);
            v.setActivated(i == mLastPosition);
        }
    }

    private static class CircleView extends View {

        private final Paint paint;

        public CircleView(Context context) {
            super(context);
            paint = new Paint();
            paint.setColor(getContext().getColor(android.R.color.white));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int size = Math.min(getWidth(), getHeight());
            if (size <= 0) return;
            canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
        }
    }
}
