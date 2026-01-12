package lucns.gupy.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import lucns.gupy.R;
import lucns.gupy.utils.Annotator;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gupy_logo, o);
        ImageView imageLogo = findViewById(R.id.imageLogo);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageLogo.getLayoutParams();
        params.width = bitmap.getWidth();
        params.height = bitmap.getHeight();
        imageLogo.setLayoutParams(params);
        imageLogo.setImageBitmap(bitmap);

        float padding = getResources().getDimension(R.dimen.margin_4);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                TextView text = findViewById(R.id.lucns);
                text.setAlpha(0f);
                text.setVisibility(View.VISIBLE);
                ObjectAnimator alpha = ObjectAnimator.ofFloat(text, View.ALPHA, 0f, 1f);
                alpha.setInterpolator(new LinearInterpolator());
                alpha.setDuration(500);
                alpha.start();
            }
        }, 500);
        ObjectAnimator animator = ObjectAnimator.ofFloat(imageLogo, View.TRANSLATION_Y, 0, (-1) * (Resources.getSystem().getDisplayMetrics().heightPixels / 2f) + (bitmap.getHeight() / 2f) + padding);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(1000);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isFinishing()) return;
                        if (new Annotator("VacanciesRegistered.json").exists()) {
                            //startActivity(new Intent(MainActivity.this, RegisteredVacanciesActivity.class), ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle());
                            startActivity(new Intent(MainActivity.this, VacanciesNewsActivity.class), ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle());
                        } else {
                            startActivity(new Intent(MainActivity.this, VacancyRegisterActivity.class), ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle());
                        }
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (isFinishing()) return;
                                finish();
                            }
                        }, 1000);
                    }
                }, 500);
            }
        });
        animator.start();
    }
}