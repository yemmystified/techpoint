package com.formatrix.techpoint.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.formatrix.techpoint.R;
import com.formatrix.techpoint.fragments.FragmentArticlesByCategory;
import com.formatrix.techpoint.providers.ProviderSuggestion;
import com.formatrix.techpoint.utils.Utils;

/**
 * Design and developed by formatrix.com
 *
 * ActivitySearch is created to search result of articles based on the keyword.
 * Created using AppCompatActivity.
 */
public class ActivitySearch extends AppCompatActivity {

    // Create view object
    Toolbar mToolbar;
    // Create variable to store keyword
    String keyword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            keyword = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    ProviderSuggestion.AUTHORITY, ProviderSuggestion.MODE);
            suggestions.saveRecentQuery(keyword, null);
        }
        setContentView(R.layout.activity_home);

        // Connect view object and view id in xml
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        // Set keyword as toolbar title
        mToolbar.setTitle(keyword);
        // Set toolbar as actionbar
        setSupportActionBar(mToolbar);
        // Tell that getSupportActionBar() will not be null
        assert getSupportActionBar() != null;
        // Add navigation back button to toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Pass selected category from previous page
        // and replace the following toolbar title with selected category
        Intent i = getIntent();
        Bundle bundle = new Bundle();
        bundle.putString(Utils.EXTRA_KEYWORD, keyword);
        bundle.putString(Utils.EXTRA_ACTIVITY, Utils.TAG_ACTIVITY_SEARCH);

        // Create FragmentArticles object and replace fragment_container with it
        Fragment fragment = new FragmentArticlesByCategory();
        fragment.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

}
