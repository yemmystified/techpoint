package com.formatrix.techpoint.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.github.mrengineer13.snackbar.SnackBar;
import com.google.android.gms.ads.AdView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Design and developed by formatrix digital
 *
 * Utils is created to set application configuration, API url, ad visibility, etc.
 */
public class Utils {

    // Key for value that passed between activities / fragments
    public static final String EXTRA_ACTIVITY                       = "activity";
    public static final String EXTRA_ARTICLE_ID                     = "articleId";
    public static final String EXTRA_SOCIAL_URL                     = "socialUrl";
    public static final String EXTRA_TITLE                          = "title";
    public static final String EXTRA_KEYWORD                        = "keyword";
    public static final String EXTRA_CATEGORY_NAME                  = "categoryName";
    public static final String EXTRA_CATEGORY_ID   	                = "category_id";

    // Tag for Activity file name, use for parent activity handling
    public static final String TAG_ACTIVITY_HOME                    = "activities.ActivityHome";
    public static final String TAG_ACTIVITY_SEARCH                  = "activities.ActivitySearch";
    public static final String TAG_ACTIVITY_ARTICLES_BY_CATEGORY    = "activities.ActivityArticlesByCategory";
    public static final String TAG_ACTIVITY_COMMENTS                = "activities.ActivityComments";

    // Website URL with Wordpress installed. Change the url with yours.
    public static final String WORDPRESS_URL                        = "https://techpoint.ng/";
    // API base url, in default it is "api".
    public static final String API_BASE                             = "api/";

    // JSON API plugin url parameters
    public static final String PARAM_COUNT                          = "count=";
    public static final String PARAM_SEARCH                         = "search=";
    public static final String PARAM_STATUS                         = "status=published";
    public static final String PARAM_OFFSET                         = "offset=";
    public static final String PARAM_POST_TYPE                      = "post_type=";
    public static final String PARAM_CATEGORIES                     = "cat=";
    public static final String PARAM_POST_ID	                    = "post_id=";
    public static final String PARAM_PAGE                           = "page=";
    public static final String PARAM_NAME                           = "name=";
    public static final String PARAM_EMAIL                          = "email=";
    public static final String PARAM_CONTENT                        = "content=";

    // Key objects of JSON API plugin
    public static final String KEY_ID                               = "id";
    public static final String KEY_TITLE                            = "title";
    public static final String KEY_DATE                             = "date";
    public static final String KEY_URL                              = "url";
//    public static final String KEY_IMAGE_FULL_URL                   = "urlFull";
public static final String KEY_IMAGE_FULL_URL                   = "medium_large";//customized
    public static final String KEY_IMAGE_THUMBNAIL_URL              = "urlThumb";
    public static final String KEY_PAGES                            = "pages";
    public static final String KEY_STATUS                           = "status";
    public static final String KEY_AUTHOR		                    = "author";
    public static final String KEY_NAME	                            = "name";
    public static final String KEY_NEWS_IMAGE		                = "images";
//    public static final String KEY_NEWS_FULL_THUMB	                = "full";//normal
public static final String KEY_NEWS_FULL_THUMB	                = "medium_large";//customized
    public static final String KEY_NEWS_URL		                    = "url";
    public static final String KEY_CONTENT		                    = "content";
    public static final String KEY_CATEGORY		                    = "category";

    // JSON API plugin value parameters
    public static final int VALUE_PER_PAGE   	                    = 10;
    public static final int VALUE_SLIDESHOW                         = 5;
    public static final int VALUE_NUMBER_OF_COMMENT                 = 5;
//    public static final String VALUE_IMAGE_DEFAULT                  = "blank";
    public static final String VALUE_POST   	                    = "post";
    public static final String VALUE_GET_POSTS   	                = "get_posts/?";
    public static final String VALUE_GET_RECENT_POSTS   	        = "get_recent_posts/?";
    public static final String VALUE_GET_SEARCH_RESULTS             = "get_search_results?";
    public static final String VALUE_GET_POST                       = "get_post/";
    public static final String VALUE_JSON_CATEGORY                  = "get_category_index/";
    public static final String VALUE_RESPOND                        = "respond/";
    public static final String VALUE_SUBMIT_COMMENT                 = "submit_comment/";

    // Array objects of JSON API plugin
    public static final String ARRAY_POSTS                          = "posts";
    public static final String ARRAY_ATTACHMENTS                    = "attachments";
    public static final String ARRAY_CATEGORIES                     = "categories";
    public static final String ARRAY_COMMENTS                       = "comments";

    // Objects of JSON API plugin
    public static final String OBJECT_IMAGES                        = "images";
//    public static final String OBJECT_IMAGES_FULL                   = "full";//normal
    public static final String OBJECT_IMAGES_FULL                   = "medium_large";//customized
//public static final String OBJECT_IMAGES_FULL                   = "url";
    public static final String OBJECT_IMAGES_THUMBNAIL              = "thumbnail";
    public static final String OBJECT_NEWS_POST		                = "post";
    public static final String VALUE_IMAGE_DEFAULT                  = "null";
    
    // Timeout time in millisecond, default is 2500.
    public static final Integer ARG_TIMEOUT_MS                      = 30000;

    // Debugging tag for the application
    public static final String TAG_FORMATRIX                         = "Formatrix:";

    // Preference keys
    public static final String ARG_DRAWER_PREFERENCE  = "drawerPreference";
    public static final String ARG_ADMOB_PREFERENCE   = "admobPreference";
    public static final String ARG_PREFERENCES_DRAWER = "viewDrawerPreference";

    // Argument for ad interstitial, do NOT change this
    public static final String ARG_TRIGGER = "trigger";
    // For every detail you want to display interstitial ad.
    // 3 means interstitial ad will display after user open detail page three times.
    public static final int ARG_TRIGGER_VALUE = 1;
    // Admob visibility parameter. Set true to show false and false to hide.
    public static final boolean IS_ADMOB_VISIBLE = false;
    // Set value to true if you are still in development process    ,
    // and false if you are ready to publish the app.
    public static final boolean IS_ADMOB_IN_DEBUG = false;

    // Method to check admob visibility
    public static boolean admobVisibility(AdView ad, boolean isInDebugMode){
        if(isInDebugMode) {
            ad.setVisibility(View.VISIBLE);
            return true;
        }else {
            ad.setVisibility(View.GONE);
            return false;
        }
    }
    // Method to save integer value to SharedPreferences
    public static void saveIntPreferences(Context ctx, String key, String param, int value){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(key, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(param, value);
        editor.apply();
    }

    // Method to load int value from SharedPreferences
    public static int loadIntPreferences(Context ctx, String key, String param){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(key, Context.MODE_PRIVATE);
        if(key.equals(ARG_ADMOB_PREFERENCE)){
            return sharedPreferences.getInt(param, 1);
        }else {
            return sharedPreferences.getInt(param, 1000);
        }
    }

    // Method to format date
    public static String formattedDate(String date){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date newDate = null;
        try {
            newDate = format.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        format = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
        return format.format(newDate);
    }

    // Method to show snackbar
    public static void showSnackBar(Activity activity, String message){
        new SnackBar.Builder(activity)
                .withMessage(message)
                .show();
    }


    // list of file extensions for download,
    // if webview URL ends with this extension, that file will be downloaded via download manager,
    // leave this array empty if you do not want to use download manager
    public static final String[] DOWNLOAD_FILE_TYPES = {
            ".zip", ".rar", ".pdf", ".doc", ".xls",
            ".mp3", ".wma", ".ogg", ".m4a", ".wav",
            ".avi", ".mov", ".mp4", ".mpg", ".3gp"
    };


    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }



    public static Point getWindowSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }



//method for onbackpressed in case override ,method doesn't work but still needs to be tweaked

//    private void setUpBackPressed() {
//        getView().setFocusableInTouchMode(true);
//        getView().requestFocus();
//        getView().setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (event.getAction() == KeyEvent.ACTION_DOWN) {
//                    if (keyCode == KeyEvent.KEYCODE_BACK) {
//                        if (fabMenu.isOpened()) {
//                            fabMenu.toggle(true);
//                            return true;
//                        } else {
//                            return false;
//                        }
//                    }
//                }
//                return false;
//            }
//        });
//    }





}
