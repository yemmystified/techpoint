package com.formatrix.techpoint.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.formatrix.techpoint.BuildConfig;
import com.formatrix.techpoint.R;
import com.formatrix.techpoint.utils.Utils;
import com.lb.material_preferences_library.PreferenceActivity;
import com.lb.material_preferences_library.custom_preferences.Preference;

import java.util.TimeZone;

/**
 * Design and developed by formatrix.com
 *
 * ActivityAbout is created to display app information such as app name,
 * version, and developer name. Created using PreferenceActivity.
 */
public class ActivityAbout extends PreferenceActivity
        implements Preference.OnPreferenceClickListener {

    private String model = Build.MODEL + "(" + Build.MANUFACTURER + ")";
    private String os = Build.VERSION.RELEASE;
    private String versionCode = BuildConfig.VERSION_NAME;
    private int api = Build.VERSION.SDK_INT;
    private TimeZone tz = TimeZone.getDefault();
    private String gmt = TimeZone.getTimeZone(tz.getID()).getDisplayName(false, TimeZone.LONG);
    private String gmt1 = TimeZone.getTimeZone(tz.getID()).getDisplayName(false, TimeZone.SHORT);


    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        // Set preference theme, you can configure the color theme
        // via res/values/styles.xml
        setTheme(R.style.AppTheme_Light);
        super.onCreate(savedInstanceState);
        Utils.saveIntPreferences(getApplicationContext(), Utils.ARG_DRAWER_PREFERENCE,
                Utils.ARG_PREFERENCES_DRAWER, 10);

        // Connect preference objects with preference key in xml file
        Preference prefShareKey      = (Preference) findPreference(getString(R.string.pref_share_key));
        Preference prefRateReviewKey = (Preference) findPreference(getString(R.string.pref_rate_review_key));
        Preference prefFeedBackKey      = (Preference) findPreference(getString(R.string.pref_feedback_key));
        Preference prefCopyRightKey      = (Preference) findPreference(getString(R.string.pref_copyright_key));
        Preference prefVersion= (Preference) findPreference(getString(R.string.pref_version));
        Preference prefDeveloper= (Preference) findPreference(getString(R.string.pref_developer));

        // Set preference click listener to the preference objects
        prefShareKey.setOnPreferenceClickListener(this);
        prefRateReviewKey.setOnPreferenceClickListener(this);
        prefFeedBackKey.setOnPreferenceClickListener(this);
        prefCopyRightKey.setOnPreferenceClickListener(this);
        prefVersion.setOnPreferenceClickListener(this);
        prefDeveloper.setOnPreferenceClickListener(this);
//        prefDeveloper.setIconCompat(R.mipmap.ic_launcher);
//        prefCopyRightKey.setIconCompat(R.mipmap.ic_launcher);
//        prefRateReviewKey.setIconCompat(R.mipmap.ic_launcher);
//        prefFeedBackKey.setIconCompat(R.mipmap.ic_launcher);
//        prefVersion.setIconCompat(R.mipmap.ic_launcher);
//        prefShareKey.setIconCompat(R.mipmap.ic_launcher);
    }

    @Override
    protected int getPreferencesXmlId()
    {
        // Connect preference activity with preference xml
        return R.xml.pref_about;
    }

    @Override
    public boolean onPreferenceClick(android.preference.Preference preference) {
        if(preference.getKey().equals(getString(R.string.pref_share_key))) {
            // Share Google Play url via other apps such as message, email, facebook, etc.
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT,
                    getString(R.string.subject));
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.message) +
                    " " + getString(R.string.google_play_url));
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_to)));
        }else if(preference.getKey().equals(getString(R.string.pref_rate_review_key))) {
            // Open App Google Play page so that user can rate and review the app.
            Intent rateReviewIntent = new Intent(Intent.ACTION_VIEW);
            rateReviewIntent.setData(Uri.parse(
                    getString(R.string.google_play_url)));
            startActivity(rateReviewIntent);
        }else if(preference.getKey().equals(getString(R.string.pref_copyright_key))) {
            // Dialog box to show company brand.
            final AlertDialog.Builder builder = new AlertDialog.Builder(this, 4);
            builder.setTitle("About");
            builder.setIcon(R.mipmap.ic_launcher);
            builder.setMessage("Version:" + versionCode + "\n\n" + "Thank you for downloading this app.\n"
                    + " \nWe hope you enjoy it.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    builder.setCancelable(true);
                }
            }).setNeutralButton("Rate Us", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW,
//                                Uri.parse("market://details?id=formatrix digital")));
                        Uri.parse("market://search?q=Formatrix Digital")));
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    } catch (ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=com.formatrix.techpoint")));
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                    }

                }
            });
            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(true);
            alertDialog.show();
        }else if (preference.getKey().equals(getString(R.string.pref_feedback_key))) {
//            send feedback to developer mail

            Intent messageIntent = new Intent(getApplicationContext(), MsgActivity.class);
            startActivity(messageIntent);

//            startEmailActivity("feedback@formatrix.ng", "Muyiwa Afolabi App Feedback", " 'Place your feedback below this text':\n\n\n\n\n\n\n" + "==========INFOMATRIX=========== "
//						+ "\n" + "Muyiwa Afolabi Android version: " + versionCode + "\n" + "DEVICE MODEL: " + model + "\n" + "ANDROID OS: "
//						+ os + "\n" + "API level: " + api + "\n" + "TimeZone: " + gmt + "\n" + "TZ: " + gmt1 + "\n" + "Location: " + tz.getID() + "\n" + "===============================");
//				Toast.makeText(this, "Please do not edit pre-written data", Toast.LENGTH_LONG).show();
//

        }
        else if(preference.getKey().equals(getString(R.string.pref_version))) {
            Toast.makeText(ActivityAbout.this, "This is version: " + versionCode, Toast.LENGTH_SHORT).show();


        }

        else if(preference.getKey().equals(getString(R.string.pref_developer))) {


//            Toast.makeText(ActivityAbout.this, "Code is Art, Art is Life!", Toast.LENGTH_LONG).show();
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
//                        Uri.parse("market://details?id=formatrix digital")));
                        Uri.parse("market://search?q=Formatrix Digital")));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=com.formatrix.techpoint")));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            }

        }
        return true;
    }

    // Method to handle physical back button with animation
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.open_main, R.anim.close_next);
    }


    private void startEmailActivity(String email, String subject, String text) {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("mailto:");
            builder.append(email);

            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(builder.toString()));
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        } catch (ActivityNotFoundException e) {
            // can't start activity
            Toast.makeText(this, "An error occurred", Toast.LENGTH_LONG).show();
        }
    }
}
