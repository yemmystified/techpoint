package com.formatrix.techpoint.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.formatrix.techpoint.R;
import com.formatrix.techpoint.utils.Utils;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

/**
 * Design and developed by formatrix.com
 *
 * ActivityBrowser is created to display Facebook and Twitter page using WebView.
 * Created using AppCompatActivity.
 */
public class ActivityBrowser extends AppCompatActivity{

	// Create view objects
	private WebView mWeb;
    private ProgressBar mPrgLoading;
	private Toolbar mToolbar;

	// Create variable to store url
	private String mSocialMediaUrl;

	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);
        
        // Get data that passed from previous activity
        Intent i = getIntent();
		String mSocialMediaTitle = i.getStringExtra(Utils.EXTRA_TITLE);
		mSocialMediaUrl = i.getStringExtra(Utils.EXTRA_SOCIAL_URL);

     	// Connect view objects with view ids in xml
		mWeb 			= (WebView) findViewById(R.id.web);
		mToolbar 		= (Toolbar) findViewById(R.id.toolbar);
		mPrgLoading 	= (ProgressBar) findViewById(R.id.prgPageLoading);

		// Set mSocialMediaTitle as toolbar title
		mToolbar.setTitle(mSocialMediaTitle);
		// Set toolbar as actionbar
		setSupportActionBar(mToolbar);
		// Tell that getSupportActionBar() will not be null
		assert getSupportActionBar() != null;
		// Add navigation back button on toolbar
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Set WebView object configuration
		mWeb.setHorizontalScrollBarEnabled(true);
		mWeb.getSettings().setAllowFileAccess(true);
		mWeb.getSettings().setUseWideViewPort(true);
		mWeb.getSettings().setJavaScriptEnabled(true);
        mWeb.getSettings().setAppCacheEnabled(true);
        mWeb.getSettings().getLoadsImagesAutomatically();
        mWeb.getSettings().getSaveFormData();
		mWeb.setInitialScale(1);

		// Load url
		webViewSocial(mSocialMediaUrl);

		mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
					case R.id.menuBackward:
						// Back to previous page
						if(mWeb.canGoBack()){
							mWeb.goBack();
						}
						break;
					case R.id.menuForward:
						// Go to the next page
						if(mWeb.canGoForward()){
							mWeb.goForward();
						}
						break;
					case R.id.menuRefresh:
						// Refresh page
						webViewSocial(mSocialMediaUrl);
						break;
				}
				return true;
			}
		});
        		
    }

    // Method to load url in WebView
    private void webViewSocial(String url){

		// Load url in WebView
		mWeb.loadUrl(url);

        final Activity act = this;
        
        // Setting loading when data request to server
		mWeb.setWebChromeClient(new WebChromeClient(){
        	public void onProgressChanged(WebView webview, int progress){
        		act.setProgress(progress*100);
				mPrgLoading.setProgress(progress);
        	}
        	
        });

		mWeb.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(mWeb, url, favicon);
				// On page start display ProgressBar and WebView
				mPrgLoading.setVisibility(View.VISIBLE);
				view.setVisibility(View.VISIBLE);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(mWeb, url);
				// When finish loading page, set progress bar to 0 and hide it
				// Display WebView
				mPrgLoading.setProgress(0);
				mPrgLoading.setVisibility(View.GONE);
				view.setVisibility(View.VISIBLE);

				if (mWeb.canGoBack()) {
					// Enable and change icon color to white if can go back to the previous page
					mToolbar.getMenu().findItem(R.id.menuBackward)
							.setEnabled(true)
							.setIcon(new IconicsDrawable(getApplicationContext())
									.icon(GoogleMaterial.Icon.gmd_navigate_before)
									.color(Color.WHITE)
									.actionBar());
				} else {
					// Disable and change icon color to dark grey if can go back to the previous page
					try {
						mToolbar.getMenu().findItem(R.id.menuBackward)
								.setEnabled(false)
								.setIcon(new IconicsDrawable(getApplicationContext())
										.icon(GoogleMaterial.Icon.gmd_navigate_before)
										.color(Color.DKGRAY)
										.actionBar());
					}catch (Exception e){
						Toast.makeText(act, e.toString(), Toast.LENGTH_SHORT).show();
					}
				}

				if (mWeb.canGoForward()) {
					// Enable and change icon color to white if can go to the next page
					mToolbar.getMenu().findItem(R.id.menuForward).setEnabled(true)
							.setIcon(new IconicsDrawable(getApplicationContext())
									.icon(GoogleMaterial.Icon.gmd_navigate_next)
									.color(Color.WHITE)
									.actionBar());
				} else {
					// Disable and change icon color dark grey if can go to the next page
					mToolbar.getMenu().findItem(R.id.menuForward)
							.setEnabled(false)
							.setIcon(new IconicsDrawable(getApplicationContext())
									.icon(GoogleMaterial.Icon.gmd_navigate_next)
									.color(Color.DKGRAY)
									.actionBar());
				}
			}

			@SuppressWarnings("deprecation")
			@Override
			public void onReceivedError(WebView view, int errorCode, String description,
										String failingUrl) {
				// Handle the error
				view.stopLoading();
				view.loadData(
						"<body style=\"margin:0;padding:0;width:100%;height:100%; display:table\">" +
								"<div style = \"display:table-cell;text-align:center; " +
								"vertical-align:middle;\"><p style=\"font-size:50px;" +
								"font-style: normal;\">" +
								getString(R.string.no_internet_connection)
								+ "</p></div>", "text/html", "utf-8");
				view.setVisibility(View.GONE);
			}

			@TargetApi(android.os.Build.VERSION_CODES.M)
			@Override
			public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
				// Redirect to deprecated method, so you can use it in all SDK versions
				onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(),
						req.getUrl().toString());
			}

		});
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu;
		// this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_activity_browser, menu);

		// Add back menu item to toolbar
		mToolbar.getMenu().findItem(R.id.menuBackward)
				.setEnabled(false)
				.setIcon(new IconicsDrawable(getApplicationContext())
						.icon(GoogleMaterial.Icon.gmd_navigate_before)
						.color(Color.DKGRAY)
						.actionBar());

		// Add forward menu item to toolbar
		mToolbar.getMenu().findItem(R.id.menuForward)
				.setEnabled(false)
				.setIcon(new IconicsDrawable(getApplicationContext())
						.icon(GoogleMaterial.Icon.gmd_navigate_next)
						.color(Color.DKGRAY)
						.actionBar());

		// Add refresh menu item to toolbar
		mToolbar.getMenu().findItem(R.id.menuRefresh)
				.setIcon(new IconicsDrawable(getApplicationContext())
						.icon(GoogleMaterial.Icon.gmd_refresh)
						.color(Color.WHITE)
						.actionBar());

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		overridePendingTransition (R.anim.open_main, R.anim.close_next);
	}

    
}