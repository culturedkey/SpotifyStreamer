package com.example.christen.spotifystreamer;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class NowPlayingActivity extends ActionBarActivity {

    public String PLAY_TAG = "playTag";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);

        Toolbar toolbar = (Toolbar) findViewById(R.id.nowPlayActivityToolbar);

        NowPlayingFragment newFragment = new NowPlayingFragment();
        if (getIntent() != null && getIntent().hasExtra("position") && getIntent().hasExtra("playlist")){
            Bundle args = new Bundle();
            args.putStringArrayList("playlist", getIntent().getStringArrayListExtra("playlist"));
            args.putInt("position", getIntent().getIntExtra("position", -1));
            newFragment.setArguments(args);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        //Need to associate fragment
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if(fragmentManager.findFragmentByTag(PLAY_TAG) == null){
            transaction.replace(R.id.nowPlayingActivity, newFragment,PLAY_TAG).commit();
        }
        toolbar.setTitle(R.string.toolBar_title_nowPlaying);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_now_playing, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
//            case R.id.action_settings:
//                return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
