package com.formatrix.techpoint.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.formatrix.techpoint.BuildConfig;
import com.formatrix.techpoint.R;

import java.util.TimeZone;

public class MsgActivity extends AppCompatActivity {



    private String model = Build.MODEL + "(" + Build.MANUFACTURER + ")";
    private String os = Build.VERSION.RELEASE;
    private String versionCode = BuildConfig.VERSION_NAME;
    private int api = Build.VERSION.SDK_INT;
    String hardware = Build.HARDWARE;
    String device = Build.DEVICE;
    private TimeZone tz = TimeZone.getDefault();
    private String gmt = TimeZone.getTimeZone(tz.getID()).getDisplayName(false, TimeZone.LONG);
    private String gmt1 = TimeZone.getTimeZone(tz.getID()).getDisplayName(false, TimeZone.SHORT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                final EditText name= (EditText) findViewById(R.id.namebox);
                String nam= name.getText().toString();
                final EditText message= (EditText) findViewById(R.id.messagebox);
                String messag= message.getText().toString();
                final EditText phone= (EditText) findViewById(R.id.phonebox);
                String phon= phone.getText().toString();
                final EditText company= (EditText) findViewById(R.id.companyname);
                String compan= company.getText().toString();


                if (nam.trim().length() == 0 || messag.trim().length() == 0 )
                {
//                    This is done to make the phone vibrate for some milliseconds, and you need to also add it to the manifest...
                    Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(300);
                    Toast.makeText(MsgActivity.this, "All  *  are required", Toast.LENGTH_SHORT).show();
                }else {


                    startEmailActivity("developer@formatrix.ng", "Enquiries / FeedBack", "===============================" + "\n" + "Name: " + nam + "\n" + "==============================="
                            + "\n" + "Company Name: " + compan.trim() + "\n" + "===============================" + "\n" + "Phone No: " + phon + "\n"
                            + "===============================" + "\n" + "Message: " + messag.trim() + "\n" + "===============================" + "\n"
                            +  "===============================" + "\n"+ "==========INFOMATRIX========== "
                            + "\n" + "Android version: " + versionCode + "\n" + "DEVICE MODEL: " + model + "\n" + "ANDROID OS: "
                            + os + "\n" + "API level: " + api + "\n" + "TimeZone: " + gmt + "\n" + "TZ: " + gmt1 + "\n" + "Location: " + tz.getID() + "\n" + "==============================="+ "\n" + "===============================" );
                    Toast.makeText(MsgActivity.this, "Your Feedback has been generated.", Toast.LENGTH_LONG).show();
//                    Toast.makeText(MsgActivity.this, "Click Send", Toast.LENGTH_LONG).show();
                }



            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    //this is done for email intent
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
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.open_main, R.anim.close_next);
    }

}
