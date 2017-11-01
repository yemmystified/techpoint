/*
 * Copyright 2012 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.formatrix.techpoint.youtube.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.formatrix.techpoint.R;
import com.formatrix.techpoint.youtube.fragments.FragmentChannelVideos;
import com.formatrix.techpoint.youtube.fragments.FragmentNewVideos;
import com.formatrix.techpoint.youtube.fragments.FragmentVideo;
import com.formatrix.techpoint.youtube.utils.UtilsVideo;
import com.google.android.youtube.player.YouTubeApiServiceUtil;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer.OnFullscreenListener;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * A sample Activity showing how to manage multiple YouTubeThumbnailViews in an adapter for display
 * in a List. When the list items are clicked, the video is played by using a YouTubePlayerFragment.
 * <p>
 * The demo supports custom fullscreen and transitioning between portrait and landscape without
 * rebuffering.
 */
@TargetApi(13)
public final class ActivityVideo extends FragmentActivity implements
        OnFullscreenListener,
        FragmentChannelVideos.OnVideoSelectedListener,
        FragmentNewVideos.OnVideoSelectedListener{

  /** The padding between the video list and the video in landscape orientation. */
  private static final int LANDSCAPE_VIDEO_PADDING_DP = 5;
  /** The request code when calling startActivityForResult to recover from an API service error. */
  private static final int RECOVERY_DIALOG_REQUEST = 1;
  // Create object of FragmentVideo
  private FragmentVideo mFragmentVideo;
  // Create variable to handle full screen status
  private boolean isFullscreen;

    //change the link below to the subscribe link you would like to use
  private String subscribeLink = "https://www.youtube.com/channel/UC9T6ECutY4EPwJnGlbw4BzA";

  // Create view objects
  private Drawer mDrawer = null;
  private Toolbar mToolbar;

    //    the line below is for the header above the navigation drawer
    private AccountHeader headerResult = null;

  // Create variables to store channels and playlists data
  private String[] mChannelNames;
  private String[] mVideoTypes;
  private String[] mChannelIds;

  // Set default selected drawer item
  private int mSelectedDrawerItem = 0;

  private Fragment mFragment;
  private FrameLayout frmLayoutList;
    
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
//      to create a fullscreen activity uncomment code below
      getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
              WindowManager.LayoutParams.FLAG_FULLSCREEN);
    setContentView(R.layout.activity_video);

    // Connect view objects with view ids in xml
    frmLayoutList  = (FrameLayout) findViewById(R.id.fragment_container);
    mToolbar       = (Toolbar) findViewById(R.id.toolbar);
//      Enable the line below if you want to disable the toolbar
//      and also comment out all the lines with drawer list in this activity
      mToolbar.setVisibility(View.GONE);
//      mToolbar.setVisibility(View.VISIBLE);
//      the line below is for the menu to be attached to the toolbar
      mToolbar.inflateMenu(R.menu.activity_video);
//      ***********************************************************************************
//      Navigation methods
          mToolbar.setNavigationIcon(R.drawable.md_nav_back);
          mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  ActivityVideo.super.finish();
                  overridePendingTransition (R.anim.open_main, R.anim.close_next);
              }
          });
//      ***********************************************************************************
//      the method below is for when the menuitem has been clicked, oin this case for the subscribe to youtube channel link
      mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
          @Override
          public boolean onMenuItemClick(MenuItem item) {
         if (item.getItemId() == R.id.subscribe){
              startActivity(new Intent(Intent.ACTION_VIEW,
                      Uri.parse(subscribeLink)));
              Toast.makeText(ActivityVideo.this, "Click on the subscribe link", Toast.LENGTH_LONG).show();
              overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
         }
//         else if (item.getItemId() == R.id.about){
//             Toast.makeText(ActivityVideo.this, "this is a toast", Toast.LENGTH_SHORT).show();
//         }
              return false;
          }
      });

    // Set FragmentVideo object
    mFragmentVideo =
        (FragmentVideo) getFragmentManager().findFragmentById(R.id.video_fragment_container);

    // Get channels data from strings.xml
    mChannelNames  = getResources().getStringArray(R.array.channel_names);
    mVideoTypes    = getResources().getStringArray(R.array.video_types);
    mChannelIds    = getResources().getStringArray(R.array.channel_ids);

    // Check Youtube API
    checkYouTubeApi();

      // Set number of PrimaryDrawerItem objects based on number of channel and playlist data
      PrimaryDrawerItem[] mPrimaryDrawerItem = new PrimaryDrawerItem[mChannelIds.length];

      // Set PrimaryDrawerItem object for each channel data
      for(int i = 0; i < mChannelIds.length; i++) {
          mPrimaryDrawerItem[i] = new PrimaryDrawerItem()
                  .withName(mChannelNames[i])
                  .withIdentifier(i)
                  .withSelectable(false);

      }

      headerResult = new AccountHeaderBuilder()
              .withActivity(this)
              .withCompactStyle(true)
              .withHeaderBackground(R.drawable.header)
              .withSavedInstance(savedInstanceState)
              .build();

      // Create drawer menu
      mDrawer = new DrawerBuilder(this)
          .withActivity(ActivityVideo.this)
//              ************************************************************************************************************
//              Rememeber to uncheck the ".withToolbar(mToolbar)" if you dont want to use up button on the toolbar anymore
//          .withToolbar(mToolbar)
//              ************************************************************************************************************
//              .withAccountHeader(headerResult)
          .withRootView(R.id.drawer_container)
          .withActionBarDrawerToggleAnimated(true)
              .withActionBarDrawerToggle(false)
          .withSavedInstance(savedInstanceState)
                  // Add menu items to the drawer
          .addDrawerItems(
                  mPrimaryDrawerItem
          )
//              this below is the stick drawer item to make an action
//          .addStickyDrawerItems(
//                  new SecondaryDrawerItem()
//                          .withName(getString(R.string.back_to_post))
//                          .withIdentifier(mChannelIds.length-1)
//                          .withSelectable(false)
//          )
          .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {

              @Override
              public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                  // Check if the drawerItem is set.
                  // There are different reasons for the drawerItem to be null
                  // --> click on the header
                  // --> click on the footer
                  // Those items don't contain a drawerItem
                  mSelectedDrawerItem = position;
                  if (drawerItem != null) {
                      if (drawerItem.getIdentifier() == 0 && mSelectedDrawerItem != -1) {
                          // Set toolbar title and selected drawer item
                          setToolbarAndSelectedDrawerItem(mChannelNames[0], 0);

                          // Pass all channel names and ids to FragmentNewVideos
                          // to display the latest video for each channel and playlist.
                          Bundle bundle = new Bundle();
                          bundle.putStringArray(UtilsVideo.TAG_CHANNEL_NAMES, mChannelNames);
                          bundle.putStringArray(UtilsVideo.TAG_VIDEO_TYPE, mVideoTypes);
                          bundle.putStringArray(UtilsVideo.TAG_CHANNEL_IDS, mChannelIds);

                          // Create FragmentNewVideos object
                          mFragment = new FragmentNewVideos();
                          mFragment.setArguments(bundle);

                          // Replace fragment in fragment_container with FragmentNewVideos
                          getSupportFragmentManager().beginTransaction()
                                  .replace(R.id.fragment_container, mFragment)
                                  .commit();

                      } else if (drawerItem.getIdentifier() > 0 && mSelectedDrawerItem != -1) {
                          // Set toolbar title and selected drawer item
                          setToolbarAndSelectedDrawerItem(
                                  mChannelNames[mSelectedDrawerItem],
                                  (mSelectedDrawerItem)
                          );

                          // Pass selected video types and channel ids to FragmentChannelVideos
                          Bundle bundle = new Bundle();
                          bundle.putString(UtilsVideo.TAG_VIDEO_TYPE,
                                  mVideoTypes[mSelectedDrawerItem]);
                          bundle.putString(UtilsVideo.TAG_CHANNEL_ID,
                                  mChannelIds[mSelectedDrawerItem]);

                          // Create FragmentChannelVideos object
                          mFragment = new FragmentChannelVideos();
                          mFragment.setArguments(bundle);

                          // Replace fragment in fragment_container with FragmentChannelVideos
                          getSupportFragmentManager().beginTransaction()
                                  .replace(R.id.fragment_container, mFragment)
                                  .commit();
                      } else if (mSelectedDrawerItem == -1) {
                          // go back to the home page closing ActivityVideo.java and opening ActivityHome.java
                          onBackPressed();
                          overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                      }
                  }

                  return false;
              }
          })
          .withSavedInstance(savedInstanceState)
          .withShowDrawerOnFirstLaunch(false)
              .withDrawerGravity(-1)// remove this line to show drawer
          .build();

    // Set toolbar title and selected drawer item with first data in default
    setToolbarAndSelectedDrawerItem(mChannelNames[0], 0);

    // In default display FragmentNewVideos first.
    // Pass all channel names and ids to FragmentNewVideos
    // to display the latest video for each channel.
    Bundle bundle = new Bundle();
    bundle.putStringArray(UtilsVideo.TAG_CHANNEL_NAMES, mChannelNames);
    bundle.putStringArray(UtilsVideo.TAG_VIDEO_TYPE, mVideoTypes);
    bundle.putStringArray(UtilsVideo.TAG_CHANNEL_IDS, mChannelIds);

    // Create FragmentNewVideos and set it as default fragment
    mFragment = new FragmentNewVideos();
    mFragment.setArguments(bundle);

    // Replace fragment in fragment_container with FragmentNewVideos
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
      mDrawer.setSelection(0, false);
    }
  }

  private void checkYouTubeApi() {
    YouTubeInitializationResult errorReason =
        YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(this);
    if (errorReason.isUserRecoverableError()) {
      errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
    } else if (errorReason != YouTubeInitializationResult.SUCCESS) {
      String errorMessage =
          String.format(getString(R.string.error_player),
                  errorReason.toString());
      Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }
  }

  // Method to set toolbar title and active drawer item base on selected drawer item
  private void setToolbarAndSelectedDrawerItem(String title, int selectedDrawerItem){
    mToolbar.setTitle(title);
    mDrawer.setSelection(selectedDrawerItem, false);
  }

  // Method to update toolbar title
  private void updateTitleAndDrawer (Fragment mFragment){
    String fragClassName = mFragment.getClass().getName();

    if (fragClassName.equals(FragmentNewVideos.class.getName())){
      setToolbarAndSelectedDrawerItem(mChannelNames[0], 0);
    } else if (fragClassName.equals(FragmentChannelVideos.class.getName())){
      setToolbarAndSelectedDrawerItem(mChannelNames[mSelectedDrawerItem ],
              (mSelectedDrawerItem ));
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.activity_video, menu);

//    getMenuInflater().inflate(R.menu.activity_video, menu);
    return true;
  }

    @NonNull
    @Override
    public MenuInflater getMenuInflater() {
        return super.getMenuInflater();
    }



  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == RECOVERY_DIALOG_REQUEST) {
      // Recreate the activity if user performed a recovery action
      recreate();
    }
  }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    layout();
  }

  @Override
  public void onFullscreen(boolean isFullscreen) {
    this.isFullscreen = isFullscreen;
    layout();
  }

  /**
   * Sets up the layout programatically for the three different states. Portrait, landscape or
   * fullscreen+landscape. This has to be done programmatically because we handle the orientation
   * changes ourselves in order to get fluent fullscreen transitions, so the xml layout resources
   * do not get reloaded.
   */
  private void layout() {
    boolean isPortrait =
            getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

    if (isFullscreen) {
        mToolbar.setVisibility(View.GONE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        frmLayoutList.setVisibility(View.GONE);
        setLayoutSize(mFragmentVideo.getView(), MATCH_PARENT, MATCH_PARENT);
    } else if (isPortrait) {
//        mToolbar.setVisibility(View.VISIBLE);
        mToolbar.setVisibility(View.GONE);
        frmLayoutList.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setLayoutSize(mFragmentVideo.getView(), WRAP_CONTENT, WRAP_CONTENT);
    } else {
        mToolbar.setVisibility(View.VISIBLE);
        frmLayoutList.setVisibility(View.VISIBLE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        int screenWidth = dpToPx(getResources().getConfiguration().screenWidthDp);
        int videoWidth = screenWidth - screenWidth / 4 - dpToPx(LANDSCAPE_VIDEO_PADDING_DP);
        setLayoutSize(mFragmentVideo.getView(), videoWidth, WRAP_CONTENT);
    }
  }

    @Override
    public void onVideoSelected(String ID) {
    FragmentVideo mFragmentVideo =
            (FragmentVideo) getFragmentManager().findFragmentById(R.id.video_fragment_container);
    mFragmentVideo.setVideoId(ID);
    }

    // Method to convert dp to pixel
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    // Method to set layout size
    private static void setLayoutSize(View view, int width, int height) {
        LayoutParams params = view.getLayoutParams();
        params.width = width;
        params.height = height;
        view.setLayoutParams(params);
    }


        @Override
        public void onBackPressed() {
            if (isFullscreen){
                mFragmentVideo.backnormal();
                mToolbar.setVisibility(View.VISIBLE);
//                mToolbar.setVisibility(View.GONE);
                frmLayoutList.setVisibility(View.VISIBLE);
                setLayoutSize(mFragmentVideo.getView(), WRAP_CONTENT, WRAP_CONTENT);
            } else{
                super.onBackPressed();
                overridePendingTransition(R.anim.open_main, R.anim.close_next);
            }
    }
}
