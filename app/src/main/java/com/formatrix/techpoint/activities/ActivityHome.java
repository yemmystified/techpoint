package com.formatrix.techpoint.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Toast;

import com.adcolony.sdk.AdColonyAdOptions;
import com.adcolony.sdk.AdColonyInterstitial;
import com.adcolony.sdk.AdColonyInterstitialListener;
import com.formatrix.techpoint.R;
import com.formatrix.techpoint.fragments.FragmentArticles;
import com.formatrix.techpoint.fragments.FragmentCategories;
import com.formatrix.techpoint.utils.Utils;
import com.formatrix.techpoint.youtube.activities.ActivityVideo;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

/**
 * Design and developed by formatrix.com
 *
 * ActivityHome is created to display main functions of this app with drawer menu.
 * Created using AppCompatActivity.
 */
public class ActivityHome extends AppCompatActivity {

    // Create tag for log
    private static final String TAG = ActivityHome.class.getSimpleName();

    // Create view objects
    private Toolbar mToolbar;
    private Fragment mFragment;
    private Drawer mDrawer;
    private String phone1 = "+2348186016629";
    SharedPreferences appPreferences;
    boolean isAppInstalled = false;

    //    Error listener for permission
    private PermissionRequestErrorListener errorListener;

//    the line below is for the header above the navigation drawer
    private AccountHeader headerResult = null;

    private AdColonyInterstitial ad;
    private AdColonyInterstitialListener listener;
    private AdColonyAdOptions ad_options;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        AppRater.app_launched(this);

        // Connect view objects and view ids from xml
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        mToolbar.setTitle(getString(R.string.recent_post));

        // Set toolbar as actionbar
        setSupportActionBar(mToolbar);



        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withDividerBelowHeader(false)
//                .withCompactStyle(true)
                .withHeaderBackground(R.drawable.header)
                .withTextColor(getResources()
                .getColor(R.color.primary_color))
                .withSelectionSecondLine("Techpoint.ng")
                .withSavedInstance(savedInstanceState)
                .build();
        // Create drawer
        mDrawer = new DrawerBuilder(this)
//                .withRootView(R.id.drawer_container)
                .withToolbar(mToolbar)
                .withAccountHeader(headerResult)
                .withActionBarDrawerToggleAnimated(true)
                // Add menu items to the drawer
                .addDrawerItems(
                        new PrimaryDrawerItem()
                                .withName(getString(R.string.recent_post))
                                .withIcon(GoogleMaterial.Icon.gmd_chrome_reader_mode)
                                .withIdentifier(1)
                                .withSelectable(true),

                        new PrimaryDrawerItem()
                                .withName(getString(R.string.categories))
                                .withIcon(GoogleMaterial.Icon.gmd_apps)
                                .withIdentifier(2)
                                .withSelectable(true),

//                        new DividerDrawerItem(), this is to create a line between the drawer items

//this line below is done to make the a unclickable header in the drawer list with the help of description and set eneble to false (unclickable)
//                        new PrimaryDrawerItem()
//                                .withDescription("Media")
//                                .withEnabled(false)
//                                .withSelectable(false),

                        new PrimaryDrawerItem()
                                .withName(getString(R.string.video))
                                .withIcon(GoogleMaterial.Icon.gmd_theaters)
                                .withIdentifier(3)
//                                .withTypeface(custom_font)
                                .withSelectable(true),

                        new DividerDrawerItem(),


                        new PrimaryDrawerItem()
                                .withName(getString(R.string.facebook))
                                .withIcon(CommunityMaterial.Icon.cmd_facebook)
                                .withIdentifier(4)
                                .withSelectable(true),

                        new PrimaryDrawerItem()
                                .withName(getString(R.string.twitter))
                                .withIcon(CommunityMaterial.Icon.cmd_twitter)
                                .withIdentifier(5)
                                .withSelectable(true),

                        new PrimaryDrawerItem()
                                .withName(getString(R.string.instagram))
                                .withIcon(CommunityMaterial.Icon.cmd_instagram)
                                .withIdentifier(6)
                                .withSelectable(true),

                        new PrimaryDrawerItem()
                                .withName(getString(R.string.contact_us))
                                .withIcon(GoogleMaterial.Icon.gmd_phone)
                                .withIdentifier(7)
                                .withSelectable(true),

                        new PrimaryDrawerItem()
                                .withName(getString(R.string.about))
                                .withIcon(GoogleMaterial.Icon.gmd_info)
                                .withIdentifier(8)
                                .withSelectable(true)
                )

//                uncomment this below if you want to include the sticky bar below the drawer
//*************************************************************************************************************************
//                .addStickyDrawerItems(
//                        new SecondaryDrawerItem()
//                                .withName(getString(R.string.back_to_post))
//                                .withIdentifier(10)
//                                .withSelectable(false)
//                )
// *************************************************************************************************************************

                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(final View view, int position, IDrawerItem drawerItem) {
                        // Check if the drawerItem is set.
                        // There are different reasons for the drawerItem to be null
                        // --> click on the header
                        // --> click on the footer
                        // Those items don't contain a drawerItem
                        if (drawerItem != null) {
                            // Utils.PARAM_PREFERENCES_DRAWER) != drawerItem.getIdentifier()
                            // it means menu can't click again when in there
                            if (drawerItem.getIdentifier() == 1 &&
                                    Utils.loadIntPreferences(getApplicationContext(),
                                            Utils.ARG_DRAWER_PREFERENCE,
                                            Utils.ARG_PREFERENCES_DRAWER) != drawerItem.getIdentifier()) {

                                // Set toolbar title and selected drawer item
                                setToolbarAndSelectedDrawerItem(getString(R.string.recent_post), 1);

                                Bundle bundle = new Bundle();
                                bundle.putString(Utils.EXTRA_ACTIVITY, Utils.TAG_ACTIVITY_HOME);

                                // Create FragmentArticles object
                                mFragment = new FragmentArticles();
                                mFragment.setArguments(bundle);

                                // Replace fragment in fragment_container with FragmentArticles
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, mFragment)
                                        .commit();

                            } else if (drawerItem.getIdentifier() == 2 &&
                                    Utils.loadIntPreferences(getApplicationContext(),
                                            Utils.ARG_DRAWER_PREFERENCE,
                                            Utils.ARG_PREFERENCES_DRAWER) !=
                                            drawerItem.getIdentifier()) {

                                // Set toolbar title and selected drawer item
                                setToolbarAndSelectedDrawerItem(getString(R.string.categories), 2);

                                // Create FragmentCategories object
                                mFragment = new FragmentCategories();

                                // Replace fragment in fragment_container with FragmentCategories
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, mFragment)
                                        .addToBackStack(null)
                                        .commit();

                            } else if (drawerItem.getIdentifier() == 3 &&
                                    Utils.loadIntPreferences(getApplicationContext(),
                                            Utils.ARG_DRAWER_PREFERENCE,
                                            Utils.ARG_PREFERENCES_DRAWER) !=
                                            drawerItem.getIdentifier()) {


                                // Open ActivityVideo.java to display Video page
                                Intent videoIntent = new Intent(getApplicationContext(),
                                        ActivityVideo.class);
                                startActivity(videoIntent);
                                overridePendingTransition(R.anim.open_next, R.anim.close_main);
//                                                                overridePendingTransition(R.anim.in_animation, R.anim.out_animation);


                            }

                            else if (drawerItem.getIdentifier() == 4 &&
                                    Utils.loadIntPreferences(getApplicationContext(),
                                            Utils.ARG_DRAWER_PREFERENCE,
                                            Utils.ARG_PREFERENCES_DRAWER) !=
                                            drawerItem.getIdentifier()) {

                                // Open ActivityBrowser to display Facebook page
                                Intent settingsIntent = new Intent(getApplicationContext(),
                                        ActivityBrowser.class);
                                settingsIntent.putExtra(Utils.EXTRA_SOCIAL_URL,
                                        getString(R.string.url_facebook));
                                settingsIntent.putExtra(Utils.EXTRA_TITLE,
                                        getString(R.string.facebook));
                                startActivity(settingsIntent);
                                overridePendingTransition(R.anim.open_next, R.anim.close_main);

                            } else if (drawerItem.getIdentifier() == 5 &&
                                    Utils.loadIntPreferences(getApplicationContext(),
                                            Utils.ARG_DRAWER_PREFERENCE,
                                            Utils.ARG_PREFERENCES_DRAWER) !=
                                            drawerItem.getIdentifier()) {


                                // Open ActivityBrowser to display Twitter page
                                Intent settingsIntent = new Intent(getApplicationContext(),
                                        ActivityBrowser.class);
                                settingsIntent.putExtra(Utils.EXTRA_SOCIAL_URL,
                                        getString(R.string.url_twitter));
                                settingsIntent.putExtra(Utils.EXTRA_TITLE,
                                        getString(R.string.twitter));
                                startActivity(settingsIntent);
                                overridePendingTransition(R.anim.open_next, R.anim.close_main);
                            }
//                            i commented the event bookings place out
//
                            else if (drawerItem.getIdentifier() == 6 &&
                                    Utils.loadIntPreferences(getApplicationContext(),
                                            Utils.ARG_DRAWER_PREFERENCE,
                                            Utils.ARG_PREFERENCES_DRAWER) !=
                                            drawerItem.getIdentifier()) {


                                // Open ActivityBrowser to display Instagram page
                                Intent settingsIntent = new Intent(getApplicationContext(),
                                        ActivityBrowser.class);
                                settingsIntent.putExtra(Utils.EXTRA_SOCIAL_URL,
                                        getString(R.string.url_instagram));
                                settingsIntent.putExtra(Utils.EXTRA_TITLE,
                                        getString(R.string.instagram));
                                startActivity(settingsIntent);
                                overridePendingTransition(R.anim.open_next, R.anim.close_main);
                            } else if (drawerItem.getIdentifier() == 7 &&
                                    Utils.loadIntPreferences(getApplicationContext(),
                                            Utils.ARG_DRAWER_PREFERENCE,
                                            Utils.ARG_PREFERENCES_DRAWER) !=
                                            drawerItem.getIdentifier()) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityHome.this, 3);
//              android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ActivityHome.this, 3);
                                builder.setIcon(R.mipmap.ic_launcher);
                                builder.setTitle("Select to dial")
                                        .setItems(R.array.numbers, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        if (which == 0) {

////                                                            this method is done to show the number in the dialer

                                                            Dexter.withActivity(ActivityHome.this)
                                                                    .withPermission(Manifest.permission.CALL_PHONE)
//                                                                    .withListener(callPermissionListener)
                                                                    .withListener(new PermissionListener() {
                                                                        @Override
                                                                        public void onPermissionGranted(PermissionGrantedResponse response) {
//                                                                            and this method is to dial directly
                                                                            Intent callIntent = new Intent(Intent.ACTION_CALL);
                                                                            callIntent.setData(Uri.parse("tel:" + Uri.encode(phone1.trim())));

                                                                            if (ActivityCompat.checkSelfPermission(ActivityHome.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                                                                // TODO: Consider calling
                                                                                //    ActivityCompat#requestPermissions
                                                                                // here to request the missing permissions, and then overriding
                                                                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                                                                //                                          int[] grantResults)
                                                                                // to handle the case where the user grants the permission. See the documentation
                                                                                // for ActivityCompat#requestPermissions for more details.
                                                                                return;
                                                                            }
                                                                            startActivity(callIntent);
                                                                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                                            Toast.makeText(ActivityHome.this, "Dialling....", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                        @Override public void onPermissionDenied(PermissionDeniedResponse response) {
                                                                            int duration =10000;// amount of time the snackbar should stay visible
                                                                            try {
                                                                                Snackbar.make(view, "Permission needs to be activated to use this feature", duration)
//                                                                                .setAction("Action", null).show();
//                                                                                .setActionTextColor(getResources().getColor(R.color.blue)) // this is to change the color ogf the action on snackbar
                                                                                        .setAction("Activate", new View.OnClickListener() {
                                                                                            @Override
                                                                                            public void onClick(View v) {
                                                                                                Intent intent = new Intent();
//                                                                                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                                                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                                                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                                                                                intent.setData(uri);
                                                                                                startActivity(intent);
                                                                                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                                                            }
                                                                                        }).show();
                                                                            }catch (Exception e){
                                                                                Toast.makeText(ActivityHome.this, "Permission needs to be activated to use this feature", Toast.LENGTH_SHORT).show();

                                                                            }
                                                                        }
                                                                        @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                                                            token.continuePermissionRequest();
                                                                        }
                                                                    })
                                                                    .withErrorListener(errorListener)
                                                                    .check();

//
                                                }
                                            }
                                        }
                                        );
                                            builder.show();
                            } else if (drawerItem.getIdentifier() == 8 &&
                                    Utils.loadIntPreferences(getApplicationContext(),
                                            Utils.ARG_DRAWER_PREFERENCE,
                                            Utils.ARG_PREFERENCES_DRAWER) !=
                                            drawerItem.getIdentifier()) {

                                // Open ActivityAbout.java to display About page
                                Intent aboutIntent = new Intent(getApplicationContext(), ActivityAbout.class);
                                startActivity(aboutIntent);
                                overridePendingTransition(R.anim.open_next, R.anim.close_main);
                            }


//                            uncheck this below to give action t the sticky bar below the mdrawer
//*************************************************************************************************************************
//                            else if (drawerItem.getIdentifier() == 10 &&
//                                    Utils.loadIntPreferences(getApplicationContext(),
//                                            Utils.ARG_DRAWER_PREFERENCE,
//                                            Utils.ARG_PREFERENCES_DRAWER) !=
//                                            drawerItem.getIdentifier()) {
//
//                                // Open ActivityAbout.java to display About page
//                                Intent aboutIntent = new Intent(getApplicationContext(), ActivityAbout.class);
//                                startActivity(aboutIntent);
//
//                                overridePendingTransition(R.anim.open_next, R.anim.close_main);
//                            }
//*************************************************************************************************************************
                        }
                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .withShowDrawerOnFirstLaunch(true)
                .withDrawerGravity(-1)
                .build();

        // Display FragmentArticles by default
        Bundle bundle = new Bundle();
        bundle.putString(Utils.EXTRA_ACTIVITY, Utils.TAG_ACTIVITY_HOME);
        mFragment = new FragmentArticles();
        mFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mFragment)
                .commit();


        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {

            @Override
            public void onBackStackChanged() {
                Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (f != null) {
                    updateTitleAndDrawer(f);
                }

            }
        });

        // Only set the active selection or active profile if we do not recreate the activity
        if (savedInstanceState == null) {
            // Set the selection to the item with the identifier 10
            mDrawer.setSelection(1, false);
        }

        //to create shortcut to homescreen
        appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        isAppInstalled = appPreferences.getBoolean("isAppInstalled",false);
        if(!isAppInstalled){

            addShortcutIcon();
        }
        // finally isAppInstalled should be true.
        SharedPreferences.Editor editor = appPreferences.edit();
        editor.putBoolean("isAppInstalled", true);
        editor.apply();
    }

    //to add shortcut to home
    private void addShortcutIcon() {
        //shortcut Intent object
        Intent shortcutIntent = new Intent(getApplicationContext(),
                ActivitySplash.class);

        shortcutIntent.setAction(Intent.ACTION_MAIN);
        //shortcutIntent is added with addIntent
        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, R.string.app_name);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(getApplicationContext(),
                        R.mipmap.ic_launcher));

        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        // finally broadcast the new Intent
        getApplicationContext().sendBroadcast(addIntent);
    }

    // Method to set toolbar title and active drawer item base on selected drawer item
    private void setToolbarAndSelectedDrawerItem(String title, int selectedDrawerItem){
        mToolbar.setTitle(title);
        mDrawer.setSelection(selectedDrawerItem, false);
    }

    // Method to update toolbar title
    private void updateTitleAndDrawer (Fragment mFragment){
        String fragClassName = mFragment.getClass().getName();

        if (fragClassName.equals(FragmentArticles.class.getName())){
            setToolbarAndSelectedDrawerItem(getString(R.string.recent_post), 1);
        } else if (fragClassName.equals(FragmentCategories.class.getName())){
            setToolbarAndSelectedDrawerItem(getString(R.string.categories), 2);
        } else {
            Log.d(Utils.TAG_FORMATRIX+TAG, "Fragment not available");
        }

    }

    @Override
    public void onBackPressed() {
        // Handle the back press :D close the drawer first and if the drawer is
        // closed close the activity
        if (mDrawer != null && mDrawer.isDrawerOpen()) {
            mDrawer.closeDrawer();
        } else {
            new AlertDialog.Builder(this, 5)
                    .setTitle("Quit")
                    .setMessage("Are you sure you want to exit the application?")
                    .setCancelable(true)
                    .setPositiveButton("YES",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                                }
                            })
                    .setNeutralButton("NO", null).show();
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Add the values which need to be saved from the drawer to the bundle
        outState = mDrawer.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_home, menu);

        // Get the SearchView and set the searchable configuration.
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menuSearch)
                .setIcon(getResources().getDrawable(R.drawable.abs__ic_search))

//                uncheck below to use a different icon class
//                .setIcon(
//                new IconicsDrawable(getApplicationContext())
////                        .icon(GoogleMaterial.Icon.gmd_search)
////                        .icon((IIcon) getResources().getDrawable(R.drawable.abs__ic_search))
//                        .color(Color.WHITE)
//                        .actionBar()
//        )

                .getActionView();
        // Assumes current activity is the searchable activity.
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        // Do not iconify the widget; expand it by default.
        searchView.setIconifiedByDefault(false);

        return super.onCreateOptionsMenu(menu);
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
    protected void onResume() {
        super.onResume();
    }
}
