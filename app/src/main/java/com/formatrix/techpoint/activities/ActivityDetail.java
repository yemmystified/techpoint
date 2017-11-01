package com.formatrix.techpoint.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.formatrix.techpoint.R;
import com.formatrix.techpoint.libs.MySingleton;
import com.formatrix.techpoint.utils.TouchImageView;
import com.formatrix.techpoint.utils.Utils;
import com.formatrix.techpoint.utils.kbv.KenBurnsView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import net.i2p.android.ext.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.formatrix.techpoint.utils.Utils.isNetworkAvailable;

//import com.devspark.robototextview.RobotoTypefaces;

/**
 * Design and developed by formatrix.com
 *
 * ActivityDetail is created to display article detail. Created using ActivityBase.
 */
public class ActivityDetail extends ActivityBase implements
        View.OnClickListener, ObservableScrollViewCallbacks {

    // Create tag for log
    private final String TAG = ActivityDetail.class.getSimpleName();

    // Create view objects
    private Toolbar mToolbar;
    private ObservableScrollView mScrollView;
    private WebView mWebContent;
    private TextView mTxtCategoryName, mTxtTitle, mTxtAuthorAndDate;
    private LinearLayout mLytRetry;
    private CircleProgressBar mPrgLoading;
//    private ImageView mImgImage;
    private TouchImageView mFullImage;
    private AdView mAdView;
    private FloatingActionButton mFabBrowser;
    private View mGradientHeader;
    private FrameLayout mAnchor;
    private AppCompatButton btnRetry;
    private KenBurnsView mImgImage;


    // Create ImageLoader object
    private ImageLoader mImageLoader;

    // Create variables to store data
    private String mArticleId, mTitle, mContent, mDate, mCategoryName, mAuthor, mImageFull, mUrl;

    // Create variables to store dimension values from dimens.xml
    private int mParallaxImageHeight;
    private int mActionBarSize;
    private int mFlexibleSpaceShowFabOffset;

    // Create variables to check view visibility status
    private boolean mFabIsShown;
    private boolean mIsAdmobVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

//        Typeface custom_font = Typeface.createFromAsset(getAssets(),"Bebas Neue.ttf");


        // Get data that pass from previous Activity
        Intent i                    = getIntent();
        mArticleId                  = i.getStringExtra(Utils.EXTRA_ARTICLE_ID);

        // Set ImageLoader object
        mImageLoader                = MySingleton.getInstance(this).getImageLoader();

        // Get dimension value from dimens.xml
        mFlexibleSpaceShowFabOffset = getResources().
                getDimensionPixelSize(R.dimen.flexible_space_show_fab_offset);
        mParallaxImageHeight = getResources().
                getDimensionPixelSize(R.dimen.parallax_image_height);
        mActionBarSize              = getActionBarSize();

        // Connect view objects with view ids in xml
        mToolbar                    = (Toolbar) findViewById(R.id.toolbar);
        mTxtTitle                   = (TextView) findViewById(R.id.txtTitle);
        mTxtCategoryName            = (TextView) findViewById(R.id.txtCategoryName);
        mTxtAuthorAndDate           = (TextView) findViewById(R.id.txtAuthorAndDate);
        mWebContent                 = (WebView) findViewById(R.id.webContent);
//        mImgImage                   = (ImageView) findViewById(R.id.imgImage);
        mImgImage                   = (KenBurnsView) findViewById(R.id.imgImage);
        mPrgLoading                 = (CircleProgressBar) findViewById(R.id.prgLoading);
        mScrollView                 = (ObservableScrollView) findViewById(R.id.scroll);
        mAdView                     = (AdView) findViewById(R.id.adView);
        mLytRetry                   = (LinearLayout) findViewById(R.id.lytRetry);
        mFabBrowser                 = (FloatingActionButton) findViewById(R.id.fabBrowser);
        mGradientHeader             = findViewById(R.id.gradientHeader);
        mAnchor                     = (FrameLayout) findViewById(R.id.anchor);
        btnRetry                    = (AppCompatButton) findViewById(R.id.btnRetry);
        mFullImage                  = (TouchImageView)findViewById(R.id.fullImage);

        // Set toolbar as actionbar
        setSupportActionBar(mToolbar);


        // Add material design icon to the fab button
        mFabBrowser.setIconDrawable(new IconicsDrawable(getApplicationContext())
//                .icon(GoogleMaterial.Icon.gmd_open_in_browser)
                .icon(GoogleMaterial.Icon.gmd_forum)
                .color(Color.WHITE));
        mScrollView.setScrollViewCallbacks(this);

        // When activity launch, hide toolbar and scrollview first
        mScrollView.setVisibility(View.GONE);
        mToolbar.setVisibility(View.GONE);

        // Add listener to button and fab
        btnRetry.setOnClickListener(this);
        mFabBrowser.setOnClickListener(this);

        // Set color to circular progress bar
        mPrgLoading.setColorSchemeResources(R.color.accent_color);

        // Tell that getSupportActionBar() will not be null
        assert getSupportActionBar() != null;
        // Add navigation back button on toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Set toolbar color to transparent in default
        mToolbar.setBackgroundColor(ScrollUtils.getColorWithAlpha(0,
                ContextCompat.getColor(getApplicationContext(), android.R.color.transparent)));
        // Set anchor color to transparent in default
        mAnchor.setBackgroundColor(ScrollUtils.getColorWithAlpha(0,
                ContextCompat.getColor(getApplicationContext(), android.R.color.transparent)));

        // Get admob visibility value
        mIsAdmobVisible = Utils.admobVisibility(mAdView, Utils.IS_ADMOB_VISIBLE);
        // Load ad in background using asynctask class
        new SyncShowAd(mAdView).execute();

        // Get article detail from server
        getArticleDetail(mArticleId);


        /**
         * Making notification bar transparent
         */
        changeStatusBarColor();

        mFullImage.setVisibility(View.GONE);

        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menuShare:
                        // Share article url to other apps
                        Intent iShare = new Intent(Intent.ACTION_SEND);
                        iShare.setType("text/plain");
                        iShare.putExtra(Intent.EXTRA_TEXT, "Hi, check out this post ( " + mUrl + " )");
                        startActivity(Intent.createChooser(
                                iShare, getResources().getString(R.string.share_to)));
                        break;
//                    case R.id.menuComment:
//                        // Display Comment in this article
//                        Intent iComment = new Intent(getApplicationContext(), ActivityComments.class);
//                        iComment.putExtra(Utils.EXTRA_ARTICLE_ID, mArticleId);
//                        startActivity(iComment);
//                        overridePendingTransition(R.anim.open_next, R.anim.close_main);
//                        break;
                }
                return true;
            }
        });

    }


    // Asynctask class to load admob in background
    public class SyncShowAd extends AsyncTask<Void, Void, Void>{

        AdView ad;
        AdRequest adRequest, interstitialAdRequest;
        InterstitialAd interstitialAd;
        int interstitialTrigger;

        public SyncShowAd(AdView ad){
            this.ad = ad;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Check ad visibility. If visible, create adRequest
            if(mIsAdmobVisible) {
                // Create an ad request
                if (Utils.IS_ADMOB_IN_DEBUG) {
                    adRequest = new AdRequest.Builder().
                            addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
                } else {
                    adRequest = new AdRequest.Builder().build();
                }

                // When interstitialTrigger equals ARG_TRIGGER_VALUE, display interstitial ad
                interstitialAd = new InterstitialAd(ActivityDetail.this);
                interstitialAd.setAdUnitId(ActivityDetail.this.getResources()
                        .getString(R.string.interstitial_ad_unit_id));
                interstitialTrigger = Utils.loadIntPreferences(getApplicationContext(),
                        Utils.ARG_ADMOB_PREFERENCE,
                        Utils.ARG_TRIGGER);
                Log.d("Interstitial", "trigger: "+interstitialTrigger);
                if(interstitialTrigger == Utils.ARG_TRIGGER_VALUE) {
                    if(Utils.IS_ADMOB_IN_DEBUG) {
                        interstitialAdRequest = new AdRequest.Builder()
                                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                                .build();
                    }else {
                        interstitialAdRequest = new AdRequest.Builder().build();
                    }
                    Utils.saveIntPreferences(getApplicationContext(),
                            Utils.ARG_ADMOB_PREFERENCE,
                            Utils.ARG_TRIGGER,
                            1);
                }else{
                    Utils.saveIntPreferences(getApplicationContext(),
                            Utils.ARG_ADMOB_PREFERENCE,
                            Utils.ARG_TRIGGER,
                            (interstitialTrigger+1));
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // Check ad visibility. If visible, display ad banner and interstitial
            if(mIsAdmobVisible) {
                // Start loading the ad
                ad.loadAd(adRequest);

                if (interstitialTrigger == Utils.ARG_TRIGGER_VALUE) {
                    // Start loading the ad
                    interstitialAd.loadAd(interstitialAdRequest);

                    // Set the AdListener
                    interstitialAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdLoaded() {
                            if (interstitialAd.isLoaded()) {
                                interstitialAd.show();
                            }
                        }

                        @Override
                        public void onAdFailedToLoad(int errorCode) {

                        }

                        @Override
                        public void onAdClosed() {

                        }

                    });
                }
            }

        }
    }

    // Method to get location detail from server
    private void getArticleDetail(String articleId){

        // Article API url
        String url = Utils.WORDPRESS_URL + Utils.API_BASE + Utils.VALUE_GET_POST + "?" +
                Utils.PARAM_POST_ID + articleId;

        JsonObjectRequest request = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    String status;
                    JSONArray categoriesArray, attachmentArray;
                    JSONObject postObject, authorObject, categoriesObject;
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(Utils.TAG_FORMATRIX + TAG, "onResponse " + response.toString());
                        try {
                            status = response.getString(Utils.KEY_STATUS);
                            if (status.equals("ok")) {

                                postObject      = response.getJSONObject(Utils.OBJECT_NEWS_POST);
                                authorObject    = postObject.getJSONObject(Utils.KEY_AUTHOR);
                                attachmentArray = postObject.getJSONArray(Utils.ARRAY_ATTACHMENTS);
                                categoriesArray = postObject.getJSONArray(Utils.ARRAY_CATEGORIES);

                                // Get article data
                                mTitle          = postObject.getString(Utils.KEY_TITLE);
                                mUrl            = postObject.getString(Utils.KEY_NEWS_URL);
                                mContent        = postObject.getString(Utils.KEY_CONTENT);
                                mDate           = Utils.formattedDate(
                                                    postObject.getString(Utils.KEY_DATE)
                                                    );
                                mAuthor	        = authorObject.getString(Utils.KEY_NAME);

                                // Check whether this article has image
                                if(attachmentArray.length() != 0){
                                    // If available get full size image
                                    JSONObject attachmentObject = attachmentArray.getJSONObject(0);
                                    JSONObject imageObject =
                                            attachmentObject.getJSONObject(Utils.KEY_NEWS_IMAGE);
                                    JSONObject fullImageObject =
                                            imageObject.getJSONObject(Utils.KEY_NEWS_FULL_THUMB);
                                    mImageFull = fullImageObject.getString(Utils.KEY_NEWS_URL);

                                } else {
                                    // If not available set default value
                                    mImageFull = Utils.VALUE_IMAGE_DEFAULT;
                                }

                                // Check whether this article has category
                                if(categoriesArray.length() != 0){
                                    // If available get category name
                                    categoriesObject = categoriesArray.getJSONObject(0);
                                    mCategoryName = categoriesObject.getString(Utils.KEY_TITLE);
                                }else{
                                    // If not available set "uncategorized"
                                    mCategoryName = getString(R.string.uncategorized);

                                }

                                // Display scrollview and toolbar, and
                                // hide retry layout and circular progress bar
                                haveResultView();

                                // After get all data required, set it the views
                                mTxtTitle.setText(mTitle);
                                mTxtCategoryName.setText(mCategoryName);

                                String authorAndDate = getString(R.string.by) + " " + mAuthor + " " +
                                        getString(R.string.published_at) + " " + mDate;
                                //        i commented this out becasue i dont want publishers name
//                                mTxtAuthorAndDate.setText(authorAndDate);
//
//                                mWebContent.loadData("<body style='margin:0;padding:0;'>"+
//                                                mContent + "</body>",
//                                        "text/html; charset=utf-8", "UTF-8");


                                mWebContent.loadData("<body style='margin:0;padding:0;'>"+
                                                mContent + "</body>",
                                        "text/html; charset=utf-8", "UTF-8");
                                mWebContent.getSettings().setJavaScriptEnabled(true);


//                                *******************************************************************************************************************

                                //Testers webcontent settings
                                mWebContent.getSettings().setAppCacheEnabled(true);
                                mWebContent.getSettings().setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
                                mWebContent.getSettings().setAllowFileAccess(true);// new addition
                                mWebContent.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
                                mWebContent.getSettings().setDomStorageEnabled(true);
                                mWebContent.getSettings().setDatabaseEnabled(true);

                                //		this next line is done to disable long clicks on webview
                                mWebContent.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View v) {
                                        return true;
                                    }
                                });
                                mWebContent.setLongClickable(false);
//                                *******************************************************************************************************************


                                // check whether mImageFull is default value
                                if(!mImageFull.equals(Utils.VALUE_IMAGE_DEFAULT)){
                                    // If mImageFull is not default value display image
                                    // with ImageLoader object
                                    // and this is where to change the background color oc the articles page
                                    mImageLoader.get(mImageFull,
                                            ImageLoader.getImageListener
                                                    (mImgImage, R.color.accent_color,
                                                            R.color.accent_color));

//                                    when the image on the detail page is clicked it shows in a fullscreen form with this method below
                                    mAnchor.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            mFullImage.setVisibility(View.VISIBLE);

                                            mImageLoader.get(mImageFull,
                                                    ImageLoader.getImageListener
                                                            (mFullImage, R.color.accent_color,
                                                                    R.color.accent_color));
                                            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

                                        }
                                    });
//                                    when the fullscreen image is clicked it disappears
                                    mFullImage.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
//                                            uncheck below to return to diappear on click
//                                            mFullImage.setVisibility(View.GONE);
//                                         getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                                        }
                                    });
                                } else {
                                    // If mImageFull is default value set mImgImage
                                    // background color with primary_color
                                    mImgImage.setBackgroundColor(ContextCompat.getColor(
                                            getApplicationContext(), R.color.slide_color));
                                }

                            }
                        } catch (JSONException e) {
                            Log.d(Utils.TAG_FORMATRIX + TAG,
                                    "onResponse JSON Parsing error: " + e.getMessage());
                        }
                    }
                },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(Utils.TAG_FORMATRIX + TAG,
                                "onResponse Response.ErrorListener()= " + error.toString());
                        // If response error, display retry layout
                        // and hide other views
                        noResultView();
                    }
                }
        );

        // Set request timeout
        request.setRetryPolicy(new DefaultRetryPolicy(Utils.ARG_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        MySingleton.getInstance(this).getRequestQueue().add(request);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu;
        // this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_detail, menu);

        // Add comment item
//        mToolbar.getMenu().findItem(R.id.menuComment)
//                .setIcon(new IconicsDrawable(getApplicationContext())
//                        .icon(GoogleMaterial.Icon.gmd_forum)
//                        .color(Color.WHITE)
//                        .actionBar());
        // Add share item
        mToolbar.getMenu().findItem(R.id.menuShare)
                .setIcon(new IconicsDrawable(getApplicationContext())
                        .icon(GoogleMaterial.Icon.gmd_share)
                        .color(Color.WHITE)
                        .actionBar());
        return super.onCreateOptionsMenu(menu);
    }

    // Method to handle physical back button with animation
    @Override
    public void onBackPressed() {
        if (mFullImage.getVisibility() == View.VISIBLE){
            mFullImage.setVisibility(View.GONE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }else {
            super.onBackPressed();
            overridePendingTransition(R.anim.open_main, R.anim.close_next);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fabBrowser:
                // Open article in browser
//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.setData(Uri.parse(mUrl));
//                startActivity(i);
//                overridePendingTransition(R.anim.open_up, R.anim.close_down);
                Intent iComment = new Intent(getApplicationContext(), ActivityComments.class);
                iComment.putExtra(Utils.EXTRA_ARTICLE_ID, mArticleId);
                startActivity(iComment);
                overridePendingTransition(R.anim.open_next, R.anim.close_main);
                break;
            case R.id.btnRetry:
             if (isNetworkAvailable(getBaseContext())){
                 retryView();
                // Re-retreive data from server
                getArticleDetail(mArticleId);

                }
                else {
                 int duration =10000;// amount of time the snackbar should stay visible
                 Snackbar.make(view, "Connect to the Internet", duration)
                         .setAction("Activate", new View.OnClickListener() {
                             @Override
                             public void onClick(View v) {
                                 Intent intent = new Intent();
                                 intent.setAction(Settings.ACTION_SETTINGS);
                                 startActivity(intent);
                                 overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                             }
                         }).show();

        }
         
                break;
        }
    }

    // Method to display retry layout if can not connect to the server
    private void noResultView() {
        mLytRetry.setVisibility(View.VISIBLE);
        mGradientHeader.setVisibility(View.GONE);
        mScrollView.setVisibility(View.GONE);
        mToolbar.setVisibility(View.GONE);
        mPrgLoading.setVisibility(View.GONE);
    }

    // Method to display result view if data is available
    private void haveResultView() {
        mLytRetry.setVisibility(View.GONE);
        mGradientHeader.setVisibility(View.VISIBLE);
        mScrollView.setVisibility(View.VISIBLE);
        mToolbar.setVisibility(View.VISIBLE);
        mPrgLoading.setVisibility(View.GONE);
    }

    // Method to display circular progress bar during loading data
    private void retryView() {
        mLytRetry.setVisibility(View.GONE);
        mScrollView.setVisibility(View.GONE);
        mToolbar.setVisibility(View.GONE);
        mGradientHeader.setVisibility(View.GONE);
        mPrgLoading.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        onScrollChanged(mScrollView.getCurrentScrollY(), false, false);
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        int baseColor = ContextCompat.getColor(getApplicationContext(), R.color.primary_color);
        float alpha = 1 - (float) Math.max(0, mParallaxImageHeight - scrollY) / mParallaxImageHeight;
        // Change toolbar background color alpha base on scroll
        mToolbar.setBackgroundColor(ScrollUtils.getColorWithAlpha(alpha, baseColor));
        mAnchor.setBackgroundColor(ScrollUtils.getColorWithAlpha(alpha, baseColor));
        ViewHelper.setTranslationY(mImgImage, scrollY / 2);

        // Set article title to toolbar if toolbar background color become visible
        if(alpha == 1){
            mToolbar.setTitle(mTitle);
        }else{
            mToolbar.setTitle("");
        }

        // Translate FAB
        int maxFabTranslationY = mParallaxImageHeight - mFabBrowser.getHeight() / 2;
        float fabTranslationY = ScrollUtils.getFloat(
                -scrollY + mParallaxImageHeight - mFabBrowser.getHeight() / 2,
                mActionBarSize - mFabBrowser.getHeight() / 2,
                maxFabTranslationY);

        // Show/hide FAB
        if (fabTranslationY < mFlexibleSpaceShowFabOffset) {
            hideFab();
        } else {
            showFab();
        }

    }

    // Method to show fab
    private void showFab() {
        if (!mFabIsShown) {
            ViewPropertyAnimator.animate(mFabBrowser).cancel();
            ViewPropertyAnimator.animate(mFabBrowser).translationY(0).setDuration(200).start();
            mFabIsShown = true;
        }
    }

    // Method to hide fab
    private void hideFab() {
        if (mFabIsShown) {
            ViewPropertyAnimator.animate(mFabBrowser).cancel();
            ViewPropertyAnimator.animate(mFabBrowser).translationY(500).setDuration(200).start();
            mFabIsShown = false;
        }
    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

    }

    // Method to get parent activity
    @Override
    public Intent getSupportParentActivityIntent() {
        Intent parentIntent= getIntent();
        // Get the parent class name
        String className = parentIntent.getStringExtra(Utils.EXTRA_ACTIVITY);

        Intent newIntent = null;
        try {
            // Open parent activity.
            newIntent = new Intent(this,Class.forName(
                    getApplicationContext().getPackageName()+"."+className));

            overridePendingTransition(R.anim.open_main, R.anim.close_next);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return newIntent;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdView != null) {
            mAdView.destroy();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mAdView != null) {
            mAdView.resume();
        }

    }


    /**
     * Making notification bar transparent
     */
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
//            window.setStatusBarColor(getResources().getColor(R.color.primary_dark));
            window.setStatusBarColor(getResources().getColor(R.color.md_black_1000));
            if (Build.VERSION.SDK_INT >= 21) {
//                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            }
        }
    }


}
