package com.formatrix.techpoint.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.formatrix.techpoint.R;
import com.formatrix.techpoint.activities.ActivityDetail;
import com.formatrix.techpoint.adapters.AdapterComments;
import com.formatrix.techpoint.libs.MySingleton;
import com.formatrix.techpoint.utils.Utils;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.rengwuxian.materialedittext.MaterialEditText;

import net.i2p.android.ext.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Design and developed by formatrix.com
 *
 * FragmentArticles is created to display recent articles.
 * Created using Fragment.
 */
public class FragmentComments extends Fragment implements
        OnClickListener{

    // Tag for log
    private static final String TAG = FragmentComments.class.getSimpleName();

    // Create view objects
    private TextView mLblNoResult;
    private LinearLayout mLytRetry;
    private CircleProgressBar mPrgLoading;
    private UltimateRecyclerView mUltimateRecyclerView;
    private FloatingActionButton mFabAddComment;

    // Create adapter object
    private AdapterComments mAdapter;

    // Paramater (true = is first time, false = not first time)
    private boolean mIsAppFirstLaunched = true;

    // Create arraylist to store location data
    private ArrayList<HashMap<String, String>> mNewsData = new ArrayList<>();
    // Create variables to store activity and keyword
    String mActivity;

    // Create variables to store data
    private String mArticleId, mCommentId, mName, mDate, mContent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comments, container, false);

        Utils.saveIntPreferences(getActivity(),
                Utils.ARG_DRAWER_PREFERENCE,
                Utils.ARG_PREFERENCES_DRAWER, 2);

        // Get Bundle data
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            // Get parent Activity who call this fragment
            mActivity = bundle.getString(Utils.EXTRA_ACTIVITY);
            mArticleId = bundle.getString(Utils.EXTRA_ARTICLE_ID);
        }

        // Connect view objects and view ids from xml
        mUltimateRecyclerView       = (UltimateRecyclerView) view.findViewById(R.id.ultimate_recycler_view);
        mLblNoResult                = (TextView) view.findViewById(R.id.lblNoResult);
        mLytRetry                   = (LinearLayout) view.findViewById(R.id.lytRetry);
        mPrgLoading                 = (CircleProgressBar) view.findViewById(R.id.prgLoading);
        mFabAddComment                = (FloatingActionButton) view.findViewById(R.id.fabAddComment);
        AppCompatButton btnRetry    = (AppCompatButton) view.findViewById(R.id.btnRetry);

        // Set click listener to the button
        btnRetry.setOnClickListener(this);
        mFabAddComment.setOnClickListener(this);
        // Set circular progress bar color
        mPrgLoading.setColorSchemeResources(R.color.accent_color);
        // Set circular progress bar visibility to visible first
        mPrgLoading.setVisibility(View.VISIBLE);


        // Add material design icon to the fab button
        mFabAddComment.setIconDrawable(new IconicsDrawable(getActivity())
                .icon(GoogleMaterial.Icon.gmd_insert_comment)
                .color(Color.WHITE));

        // Set arraylist
        mNewsData = new ArrayList<>();

        // Set mAdapter
        mAdapter = new AdapterComments(getActivity(), mNewsData);

        // Set recyclerview configuration
        mUltimateRecyclerView.setAdapter(mAdapter);
        mUltimateRecyclerView.setHasFixedSize(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mUltimateRecyclerView.setLayoutManager(linearLayoutManager);
//        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
//        mUltimateRecyclerView.setLayoutManager(gridLayoutManager);

        mUltimateRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    ViewPropertyAnimator.animate(mFabAddComment).cancel();
                    ViewPropertyAnimator.animate(mFabAddComment).translationY(500).setDuration(200).start();
                } else {
                    ViewPropertyAnimator.animate(mFabAddComment).cancel();
                    ViewPropertyAnimator.animate(mFabAddComment).translationY(0).setDuration(200).start();
                }
            }
        });

        mIsAppFirstLaunched = true;
        // Get data from server in first time when activity create
        getCommentsData(mArticleId);

        return view;
    }

    // Method to get articles or category data from server
    private void getCommentsData(String articleId) {
        // Article API url
        String url = Utils.WORDPRESS_URL + Utils.API_BASE + Utils.VALUE_GET_POST + "?" +
                Utils.PARAM_POST_ID + articleId;

        JsonObjectRequest request = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    String status;
                    JSONArray commentsArray;
                    JSONObject postObject, commentsObject;

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            status = response.getString(Utils.KEY_STATUS);
                            if (status.equals("ok")) {
                                postObject      = response.getJSONObject(Utils.OBJECT_NEWS_POST);
                                commentsArray = postObject.getJSONArray(Utils.ARRAY_COMMENTS);

                                if (commentsArray.length() > 0) {
                                    haveResultView();
                                    for (int i = (commentsArray.length() - 1); i >= 0; i--) {
                                        HashMap<String, String> dataMap = new HashMap<>();

                                        commentsObject         = commentsArray.getJSONObject(i);
                                        // Store data to variables
                                        mCommentId          = commentsObject.getString(Utils.KEY_ID);
                                        mName              = commentsObject.getString(Utils.KEY_NAME);
                                        mContent	            = commentsObject.getString(Utils.KEY_CONTENT);
                                        mDate               = Utils.formattedDate(commentsObject.getString(Utils.KEY_DATE));

                                        // Add all data to dataMap
                                        dataMap.put(Utils.KEY_ID, mCommentId);
                                        dataMap.put(Utils.KEY_NAME, mName);
                                        dataMap.put(Utils.KEY_CONTENT, mContent);
                                        dataMap.put(Utils.KEY_DATE, mDate);
                                        mNewsData.add(dataMap);
                                        // Insert 1 by 1 to mAdapter
                                        mAdapter.notifyItemInserted(mNewsData.size());

                                        if(i > Utils.VALUE_NUMBER_OF_COMMENT){
                                            break;
                                        }
                                    }

                                }else{
                                    noResultView();
                                }

                                // If success get data it means next its not first time again
                                mIsAppFirstLaunched = false;

                                // Data from server already load all or no data in server
                            } else {
                                if (mIsAppFirstLaunched && mAdapter.getAdapterItemCount() <= 0) {
                                    noResultView();
                                }
                            }

                        } catch (JSONException e) {
                            Log.d(Utils.TAG_FORMATRIX + TAG, "JSON Parsing error: " + e.getMessage());
                            retryView();
                            mPrgLoading.setVisibility(View.GONE);
                        }
                        mPrgLoading.setVisibility(View.GONE);

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
                                    retryView();
                                } else {
                                    retryView();
                                }

                                // To handle when no data in mAdapter and then get error because
                                // no connection or problem in server
                                if (mNewsData.size() == 0) {
                                    retryView();

                                // Conditon when loadmore, it have data when loadmore then get
                                // error because no connection
                                } else {
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
        request.setRetryPolicy(new DefaultRetryPolicy(Utils.ARG_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(getActivity()).getRequestQueue().add(request);

    }


    // Method to display retry layout if can not connect to the server
    private void retryView() {
        mLytRetry.setVisibility(View.VISIBLE);
        mUltimateRecyclerView.setVisibility(View.GONE);
        mLblNoResult.setVisibility(View.GONE);
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

    // Method to show add comment form dialog
    private void showAddCommentDialog() {

        // Create view objects
        MaterialEditText edtName;
        MaterialEditText edtEmail;
        MaterialEditText edtComment;
        // Create 1 length array to store edittext value
        final String[] name = new String[1];
        final String[] email = new String[1];
        final String[] comment = new String[1];

        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.add_comment)
                .customView(R.layout.add_comment, true)
                .backgroundColorRes(R.color.material_background_color)
                .titleColorRes(R.color.primary_text)
                .widgetColorRes(R.color.divider_color)
                .buttonRippleColorRes(R.color.light_primary_color)
                .positiveText(R.string.send)
                .negativeText(android.R.string.cancel)
                .cancelable(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                        // If not all forms are filled, then display snack bar
                        if (name[0] == null || email[0] == null || comment[0] == null) {
                            Utils.showSnackBar(getActivity(), getString(R.string.all_forms_must_be_filled));
                        } else {
                            submitComment(mArticleId, name[0], email[0], comment[0]);
                        }
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {

                    }
                }).build();

        // Connect view objects with view ids in xml
        edtName = (MaterialEditText) dialog.getCustomView().findViewById(R.id.edtName);
        edtEmail = (MaterialEditText) dialog.getCustomView().findViewById(R.id.edtEmail);
        edtComment = (MaterialEditText) dialog.getCustomView().findViewById(R.id.edtComment);

        edtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().trim().length() > 0){
                    name[0] = charSequence.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        edtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().trim().length() > 0){
                    email[0] = charSequence.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        edtComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().trim().length() > 0){
                    comment[0] = charSequence.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        dialog.show();
    }

    // Method to submit comment to the API url
    private void submitComment(String articleId, String name, String email, String comment) {

        // Encode value of the variables first.
        try {
            name = URLEncoder.encode(name, "UTF-8");
            name = name.replace("+","%20");
            comment = URLEncoder.encode(comment, "UTF-8");
            comment = comment.replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // Submit comment API url
        String url = Utils.WORDPRESS_URL + Utils.API_BASE + Utils.VALUE_RESPOND +
                Utils.VALUE_SUBMIT_COMMENT + "?" +
                Utils.PARAM_POST_ID + articleId + "&" +
                Utils.PARAM_NAME + name + "&" +
                Utils.PARAM_EMAIL + email + "&" +
                Utils.PARAM_CONTENT + comment;

        JsonObjectRequest request = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    String status;
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(Utils.TAG_FORMATRIX + TAG, "onResponse " + response.toString());
                        try {
                            status = response.getString(Utils.KEY_STATUS);
                            if (status.equals("ok") || status .equals("pending")) {
                                // If finish submitting, display success message
                                Utils.showSnackBar(getActivity(), getString(R.string.add_comment_success));
                            }else{
                                // else display failed message
                                Utils.showSnackBar(getActivity(), getString(R.string.add_comment_failed));
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
                        // To make sure Activity is still in the foreground
                        Activity activity = getActivity();
                        if(activity != null && isAdded()){
                            Log.d(Utils.TAG_FORMATRIX + TAG, "on Error Response: " + error.getMessage());
                            // "try-catch" To handle when still in process and then application closed
                            try {
                                if (error instanceof NoConnectionError) {
                                    Utils.showSnackBar(getActivity(), getString(R.string.add_comment_failed));
                                } else {
                                    Utils.showSnackBar(getActivity(), getString(R.string.add_comment_failed));
                                }

                                // To handle when no data in mAdapter and then get error because
                                // no connection or problem in server
                                if (mNewsData.size() == 0) {
                                    Utils.showSnackBar(getActivity(), getString(R.string.add_comment_failed));

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
        request.setRetryPolicy(new DefaultRetryPolicy(Utils.ARG_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(getActivity()).getRequestQueue().add(request);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnRetry:
                // Re-retrive data from server
                mPrgLoading.setVisibility(View.VISIBLE);
                haveResultView();
                getCommentsData(mArticleId);
                break;
            case R.id.fabAddComment:
                showAddCommentDialog();
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

}

