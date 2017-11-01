package com.formatrix.techpoint.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.formatrix.techpoint.R;
import com.formatrix.techpoint.activities.ActivityDetail;
import com.formatrix.techpoint.adapters.AdapterArticles;
import com.formatrix.techpoint.libs.MySingleton;
import com.formatrix.techpoint.utils.Utils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.marshalchen.ultimaterecyclerview.ItemTouchListenerAdapter;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static com.formatrix.techpoint.utils.Utils.isNetworkAvailable;

/**
 * Design and developed by formatrix.com
 *
 * FragmentArticles is created to display recent articles.
 * Created using Fragment.
 */
public class FragmentArticles extends Fragment implements
        OnClickListener{

    // Tag for log
    private static final String TAG = FragmentArticles.class.getSimpleName();


    // Create view objects
    private TextView mLblNoResult;
    private LinearLayout mLytRetry;
    private CircleProgressBar mPrgLoading;
    private UltimateRecyclerView mUltimateRecyclerView;
    private ViewFlipper mFlipper;
    private AdView mAdView;

    // Create adapter object
    private AdapterArticles mAdapter;

    // Paramater (true = data still exist in server, false = data already loaded all)
    private boolean mIsStillLoding = true;
    // Paramater (true = is first time, false = not first time)
    private boolean mIsAppFirstLaunched = true;
    // initially offset will be 0, later will be updated while parsing the json
    private int mStartIndex = 0;

    // Create arraylist to store location data
    private ArrayList<HashMap<String, String>> mNewsData = new ArrayList<>();
    // Create variable to handle admob visibility
    private boolean mIsAdmobVisible;

    // Create variable to store current page value
    private Integer mCurrentPage = 1;
    // Create ImageLoader object to load image in background
    private ImageLoader mImageLoader;
    // Create variables to store activity
    String mActivity;

    // Create variables to store data
    private String mArticleId, mTitle, mDate, mCategoryName, mAuthor, mImageFull, mImageThumbnail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_articles, container, false);

        mCurrentPage = 1;

        // Set ImageLoader object
        mImageLoader = MySingleton.getInstance(getActivity()).getImageLoader();

        Utils.saveIntPreferences(getActivity(),
                Utils.ARG_DRAWER_PREFERENCE,
                Utils.ARG_PREFERENCES_DRAWER, 1);

        // Get Bundle data
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            // Get parent Activity who call this fragment
            mActivity = bundle.getString(Utils.EXTRA_ACTIVITY);
        }

        // Connect view objects and view ids from xml
        mFlipper 	                = (ViewFlipper) view.findViewById(R.id.flipper);
        mUltimateRecyclerView       = (UltimateRecyclerView) view.findViewById(R.id.ultimate_recycler_view);
        mLblNoResult                = (TextView) view.findViewById(R.id.lblNoResult);

        TextView mlblAlert = (TextView) view.findViewById(R.id.lblAlert);
//        Typeface custom_font = Typeface.createFromAsset(getResources().getAssets(),"BebasNeue.otf");
//        mlblAlert.setTypeface(custom_font);
        mLytRetry                   = (LinearLayout) view.findViewById(R.id.lytRetry);
        mPrgLoading                 = (CircleProgressBar) view.findViewById(R.id.prgLoading);
        AppCompatButton btnRetry    = (AppCompatButton) view.findViewById(R.id.btnRetry);
        mAdView                     = (AdView) view.findViewById(R.id.adView);

        // Set click listener to the button
        btnRetry.setOnClickListener(this);
        // Set circular progress bar color
        mPrgLoading.setColorSchemeResources(R.color.accent_color);
        // Set circular progress bar visibility to visible first
        mPrgLoading.setVisibility(View.VISIBLE);

        // Get admob visibility value
        mIsAdmobVisible = Utils.admobVisibility(mAdView, Utils.IS_ADMOB_VISIBLE);
        // Load ad in background using asynctask class
        new SyncShowAd(mAdView).execute();

        // Set arraylist
        mNewsData = new ArrayList<>();

        // Set mAdapter
        mAdapter = new AdapterArticles(null, null, getActivity(), mNewsData);

        // Set flipper configuration
        mFlipper.setInAnimation (AnimationUtils.loadAnimation(getActivity(),
                R.anim.fade_in));
        mFlipper.setOutAnimation(AnimationUtils.loadAnimation(getActivity(),
                R.anim.fade_out));
        mFlipper.setFlipInterval(5000);
        mFlipper.startFlipping();

        // Set recyclerview configuration
        mUltimateRecyclerView.setAdapter(mAdapter);
        mUltimateRecyclerView.setHasFixedSize(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mUltimateRecyclerView.setLayoutManager(linearLayoutManager);
//        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
//        mUltimateRecyclerView.setLayoutManager(gridLayoutManager);
        mUltimateRecyclerView.enableLoadmore();

        // Set layout for custom loading when load more
        mAdapter.setCustomLoadMoreView(LayoutInflater.from(getActivity())
                .inflate(R.layout.loadmore_progressbar, null));

        // Listener for handle load more
        mUltimateRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
                // if True is means still have data in server
                if (mIsStillLoding) {
                    // Set layout for custom the loading when load more. mAdapter is set
                    // again because when load data is response error setCustomLoadMoreView
                    // is null to clear view loading
                    mAdapter.setCustomLoadMoreView(LayoutInflater.from(getActivity())
                            .inflate(R.layout.loadmore_progressbar, null));

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            getArticlesData();
                        }
                    }, 1000);
                } else {
                    disableLoadmore();
                }

            }
        });

        // Condition when item in list is click
        ItemTouchListenerAdapter itemTouchListenerAdapter = new ItemTouchListenerAdapter(mUltimateRecyclerView.mRecyclerView,
                new ItemTouchListenerAdapter.RecyclerViewOnItemClickListener() {
                    @Override
                    public void onItemClick(RecyclerView parent, View clickedView, int position) {
                        // To handle when position  = newsData.size means loading view si click
                        if (position < mNewsData.size()) {
                            Intent i = new Intent(getActivity(), ActivityDetail.class);
                            i.putExtra(Utils.EXTRA_ACTIVITY, mActivity);
                            i.putExtra(Utils.EXTRA_ARTICLE_ID, mNewsData.get(position).get(Utils.KEY_ID));
                            startActivity(i);
                            getActivity().overridePendingTransition(R.anim.open_next, R.anim.close_main);
                        }
                    }

                    @Override
                    public void onItemLongClick(RecyclerView recyclerView, View view, int i) {
                    }
                });

        // Enable touch listener
        mUltimateRecyclerView.mRecyclerView.addOnItemTouchListener(itemTouchListenerAdapter);

        // Set StartIndex from zero and first time is true
        mStartIndex = 0;
        mIsAppFirstLaunched = true;
        // Get data from server in first time when activity create
        getArticlesData();

        return view;
    }

    // Asynctask class to load admob in background
    public class SyncShowAd extends AsyncTask<Void, Void, Void> {

        final AdView ad;
        AdRequest adRequest;

        public SyncShowAd(AdView ad) {
            this.ad = ad;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Check AD visibility. If visible, create adRequest
            if (mIsAdmobVisible) {
                // Create an AD request
                if (Utils.IS_ADMOB_IN_DEBUG) {
                    adRequest = new AdRequest.Builder().
                            addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
                } else {
                    adRequest = new AdRequest.Builder().build();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // Check ad visibility. If visible, display ad banner and interstitial
            if (mIsAdmobVisible) {
                // Start loading the ad
                ad.loadAd(adRequest);

            }

        }
    }

    // Method to get articles or category data from server
    private void getArticlesData() {
        // Recent post API url
        String url = Utils.WORDPRESS_URL + Utils.API_BASE + Utils.VALUE_GET_POSTS + "?" +
                Utils.PARAM_COUNT + Utils.VALUE_PER_PAGE + "&" +
                Utils.PARAM_STATUS + "&" +
                Utils.PARAM_OFFSET + mStartIndex;

        JsonObjectRequest request = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    JSONArray postsArray, attachmentsArray, categoriesArray;
                    JSONObject postsObject, attachmentsObject,
                            attachmentsImagesObject, attachmentsImagesThumbnailObject,
                            attachmentsImagesFullObject, categoriesObject, authorObject;
                    Integer mTotalPages;

                    @Override
                    public void onResponse(JSONObject response) {
                        // To make sure Activity is still in the foreground
                        Activity activity = getActivity();
                        if(activity != null && isAdded()) {
                            try {
                                // Get all json from server
                                postsArray = response.getJSONArray(Utils.ARRAY_POSTS);
                                if (postsArray.length() > 0) {
                                    haveResultView();
                                    for (int i = 0; i < postsArray.length(); i++) {
                                        HashMap<String, String> dataMap = new HashMap<>();

                                        // Get object and array
                                        postsObject = postsArray.getJSONObject(i);
                                        authorObject = postsObject.getJSONObject(Utils.KEY_AUTHOR);
                                        attachmentsArray = postsObject.getJSONArray(Utils.ARRAY_ATTACHMENTS);
                                        categoriesArray = postsObject.getJSONArray(Utils.ARRAY_CATEGORIES);

                                        // Store data to variables
                                        mArticleId = postsObject.getString(Utils.KEY_ID);
                                        mTitle = postsObject.getString(Utils.KEY_TITLE);
                                        mAuthor = authorObject.getString(Utils.KEY_NAME);
                                        mDate = Utils.formattedDate(postsObject.getString(Utils.KEY_DATE));

                                        // Check whether attachment is available
                                        if (attachmentsArray.length() != 0) {
                                            // If available store value to variable
                                            attachmentsObject = attachmentsArray.getJSONObject(0);
                                            attachmentsImagesObject = attachmentsObject.
                                                    getJSONObject(Utils.OBJECT_IMAGES);
                                            attachmentsImagesFullObject = attachmentsImagesObject.
                                                    getJSONObject(Utils.OBJECT_IMAGES_FULL);

//                                        commented the below lines out to make the app work
//                                            attachmentsImagesThumbnailObject = attachmentsImagesObject.
//                                                    getJSONObject(Utils.OBJECT_IMAGES_THUMBNAIL);
//                                            mImageThumbnail = attachmentsImagesThumbnailObject.
//                                                    getString(Utils.KEY_URL);
                                            mImageFull = attachmentsImagesFullObject.
                                                    getString(Utils.KEY_URL);
                                        } else {
                                            // If not available set default value
                                            mImageThumbnail = Utils.VALUE_IMAGE_DEFAULT;
                                            mImageFull = Utils.VALUE_IMAGE_DEFAULT;

                                        }

                                        // Check whether category is available
                                        if (categoriesArray.length() != 0) {
                                            // if available store value to variable
                                            categoriesObject = categoriesArray.getJSONObject(0);
                                            mCategoryName = categoriesObject.getString(Utils.KEY_TITLE);
                                        } else {
                                            // If not available set "uncategorized" as value
                                            mCategoryName = getString(R.string.uncategorized);
                                        }

                                        if ((i < Utils.VALUE_SLIDESHOW) && mIsAppFirstLaunched) {
                                            createSlideshow(mArticleId, mDate, mImageFull,
                                                    mTitle, mCategoryName, mAuthor);
                                        } else {
                                            // Add all data to dataMap
                                            dataMap.put(Utils.KEY_ID, mArticleId);
                                            dataMap.put(Utils.KEY_TITLE, mTitle);
                                            dataMap.put(Utils.KEY_DATE, mDate);
                                            dataMap.put(Utils.KEY_CATEGORY, mCategoryName);
                                            dataMap.put(Utils.KEY_AUTHOR, mAuthor);
                                            dataMap.put(Utils.KEY_IMAGE_THUMBNAIL_URL, mImageThumbnail);
                                            dataMap.put(Utils.KEY_IMAGE_FULL_URL, mImageFull);
                                            mNewsData.add(dataMap);
                                            // Insert 1 by 1 to mAdapter
                                            mAdapter.notifyItemInserted(mNewsData.size());
                                        }


                                    }
                                    mTotalPages = response.getInt(Utils.KEY_PAGES);
                                    // Condition when paginationFLICKRObject is null its mean nomore data in server.
                                    if (mCurrentPage < mTotalPages) {
                                        mCurrentPage += 1;
                                        mStartIndex += Utils.VALUE_PER_PAGE;
                                    } else {
                                        disableLoadmore();
                                    }

                                    // If success get data it means next its not first time again
                                    mIsAppFirstLaunched = false;

                                    // Possibility still exist in server
                                    mIsStillLoding = true;

                                    // Data from server already load all or no data in server
                                } else {
                                    if (mIsAppFirstLaunched && mAdapter.getAdapterItemCount() <= 0) {
                                        noResultView();
                                    }
                                    disableLoadmore();
                                }

                            } catch (JSONException e) {
                                Log.d(Utils.TAG_FORMATRIX + TAG, "JSON Parsing error: " + e.getMessage());
                                mPrgLoading.setVisibility(View.GONE);
                            }
                            mPrgLoading.setVisibility(View.GONE);
                        }
                    }
                },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // To make sure Activity is still in the foreground
                        Activity activity = getActivity();
                        if(activity != null && isAdded()){
                            Log.d(Utils.TAG_FORMATRIX + TAG, "on Error Response: " + error.getMessage());
                            // "try-catch" To handle when still in process and then application closed
                            try {
                                if (error instanceof NoConnectionError) {
                                    retryView(getString(R.string.no_internet_connection));
                                } else {
                                    retryView(getString(R.string.response_error));
                                }

                                // To handle when no data in mAdapter and then get error because
                                // no connection or problem in server
                                if (mNewsData.size() == 0) {
                                    retryView(getString(R.string.no_result));

                                    // Conditon when loadmore, it have data when loadmore then get
                                    // error because no connection
                                } else {
                                    mAdapter.setCustomLoadMoreView(null);
                                    mAdapter.notifyDataSetChanged();
                                }

                                mPrgLoading.setVisibility(View.GONE);

                            } catch (Exception e) {
                                Log.d(Utils.TAG_FORMATRIX + TAG, "failed catch volley " + e.toString());
                                mPrgLoading.setVisibility(View.GONE);
                            }
                        }

                    }
                }
        );

/////////////////////***************************************************

        request.setRetryPolicy(new DefaultRetryPolicy(Utils.ARG_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(getActivity()).getRequestQueue().add(request);

    }

    // Method to create slideshow
    private void createSlideshow(String id, String date, String image,
                                 String title, String categoryName, String author){

        // Get layout
        View view = getActivity().getLayoutInflater().inflate(R.layout.layout_slideshow, null);

        // Create view objects
        ImageView imgImage = (ImageView) view.findViewById(R.id.imgImage);
        TextView txtCategoryName = (TextView) view.findViewById(R.id.txtCategoryName);
        TextView txtTitle = (TextView) view.findViewById(R.id.txtTitle);
        TextView txtAuthorAndDate = (TextView) view.findViewById(R.id.txtAuthorAndDate);

        imgImage.setId(Integer.parseInt(id));
        imgImage.setOnClickListener(this);
        // Check whether image is available or not
        if (!image.equals(Utils.VALUE_IMAGE_DEFAULT)){
            // If available load image with imageloader
            mImageLoader.get(image,
                    ImageLoader.getImageListener(imgImage, R.mipmap.empty,
                            R.mipmap.empty));
        } else {
            // If not available set image backgroud color with primary color
            // this is where you change the slideshow image color
//            imgImage.setBackgroundColor(ContextCompat.getColor(getActivity(),
//                    R.color.accent_color));
//            imgImage.setBackgroundColor(ContextCompat.getColor(getActivity(),
//                    R.color.accent_color));
            imgImage.setBackgroundResource(R.drawable.splash_image_2);
        }

        String authorAndDate = getString(R.string.by) + " " + author + " " +
                getString(R.string.published_at) + " " + date;
        // Set data to the view
        txtCategoryName.setText(categoryName.toUpperCase());
        txtTitle.setText(title);
//        i commented this out becasue i dont want publishers name
//        txtAuthorAndDate.setText(authorAndDate);

        // Add layout to flipper
        mFlipper.addView(view);

    }

    // Method to display retry layout if can not connect to the server
    private void retryView(String message) {
        mLytRetry.setVisibility(View.VISIBLE);
        mUltimateRecyclerView.setVisibility(View.GONE);
        mLblNoResult.setVisibility(View.GONE);
        mLblNoResult.setText(message);
    }

    // Method to display result view if data is available
    private void haveResultView() {
        mLytRetry.setVisibility(View.GONE);
        mUltimateRecyclerView.setVisibility(View.VISIBLE);
        mLblNoResult.setVisibility(View.GONE);
    }

    // Method to display no result view if data is not available
    private void noResultView() {
        mLytRetry.setVisibility(View.GONE);
        mUltimateRecyclerView.setVisibility(View.GONE);
        mLblNoResult.setVisibility(View.VISIBLE);

    }

    // Method to disable load more
    private void disableLoadmore() {
        mIsStillLoding = false;
        if (mUltimateRecyclerView.isLoadMoreEnabled()) {
            mUltimateRecyclerView.disableLoadmore();
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnRetry:

                if (isNetworkAvailable(getContext())){
                    // Re-retrive data from server
                    mPrgLoading.setVisibility(View.VISIBLE);
                    haveResultView();
                    getArticlesData();

                }
                else {
                    int duration =10000;// amount of time the snackbar should stay visible
                    Snackbar.make(view, "Connect to the Internet", duration)
                            .setAction("Activate", new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_SETTINGS);
                                    startActivity(intent);
//                                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                }
                            }).show();

                }
                break;
            default:
                String articleId = String.valueOf(view.getId());
                // Open ActivityDetail and pass article id to that activity
                Intent i = new Intent(getActivity(), ActivityDetail.class);
                i.putExtra(Utils.EXTRA_ACTIVITY, mActivity);
                i.putExtra(Utils.EXTRA_ARTICLE_ID, articleId);
                startActivity(i);
                getActivity().overridePendingTransition(R.anim.open_next, R.anim.close_main);
                break;
        }
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

        Utils.saveIntPreferences(getActivity(),
                Utils.ARG_DRAWER_PREFERENCE, Utils.ARG_PREFERENCES_DRAWER, 1);
    }

}


