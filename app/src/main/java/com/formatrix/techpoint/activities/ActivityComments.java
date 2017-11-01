package com.formatrix.techpoint.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.formatrix.techpoint.R;
import com.formatrix.techpoint.fragments.FragmentComments;
import com.formatrix.techpoint.utils.Utils;

/**
 * Design and developed by formatrix.com
 *
 * ActivityComments is created to display comments that available in an article.
 * Created using AppCompatActivity.
 */
public class ActivityComments extends AppCompatActivity {

    // Create variables to store data
    private String mArticleId;
    // Create view object
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Get data that passed from previous Activity
        Intent i        = getIntent();
        mArticleId      = i.getStringExtra(Utils.EXTRA_ARTICLE_ID);

        // Connect view object and view id in xml
        mToolbar        = (Toolbar) findViewById(R.id.toolbar);

        // Set toolbar as actionbar
        setSupportActionBar(mToolbar);
        // Add navigation back button to toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Pass selected category from previous page
        // and replace the following toolbar title with selected category
        Bundle bundle = new Bundle();
        bundle.putString(Utils.EXTRA_ARTICLE_ID, mArticleId);
        bundle.putString(Utils.EXTRA_ACTIVITY, Utils.TAG_ACTIVITY_COMMENTS);

        // Create FragmentComments object and replace fragment_container with it
        Fragment fragment = new FragmentComments();
        fragment.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }


    // Method to handle physical back button with animation
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.open_main, R.anim.close_next);
    }

}
