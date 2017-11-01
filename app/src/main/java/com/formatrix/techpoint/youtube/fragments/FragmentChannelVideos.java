package com.formatrix.techpoint.youtube.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.formatrix.techpoint.R;
import com.formatrix.techpoint.youtube.adapters.AdapterList;
import com.formatrix.techpoint.youtube.utils.MySingleton;
import com.formatrix.techpoint.youtube.utils.UtilsVideo;
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

/**
 * Design and developed by formatrix.com
 *
 * FragmentChannelVideos is created to display videos data of a youtube channel or playlist.
 * Created using Fragment.
 */
public class FragmentChannelVideos extends Fragment implements View.OnClickListener {

    // Create tag for log
    private static final String TAG = FragmentChannelVideos.class.getSimpleName();
    // Create view objects
    private TextView mLblNoResult;
    private LinearLayout mLytRetry;
    private CircleProgressBar mPrgLoading;
    private UltimateRecyclerView mUltimateRecyclerView;
    private AdView mAdView;

    // Create variable to handle admob visibility
    private boolean mIsAdmobVisible;

    // Create variable to store channel id and video type
    private int mVideoType;
    private String mChannelId;

    // Create listener
    private OnVideoSelectedListener mCallback;

    // Create AdapterList object
    private AdapterList mAdapterList = null;

    // Create arraylist variable to store video data before get video duration
    private ArrayList<HashMap<String, String>> mTempVideoData = new ArrayList<>();
    // Create arraylist variable to store final data
    private ArrayList<HashMap<String, String>> mVideoData     = new ArrayList<>();

    private String mNextPageToken = "";
    private String mVideoIds = "";
    private String mDuration = "00:00";

    // Paramater (true = data still exist in server, false = data already loaded all)
    private boolean mIsStillLoading = true;

    // Paramater (true = is first time, false = not first time)
    private boolean mIsAppFirstLaunched = true;

    // Create variable to check the first video
    private boolean mIsFirstVideo = true;

    // Interface, activity that use FragmentChannelVIdeo must implement onVideoSelected method
    public interface OnVideoSelectedListener {
        void onVideoSelected(String ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View view = inflater.inflate(R.layout.fragment_video_list, container, false);
        setHasOptionsMenu(true);
        // Get Bundle data
        Bundle bundle = this.getArguments();

        // Get data from ActivityVideo
        mVideoType = Integer.parseInt(bundle.getString(UtilsVideo.TAG_VIDEO_TYPE));
        mChannelId = bundle.getString(UtilsVideo.TAG_CHANNEL_ID);

        // Connect view objects and view ids from xml
        mUltimateRecyclerView       = (UltimateRecyclerView) view.findViewById(R.id.ultimate_recycler_view);
        mLblNoResult                = (TextView) view.findViewById(R.id.lblNoResult);
        mLytRetry                   = (LinearLayout) view.findViewById(R.id.lytRetry);
        mPrgLoading                 = (CircleProgressBar) view.findViewById(R.id.prgLoading);
        AppCompatButton btnRetry    = (AppCompatButton) view.findViewById(R.id.raisedRetry);
        mAdView                     = (AdView) view.findViewById(R.id.adView);

        // Set click listener to btnRetry
        btnRetry.setOnClickListener(this);
        // Set circular bar color and visibility
        mPrgLoading.setColorSchemeResources(R.color.accent_color);
        mPrgLoading.setVisibility(View.VISIBLE);

        // Set value to true in default
        mIsAppFirstLaunched = true;
        mIsFirstVideo = true;

        // Get admob visibility value
        mIsAdmobVisible = UtilsVideo.admobVisibility(mAdView, UtilsVideo.IS_ADMOB_VISIBLE);

        // Load ad in background using asynctask class
        new SyncShowAd(mAdView).execute();

        // Set arraylist variable of videoData
        mVideoData = new ArrayList<>();

        // Set mAdapterList to UltimateRecyclerView object
        mAdapterList = new AdapterList(getActivity(), mVideoData);
        mUltimateRecyclerView.setAdapter(mAdapterList);
        mUltimateRecyclerView.setHasFixedSize(false);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mUltimateRecyclerView.setLayoutManager(linearLayoutManager);
//        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
//        mUltimateRecyclerView.setLayoutManager(gridLayoutManager);
        mUltimateRecyclerView.enableLoadmore();

        // Set layout for custom circular bar when load more
        mAdapterList.setCustomLoadMoreView(LayoutInflater.from(getActivity())
                .inflate(R.layout.loadmore_progressbar, null));

        // Listener for handle load more
        mUltimateRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
                // if true it means that it still has data in server
                if (mIsStillLoading) {
                    mIsStillLoading = false;
                    // Set layout for custom circular bar when load more.
                    // mAdapter is set again because when load data is response error
                    // setCustomLoadMoreView is null to clear view loading
                    mAdapterList.setCustomLoadMoreView(LayoutInflater.from(getActivity())
                            .inflate(R.layout.loadmore_progressbar, null));

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            getVideoData();

                        }
                    }, 1000);
                } else {
                    disableLoadmore();
                }

            }
        });

        // Condition when item in list is click
        ItemTouchListenerAdapter itemTouchListenerAdapter =
                new ItemTouchListenerAdapter(mUltimateRecyclerView.mRecyclerView,
            new ItemTouchListenerAdapter.RecyclerViewOnItemClickListener() {
                @Override
                public void onItemClick(RecyclerView parent, View clickedView, int position) {
                    // To handle when position  = locationsData.size means loading view si click
                    if (position < mVideoData.size()) {
                        // Pass data to onVideoSelected in ActivityVideo
                        mCallback.onVideoSelected(mVideoData.get(position).get(UtilsVideo.KEY_VIDEO_ID));
                    }
                }

                @Override
                public void onItemLongClick(RecyclerView recyclerView, View view, int i) {
                }
            });

        // Enable touch listener
        mUltimateRecyclerView.mRecyclerView.addOnItemTouchListener(itemTouchListenerAdapter);

        // Get data from server in first time when fragment create
        getVideoData();

        return view;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            mCallback = (OnVideoSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnVideoSelectedListener");
        }
    }

    // Asynctask class to load admob in background
    public class SyncShowAd extends AsyncTask<Void, Void, Void> {

        final AdView AD;
        AdRequest adRequest;

        public SyncShowAd(AdView AD){
            this.AD = AD;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Check ad visibility. If visible, create adRequest
            if(mIsAdmobVisible) {
                // Create an ad request
                if (UtilsVideo.IS_ADMOB_IN_DEBUG) {
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
            if(mIsAdmobVisible) {
                // Start loading the ad
                AD.loadAd(adRequest);
            }
        }
    }

    // Method to get videos data of a youtube channel of playlist
    private void getVideoData() {

        // Set mVideoIds empty, we need video ID to get video duration
        mVideoIds = "";
        // Create array variable to store first video id of the videos channel
        final String[] videoId = new String[1];

        // Create variable to store youtube api url
        String url;
        // Check whether it is channel or playlist (in this case is "Acoustic Covers")
        if(mVideoType == 2) {
            // Youtube API url for playlist
            url = UtilsVideo.API_YOUTUBE + UtilsVideo.FUNCTION_PLAYLIST_ITEMS_YOUTUBE +
                    UtilsVideo.PARAM_PART_YOUTUBE + "snippet,id&" +
                    UtilsVideo.PARAM_FIELD_PLAYLIST_YOUTUBE + "&" +
                    UtilsVideo.PARAM_KEY_YOUTUBE + getResources().getString(R.string.youtube_apikey) + "&" +
                    UtilsVideo.PARAM_PLAYLIST_ID_YOUTUBE + mChannelId + "&" +
                    UtilsVideo.PARAM_PAGE_TOKEN_YOUTUBE + mNextPageToken + "&" +
                    UtilsVideo.PARAM_MAX_RESULT_YOUTUBE + UtilsVideo.PARAM_RESULT_PER_PAGE;
        }else {
            // Youtube API url for channel
            url = UtilsVideo.API_YOUTUBE + UtilsVideo.FUNCTION_SEARCH_YOUTUBE +
                    UtilsVideo.PARAM_PART_YOUTUBE + "snippet,id&" + UtilsVideo.PARAM_ORDER_YOUTUBE + "&" +
                    UtilsVideo.PARAM_TYPE_YOUTUBE + "&" +
                    UtilsVideo.PARAM_FIELD_SEARCH_YOUTUBE + "&" +
                    UtilsVideo.PARAM_KEY_YOUTUBE + getResources().getString(R.string.youtube_apikey) + "&" +
                    UtilsVideo.PARAM_CHANNEL_ID_YOUTUBE + mChannelId + "&" +
                    UtilsVideo.PARAM_PAGE_TOKEN_YOUTUBE + mNextPageToken + "&" +
                    UtilsVideo.PARAM_MAX_RESULT_YOUTUBE + UtilsVideo.PARAM_RESULT_PER_PAGE;
        }

        JsonObjectRequest request = new JsonObjectRequest(url, null,
            new Response.Listener<JSONObject>() {
                JSONArray dataItemArray;
                JSONObject itemIdObject, itemSnippetObject, itemSnippetThumbnailsObject,
                        itemSnippetResourceIdObject;
                @Override
                public void onResponse(JSONObject response) {
                    // To make sure Activity is still in the foreground
                    Activity activity = getActivity();
                    if(activity != null && isAdded()){
                        try {
                            // Get all Items json Array from server
                            dataItemArray = response.getJSONArray(UtilsVideo.ARRAY_ITEMS);

                            if (dataItemArray.length() > 0) {
                                haveResultView();
                                for (int i = 0; i < dataItemArray.length(); i++) {
                                    HashMap<String, String> dataMap = new HashMap<>();

                                    // Detail Array per Item
                                    JSONObject itemsObject = dataItemArray.getJSONObject(i);
                                    // Array snippet to get title and thumbnails
                                    itemSnippetObject = itemsObject.
                                            getJSONObject(UtilsVideo.OBJECT_ITEMS_SNIPPET);

                                    if(mVideoType == 2){
                                        // Get video ID in playlist
                                        itemSnippetResourceIdObject = itemSnippetObject.
                                                getJSONObject(UtilsVideo.OBJECT_ITEMS_SNIPPET_RESOURCEID);
                                        dataMap.put(UtilsVideo.KEY_VIDEO_ID,
                                                itemSnippetResourceIdObject.
                                                        getString(UtilsVideo.KEY_VIDEO_ID));
                                        videoId[0] = itemSnippetResourceIdObject.
                                                getString(UtilsVideo.KEY_VIDEO_ID);

                                        // Concat all video IDs and use it as parameter to
                                        // get all video durations.
                                        mVideoIds = mVideoIds + itemSnippetResourceIdObject.
                                                getString(UtilsVideo.KEY_VIDEO_ID) + ",";
                                    }else {
                                        // Get video ID in channel
                                        itemIdObject = itemsObject.
                                                getJSONObject(UtilsVideo.OBJECT_ITEMS_ID);
                                        dataMap.put(UtilsVideo.KEY_VIDEO_ID,
                                                itemIdObject.getString(UtilsVideo.KEY_VIDEO_ID));
                                        videoId[0] = itemIdObject.getString(UtilsVideo.KEY_VIDEO_ID);

                                        // Concat all video IDs and use it as parameter to
                                        // get all video durations.
                                        mVideoIds = mVideoIds + itemIdObject.
                                                getString(UtilsVideo.KEY_VIDEO_ID) + ",";
                                    }

                                    // When fragment first created display first video to
                                    // video player.
                                    if(mIsFirstVideo && i == 0) {
                                        mIsFirstVideo = false;
                                        mCallback.onVideoSelected(videoId[0]);
                                    }

                                    // Get video title
                                    dataMap.put(UtilsVideo.KEY_TITLE,
                                            itemSnippetObject.getString(UtilsVideo.KEY_TITLE));

                                    // Convert ISO 8601 date to string
                                    String formattedPublishedDate = UtilsVideo.formatPublishedDate(
                                            getActivity(),
                                            itemSnippetObject.getString(UtilsVideo.KEY_PUBLISHEDAT));

                                    // Get published date
                                    dataMap.put(UtilsVideo.KEY_PUBLISHEDAT, formattedPublishedDate);

                                    // Get video thumbnail
                                    itemSnippetThumbnailsObject = itemSnippetObject.
                                            getJSONObject(UtilsVideo.OBJECT_ITEMS_SNIPPET_THUMBNAILS);
                                    itemSnippetThumbnailsObject = itemSnippetThumbnailsObject.
                                            getJSONObject
                                                    (UtilsVideo.OBJECT_ITEMS_SNIPPET_THUMBNAILS_MEDIUM);
                                    dataMap.put(UtilsVideo.KEY_URL_THUMBNAILS,
                                            itemSnippetThumbnailsObject.getString
                                                    (UtilsVideo.KEY_URL_THUMBNAILS));

                                    // Store video data temporarily to get video duration
                                    mTempVideoData.add(dataMap);
                                }

                                // After finish getting video IDs, titles, and thumbnails
                                // now get video duration.
                                getDuration();

                                // Condition if dataItemArray == result perpage it means maybe
                                // server still have data
                                if (dataItemArray.length() == UtilsVideo.PARAM_RESULT_PER_PAGE) {
                                    // To get next page data youtube have parameter Next Page Token
                                    mNextPageToken = response.getString(UtilsVideo.ARRAY_PAGE_TOKEN);

                                    // No data anymore in this URL
                                } else {
                                    // Clear mNextPageToken
                                    mNextPageToken = "";
                                    disableLoadmore();
                                }

                                // If success get data, it means next it is not first time again.
                                mIsAppFirstLaunched = false;

                                // Data from server already load all or no data in server
                            } else {
                                if (mIsAppFirstLaunched &&
                                        mAdapterList.getAdapterItemCount() <= 0) {
                                    noResultView();
                                }
                                disableLoadmore();
                            }

                        } catch (JSONException e) {
                            Log.d(UtilsVideo.TAG_formatrix + TAG, "JSON Parsing error: " +
                                    e.getMessage());
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
                        Log.d(UtilsVideo.TAG_formatrix + TAG, "on Error Response: " + error.getMessage());
                        // "try-catch" To handle when still in process and then application closed
                        try {
                            String msgSnackBar;
                            if (error instanceof NoConnectionError) {
                                msgSnackBar = getResources().getString(R.string.no_internet_connection);
                            } else {
                                msgSnackBar = getResources().getString(R.string.response_error);
                            }

                            // To handle when no data in mAdapter and then get error because no
                            // connection or problem in server.
                            if (mVideoData.size() == 0) {
                                retryView();

                                // Condition when loadmore, it have data when loadmore then
                                // get error because no connection.
                            } else {
                                mAdapterList.setCustomLoadMoreView(null);
                                mAdapterList.notifyDataSetChanged();
                            }

                            //UtilsVideo.showSnackBar(getActivity(), msgSnackBar);
                            mPrgLoading.setVisibility(View.GONE);

                        } catch (Exception e) {
                            Log.d(UtilsVideo.TAG_formatrix + TAG, "failed catch volley " + e.toString());
                            mPrgLoading.setVisibility(View.GONE);
                        }
                    }
                }
            }
        );
        request.setRetryPolicy(new DefaultRetryPolicy(UtilsVideo.ARG_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(getActivity()).getRequestQueue().add(request);

    }

    // Method to get duration of the video
    private void getDuration() {
        // Youtube API url to get video duration
        String url = UtilsVideo.API_YOUTUBE+ UtilsVideo.FUNCTION_VIDEO_YOUTUBE+
                UtilsVideo.PARAM_PART_YOUTUBE+"contentDetails&"+
                UtilsVideo.PARAM_FIELD_VIDEO_YOUTUBE+"&"+
                UtilsVideo.PARAM_KEY_YOUTUBE+getResources().getString(R.string.youtube_apikey)+"&"+
                UtilsVideo.PARAM_VIDEO_ID_YOUTUBE+mVideoIds;

        JsonObjectRequest request = new JsonObjectRequest(url, null,
            new Response.Listener<JSONObject>() {
                JSONArray dataItemArrays;
                JSONObject itemContentObject;
                @Override
                public void onResponse(JSONObject response) {
                    // To make sure Activity is still in the foreground
                    Activity activity = getActivity();
                    if(activity != null && isAdded()){
                        try {
                            haveResultView();
                            dataItemArrays = response.getJSONArray(UtilsVideo.ARRAY_ITEMS);
                            if (dataItemArrays.length() > 0 && !mTempVideoData.isEmpty()) {
                                for (int i = 0; i < dataItemArrays.length(); i++) {
                                    HashMap<String, String> dataMap = new HashMap<>();

                                    // Detail Array per Item
                                    JSONObject itemsObjects = dataItemArrays.getJSONObject(i);

                                    // Item to get duration
                                    itemContentObject = itemsObjects.
                                            getJSONObject(UtilsVideo.OBJECT_ITEMS_CONTENT_DETAIL);
                                    mDuration         = itemContentObject.
                                            getString(UtilsVideo.KEY_DURATION);

                                    // Convert ISO 8601 time to string
                                    String mDurationInTimeFormat = UtilsVideo.
                                            getTimeFromString(mDuration);

                                    // Store titles, video IDs, and thumbnails from mTempVideoData
                                    // to dataMap.
                                    dataMap.put(UtilsVideo.KEY_DURATION, mDurationInTimeFormat);
                                    dataMap.put(UtilsVideo.KEY_URL_THUMBNAILS,
                                            mTempVideoData.get(i).get(UtilsVideo.KEY_URL_THUMBNAILS));
                                    dataMap.put(UtilsVideo.KEY_TITLE,
                                            mTempVideoData.get(i).get(UtilsVideo.KEY_TITLE));
                                    dataMap.put(UtilsVideo.KEY_VIDEO_ID,
                                            mTempVideoData.get(i).get(UtilsVideo.KEY_VIDEO_ID));
                                    dataMap.put(UtilsVideo.KEY_PUBLISHEDAT,
                                            mTempVideoData.get(i).get(UtilsVideo.KEY_PUBLISHEDAT));

                                    // And store dataMap to videoData
                                    mVideoData.add(dataMap);

                                    // Insert 1 by 1 to mAdapter
                                    mAdapterList.notifyItemInserted(mVideoData.size());

                                }
                                mIsStillLoading = true;

                                // Clear mTempVideoData after it done to insert all in videoData
                                mTempVideoData.clear();
                                mTempVideoData = new ArrayList<>();

                            // Data from server already load all or no data in server
                            }else {
                                if (mIsAppFirstLaunched && mAdapterList.getAdapterItemCount() <= 0)
                                {
                                    noResultView();
                                }
                                disableLoadmore();
                            }

                        } catch (JSONException e) {
                            Log.d(UtilsVideo.TAG_formatrix + TAG,
                                    "JSON Parsing error: " + e.getMessage());
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
                        Log.d(UtilsVideo.TAG_formatrix + TAG, "on Error Response: " + error.getMessage());
                        // "try-catch" To handle when still in process and then application closed
                        try {
                            String msgSnackBar;
                            if (error instanceof NoConnectionError) {
                                msgSnackBar = getResources().getString(R.string.no_internet_connection);
                            } else {
                                msgSnackBar = getResources().getString(R.string.response_error);
                            }

                            // To handle when no data in mAdapter and then get error because no
                            // connection or problem in server.
                            if (mVideoData.size() == 0) {
                                retryView();
                                // Condition when loadmore it has data,
                                // when loadmore then get error because no connection.
                            }

                            //UtilsVideo.showSnackBar(getActivity(), msgSnackBar);
                            mPrgLoading.setVisibility(View.GONE);

                        } catch (Exception e) {
                            Log.d(UtilsVideo.TAG_formatrix + TAG, "failed catch volley " + e.toString());
                            mPrgLoading.setVisibility(View.GONE);
                        }
                    }
                }
            }
        );
        request.setRetryPolicy(new DefaultRetryPolicy(UtilsVideo.ARG_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(getActivity()).getRequestQueue().add(request);
    }

    // Method to hide other view and display retry layout
    private void retryView() {
        mLytRetry.setVisibility(View.VISIBLE);
        mUltimateRecyclerView.setVisibility(View.GONE);
        mLblNoResult.setVisibility(View.GONE);
    }

    // Method to display Recyclerview and hide other view
    private void haveResultView() {
        mLytRetry.setVisibility(View.GONE);
        mUltimateRecyclerView.setVisibility(View.VISIBLE);
        mLblNoResult.setVisibility(View.GONE);
    }

    // Method to display no result view and hide other view
    private void noResultView() {
        mLytRetry.setVisibility(View.GONE);
        mUltimateRecyclerView.setVisibility(View.GONE);
        mLblNoResult.setVisibility(View.VISIBLE);

    }

    // Method to disable loadmore
    private void disableLoadmore() {
        mIsStillLoading = false;
        if (mUltimateRecyclerView.isLoadMoreEnabled()) {
            mUltimateRecyclerView.disableLoadmore();
        }
        mAdapterList.notifyDataSetChanged();
    }


    @Override
    public void onPause() {
        super.onPause();
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

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.raisedRetry:
                // Re-load video channel
                mPrgLoading.setVisibility(View.VISIBLE);
                haveResultView();
                getVideoData();
                break;
            default:
                break;
        }
    }
}
