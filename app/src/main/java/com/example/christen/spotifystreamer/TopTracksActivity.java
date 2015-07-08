package com.example.christen.spotifystreamer;

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


        FragmentManager fm = getSupportFragmentManager();
        mTaskFragment = (MainActivityFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (mTaskFragment == null) {
            mTaskFragment = new MainActivityFragment();
            fm.beginTransaction().add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.toolBar_title_topTen);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
