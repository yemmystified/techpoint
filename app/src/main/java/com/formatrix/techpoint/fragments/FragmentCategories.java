package com.formatrix.techpoint.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.formatrix.techpoint.R;
import com.formatrix.techpoint.activities.ActivityArticlesByCategory;
import com.formatrix.techpoint.adapters.AdapterCategories;
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
 * FragmentCategories is created to display category item.
 * Created using Fragment.
 */
public class FragmentCategories extends Fragment implements OnClickListener {

    // Tag for log
    private final static String TAG = FragmentCategories.class.getSimpleName();

    // Create view objects
    private TextView mLblNoResult;
    private LinearLayout mLytRetry;
    private CircleProgressBar mPrgLoading;
    private UltimateRecyclerView mUltimateRecyclerView;
    private AdView mAdView;

    // Create AdapterCategories object
    private AdapterCategories mAdapter;

    // Create variable to handle admob visibility status
    private boolean mIsAdmobVisible;

    // Create arraylist variable to store category data
    private final ArrayList<HashMap<String, String>> CATEGORY_DATA = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories, container, false);

        Utils.saveIntPreferences(getActivity(), Utils.ARG_DRAWER_PREFERENCE,
                Utils.ARG_PREFERENCES_DRAWER, 2);

        // Connect view objects and view ids from xml
        mUltimateRecyclerView = (UltimateRecyclerView) view.findViewById(R.id.ultimate_recycler_view);
        mLblNoResult          = (TextView) view.findViewById(R.id.lblNoResult);
        mLytRetry             = (LinearLayout) view.findViewById(R.id.lytRetry);
        mPrgLoading           = (CircleProgressBar) view.findViewById(R.id.prgLoading);
        Button btnRetry       = (Button) view.findViewById(R.id.btnRetry);
        mAdView               = (AdView) view.findViewById(R.id.adView);

        // Set click listener to the button
        btnRetry.setOnClickListener(this);
        // Set circular progress bar color
        mPrgLoading.setColorSchemeResources(R.color.accent_color);
        // Set circular progress bar visibility to visible by default
        mPrgLoading.setVisibility(View.VISIBLE);

        // Get admob visibility value
        mIsAdmobVisible = Utils.admobVisibility(mAdView, Utils.IS_ADMOB_VISIBLE);
        // Load ad in background using asynctask class
        new SyncShowAd(mAdView).execute();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mUltimateRecyclerView.setLayoutManager(linearLayoutManager);
//        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
//        mUltimateRecyclerView.setLayoutManager(gridLayoutManager );


        // Condition when item in list is click
        ItemTouchListenerAdapter itemTouchListenerAdapter =
                new ItemTouchListenerAdapter(mUltimateRecyclerView.mRecyclerView,
                new ItemTouchListenerAdapter.RecyclerViewOnItemClickListener() {
                    @Override
                    public void onItemClick(RecyclerView parent, View clickedView, int position) {
                        // To handle when position  = dataLocations.size means loading view is click
                        if(position < CATEGORY_DATA.size()){
                            Intent i = new Intent(getActivity(), ActivityArticlesByCategory.class);
                            i.putExtra(Utils.EXTRA_CATEGORY_ID, CATEGORY_DATA.get(position).
                                    get(Utils.KEY_ID));
                            i.putExtra(Utils.EXTRA_CATEGORY_NAME, CATEGORY_DATA.get(position).
                                    get(Utils.KEY_TITLE));
                            startActivity(i);
                            getActivity().overridePendingTransition(R.anim.open_next, R.anim.close_main);
                        }
                    }

                    @Override
                    public void onItemLongClick(RecyclerView recyclerView, View view, int i) {}
                });

        // Enable touch listener
        mUltimateRecyclerView.mRecyclerView.addOnItemTouchListener(itemTouchListenerAdapter);

        getCategoryData();

        return view;
    }

    // Asynctask class to load admob in background
    public class SyncShowAd extends AsyncTask<Void, Void, Void> {

        final AdView ad;
        AdRequest adRequest;

        public SyncShowAd(AdView ad){
            this.ad = ad;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Check AD visibility. If visible, create adRequest
            if(mIsAdmobVisible) {
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
            // Check AD visibility. If visible, display AD banner and interstitial
            if(mIsAdmobVisible) {
                // Start loading the AD
                ad.loadAd(adRequest);
            }
        }
    }

    // Method to get category data
    private void getCategoryData(){
        // Categories API url
        String url = Utils.WORDPRESS_URL + Utils.API_BASE + Utils.VALUE_JSON_CATEGORY;
        JsonObjectRequest request = new JsonObjectRequest(url, null,
            new Response.Listener<JSONObject>() {
                JSONArray categoryArray;
                JSONObject categoryObject;

                @Override
                public void onResponse(JSONObject response) {
                    Activity activity = getActivity();
                    if(activity != null && isAdded()){
                        haveResultView();
                        try {
                            categoryArray = response.getJSONArray(Utils.ARRAY_CATEGORIES);
                            if (categoryArray.length() > 0) {
                                // Store data to variable array
                                for (int i = 0; i < categoryArray.length(); i++) {
                                    HashMap<String, String> dataMap = new HashMap<>();
                                    categoryObject = categoryArray.getJSONObject(i);

                                    // Get id, title, and date
                                    dataMap.put(Utils.KEY_ID, categoryObject.
                                            getString(Utils.KEY_ID));
                                    dataMap.put(Utils.KEY_TITLE, categoryObject.
                                            getString(Utils.KEY_TITLE));

                                    CATEGORY_DATA.add(dataMap);
                                }
                                // Set mAdapter
                                mAdapter = new AdapterCategories(CATEGORY_DATA);
                                mUltimateRecyclerView.setAdapter(mAdapter);
                                mUltimateRecyclerView.setHasFixedSize(false);

                            } else {
                                // if there is no data in server
                                noResultView();
                            }

                        } catch (JSONException e) {
                            mPrgLoading.setVisibility(View.GONE);
                            Log.d(Utils.TAG_FORMATRIX + TAG,
                                    "JSON Parsing error: " + e.getMessage());
                        }

                        mPrgLoading.setVisibility(View.GONE);
                    }

                }
            },

            new Response.ErrorListener() {
                // "try-catch" To handle when still in process and then application closed
                @Override
                public void onErrorResponse(VolleyError error) {
                Activity activity = getActivity();
                if(activity != null && isAdded()){
                    Log.d(Utils.TAG_FORMATRIX + TAG,
                            "Response.ErrorListener()= " + error.toString());
                    try{
                        mPrgLoading.setVisibility(View.GONE);
                        if(error instanceof NoConnectionError) {
                            retryView(getString(R.string.no_internet_connection));
                        } else {
                            retryView(getString(R.string.response_error));
                        }

                    } catch (Exception e){
                        Log.d(Utils.TAG_FORMATRIX + TAG, "failed catch volley " + e.toString());
                    }
                }

                }
            }
        );
        request.setRetryPolicy(new DefaultRetryPolicy(Utils.ARG_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        MySingleton.getInstance(getActivity()).getRequestQueue().add(request);
    }

    // Method to display retry layout if cannot connect to the server
    private void retryView(String message){
        mLytRetry.setVisibility(View.VISIBLE);
        mUltimateRecyclerView.setVisibility(View.GONE);
        mLblNoResult.setVisibility(View.GONE);
        mLblNoResult.setText(message);
    }

    // Method to display result view if data available
    private void haveResultView(){
        mLytRetry.setVisibility(View.GONE);
        mUltimateRecyclerView.setVisibility(View.VISIBLE);
        mLblNoResult.setVisibility(View.GONE);
        CATEGORY_DATA.clear();
    }

    // Method to display no result view if data not available in the server
    private void noResultView(){
        mLytRetry.setVisibility(View.GONE);
        mUltimateRecyclerView.setVisibility(View.GONE);
        mLblNoResult.setVisibility(View.VISIBLE);

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.btnRetry:
                if (isNetworkAvailable(getContext())){
                    // Re-retrieve data from server
                    mPrgLoading.setVisibility(View.VISIBLE);
                    haveResultView();
                    getCategoryData();

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

        Utils.saveIntPreferences(getActivity(), Utils.ARG_DRAWER_PREFERENCE,
                Utils.ARG_PREFERENCES_DRAWER, 2);
    }

}

