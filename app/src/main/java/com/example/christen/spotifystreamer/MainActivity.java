package com.example.christen.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;


public class MainActivity extends ActionBarActivity implements MainActivityFragment.Callback {

    //Retaining object code modified from http://www.androiddesignpatterns.com/2013/04/retaining-objects-across-config-changes.html and
    //found issue in my code around not being able to cast to my fragment type thanks to
    //https://github.com/pmatushkin/SpotifyStreamer/blob/master/app/src/main/java/net/catsonmars/android/spotifystreamer/ArtistsFragment.java
    private static final String TAG_TASK_FRAGMENT = "fragment_main";
    private static final String TOPTRACKS_TAG = "TTTAG";

    private boolean mTwoPane;

    private MainActivityFragment mTaskFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getSupportFragmentManager();
        mTaskFragment = (MainActivityFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (mTaskFragment == null) {
            mTaskFragment = new MainActivityFragment();
            fm.beginTransaction().add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
        }
            if (findViewById(R.id.top_track_container) != null) {
                //Detail container only exists in large screen layouts, thus activity is in
                //two pane mode

                mTwoPane = true;

                if (savedInstanceState == null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.top_track_container, new TopTracksActivityFragment(), TOPTRACKS_TAG)
                            .commit();
                }
            } else {
                mTwoPane = false;
            }

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setTitle(R.string.toolBar_title_main);
            setSupportActionBar(toolbar);

    }

    public void onItemSelected (String artistIDandName){
        if (mTwoPane){

            //If its a tablet, make a new fragment with the string for your artist ID and name
            // just like you did originally in MainActivityFragment
            Bundle args = new Bundle();
            args.putString("artistID", artistIDandName);

            TopTracksActivityFragment fragment = new TopTracksActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.top_track_container,fragment,TOPTRACKS_TAG)
                    .commit();
        }
        else {
            Intent intent = new Intent(this,TopTracksActivity.class)
                    .putExtra(Intent.EXTRA_TEXT, artistIDandName);
            startActivity(intent);
        }
    }

    protected void onResume() {
        super.onResume();

    }

}
