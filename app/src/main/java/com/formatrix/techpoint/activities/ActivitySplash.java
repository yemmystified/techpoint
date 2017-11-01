package com.formatrix.techpoint.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.formatrix.techpoint.R;

import oak.svg.AnimatedSvgView;

import static com.formatrix.techpoint.utils.Utils.getWindowSize;

/**
 * Design and developed by formatrix.com
 *
 * ActivitySplash is created to display welcome screen.
 * Created using AppCompatActivity.
 */
public class ActivitySplash extends AppCompatActivity {

    private AnimatedSvgView mAnimatedSvgView;
    private Handler mHandler = new Handler();
    private ImageView splashImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Configuration in Android API below 21 to set window to full screen.
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Create loading to wait for few second before displaying ActivityHome
         new Loading().execute();
        setResponsiveSplashLogo();


//        this is for the logopath activity
//****************************************************************************************************
//        mAnimatedSvgView = (AnimatedSvgView) findViewById(R.id.animated_svg_viewb);
//
//        mAnimatedSvgView.setGlyphStrings(MyLogoPath.ANDROID_GLYPHS);
//
//        // ARGB values for each glyph
//        mAnimatedSvgView.setFillPaints(
//                new int[]{255, 255, 255, 255}, //alpha
//                new int[]{232, 232, 232, 232}, //red
//                new int[]{76, 76, 76, 76}, //green
//                new int[]{61, 61, 61, 61}); //blue
//
////        int traceColor = Color.argb(255, 0, 0, 0); //default
////        int traceColor = Color.argb(253, 253, 253, 253);
//        int traceColor = Color.argb(255, 255, 255, 255); //newset
//        int[] traceColors = new int[4]; // 4 glyphs
////        int residueColor = Color.argb(253, 253, 253, 253);//white residue color
//        int residueColor = Color.argb(50, 50, 50, 50);  //default color
//        int[] residueColors = new int[4]; // 4 glyphs
//
//        // Every glyph will have the same trace/residue
//        for (int i = 0; i < traceColors.length; i++) {
//            traceColors[i] = traceColor;
//            residueColors[i] = residueColor;
//        }
//        mAnimatedSvgView.setTraceColors(traceColors);
//        mAnimatedSvgView.setTraceResidueColors(residueColors);
//**************************************************************************************************






    }

//    this is the on post create to start animation

//****************************************************************************************************
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mAnimatedSvgView.start();
//            }
//        }, 0);
    }
//****************************************************************************************************
    // Asynctask class to process loading in background
    public class Loading extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Thread.sleep(2000);
            }catch(InterruptedException ie){
                ie.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // When progress finished, open ActivityHome
            Intent homeIntent = new Intent(getApplicationContext(), ActivityHome.class);
            startActivity(homeIntent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    // Configuration in Android API 21 to set window to full screen.
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            if (hasFocus) {
                getWindow().getDecorView()
                        .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        }
    }

    private void setResponsiveSplashLogo() {
        splashImageView = (ImageView)findViewById(R.id.splashImageView);
        splashImageView.getLayoutParams().width = (int) (getWindowSize(this).x * 0.75);
    }



}