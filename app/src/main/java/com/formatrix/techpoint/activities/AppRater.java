package com.formatrix.techpoint.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.Toast;

/**
 * Created by mystified official on 3/17/2016.
 */
public class AppRater extends ActivityHome {
    public static String getAppPackageName() {
        return APP_PACKAGE_NAME;
    }

    final String appName = getPackageName();//your application package name i.e play store application url


    private final static String APP_TITLE = "APP_ID";
    private final static String APP_PACKAGE_NAME = "com.formatrix.techpoint";

    private final static int DAYS_UNTIL_PROMPT = 2;
    private final static int LAUNCH_UNTIL_PROMPT = 5;

    public static void app_launched(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("rate_app", 0);
        if (prefs.getBoolean("dontshowagain", false)){
            return;
        }
        SharedPreferences.Editor editor = prefs.edit();
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        Long date_firstLaunch = prefs.getLong("date_first_launch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_first_launch", date_firstLaunch);
        }

        if (launch_count >= LAUNCH_UNTIL_PROMPT){
            if (System.currentTimeMillis() >= date_firstLaunch + (DAYS_UNTIL_PROMPT * 24 *60 * 60 * 1000)) {
                showRateDialog(context, editor);
            }
        }
//        editor.commit();
        editor.apply();
    }

    public static void showRateDialog(final Context context, final SharedPreferences.Editor editor){

        Dialog dialog = new Dialog(context);
        AlertDialog.Builder builder = new AlertDialog.Builder(context, 2);
//        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context, 2);
        String message = "If you enjoy using "
                + "this app"
                + ", Please take a moment to rate and write us a review to help us improve."
                + "Thank you for your support";

        builder.setMessage(message)
                .setTitle("Rate Us")
//                .setIcon(R.mipmap.ic_launcher)
                .setCancelable(true)
                .setPositiveButton("Rate Now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editor.putBoolean("dontshowagain", true);
                        editor.commit();

                        //if your app hasnt been uploaded you'll get an exception
                        try {
                            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PACKAGE_NAME)));


                        }catch (ActivityNotFoundException e){
                            Toast.makeText(context, "OMG!!! Something went wrong!!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNeutralButton("Later", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        Toast.makeText(context, "Please don't forget to rate us!", Toast.LENGTH_SHORT).show();
                    }
                });
//                .setNegativeButton("No, Thanks", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        if (editor != null) {
//                            editor.putBoolean("dontshowagain", true);
//                            editor.commit();
//                        }
//
//                        dialog.dismiss();
//                    }
//                });
        dialog = builder.create();
        dialog.show();
    }
}
