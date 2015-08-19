package com.example.christen.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;


public class TopTracksActivity extends ActionBarActivity {
    //Retaining object code modified from http://www.androiddesignpatterns.com/2013/04/retaining-objects-across-config-changes.html and
    //found issue in my code around not being able to cast to my fragment type thanks to
    //https://github.com/pmatushkin/SpotifyStreamer/blob/master/app/src/main/java/net/catsonmars/android/spotifystreamer/ArtistsFragment.java
    private static final String TAG_TASK_FRAGMENT = "fragment_top_tracks";

    private MainActivityFragment mTaskFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);

        Toolbar toolbar = (Toolbar) findViewById(R.id.trackActivityToolbar);

        if (savedInstanceState == null){
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

            Bundle arguments = new Bundle();
            String artistInfo = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            arguments.putString("artistID", artistInfo);

            TopTracksActivityFragment fragment = new TopTracksActivityFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.top_track_container, fragment)
                    .commit();

            if(artistInfo != null) {
                String artistName = artistInfo.substring(artistInfo.indexOf(",") + 2);
                toolbar.setSubtitle(artistName);
            }
        }
        else{
            String artistName = savedInstanceState.getString("artistID");
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)){
                artistName = intent.getStringExtra(Intent.EXTRA_TEXT);
            }
            if (artistName != null){
                artistName = artistName.substring(artistName.indexOf(",") + 2);
                toolbar.setSubtitle(artistName);
            }
        }

        FragmentManager fm = getSupportFragmentManager();
        mTaskFragment = (MainActivityFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (mTaskFragment == null) {
            mTaskFragment = new MainActivityFragment();
            fm.beginTransaction()
                    .add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
        }

        toolbar.setTitle(R.string.toolBar_title_topTen);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}