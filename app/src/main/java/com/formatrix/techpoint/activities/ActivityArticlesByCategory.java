package com.formatrix.techpoint.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.formatrix.techpoint.R;
import com.formatrix.techpoint.fragments.FragmentArticlesByCategory;
import com.formatrix.techpoint.utils.Utils;

/**
 * Design and developed by formatrix.com
 *
 * ActivityArticlesByCategory is created to display articles in one category.
 * Created using AppCompatActivity.
 */
public class ActivityArticlesByCategory extends AppCompatActivity {

    // Create variables to store data
    private String mCategoryId, mCategoryName;
    // Create view object
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Get data that passed from previous Activity
        Intent i        = getIntent();
        mCategoryId     = i.getStringExtra(Utils.EXTRA_CATEGORY_ID);
        mCategoryName   = i.getStringExtra(Utils.EXTRA_CATEGORY_NAME);

        // Connect view object and view id in xml
        mToolbar        = (Toolbar) findViewById(R.id.toolbar);

        // Set category name as toolbar title
        mToolbar.setTitle(mCategoryName);
        // Set toolbar as actionbar
        setSupportActionBar(mToolbar);
        // Tell that getSupportActionBar() will not be null
        assert getSupportActionBar() != null;
        // Add navigation back button to toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Pass selected category from previous page
        // and replace the following toolbar title with selected category
        Bundle bundle = new Bundle();
        bundle.putString(Utils.EXTRA_CATEGORY_ID, mCategoryId);
        bundle.putString(Utils.EXTRA_ACTIVITY, Utils.TAG_ACTIVITY_ARTICLES_BY_CATEGORY);

        // Create FragmentArticles object and replace fragment_container with it
        Fragment fragment = new FragmentArticlesByCategory();
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
