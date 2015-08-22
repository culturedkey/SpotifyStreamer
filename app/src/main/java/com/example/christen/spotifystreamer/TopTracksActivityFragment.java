package com.example.christen.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A placeholder fragment containing a simple view.
 */
public class TopTracksActivityFragment extends Fragment {

    private View rootView;
    private ArrayList<Track> topTracks;
    private TracksAdapter trackResultsAdapter;

    private int mPosition = ListView.INVALID_POSITION;

    private static final String SELECTED_KEY = "selected_position";

    private String mArtistName;
    private String mArtistID;
    boolean mIsLargeLayout;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);


        mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);
        // Retain this fragment across configuration changes.
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        if (topTracks == null)
        {
            topTracks = new ArrayList<Track>();
        }

         trackResultsAdapter =
                new TracksAdapter(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_track, // The name of the layout ID.
                        topTracks);

        //Attach listview to Adapter
        final ListView listView = (ListView) rootView.findViewById(R.id.track_results_listView);
        listView.setAdapter(trackResultsAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Track trackSelected = trackResultsAdapter.getItem(position);
                String trackID = trackSelected.id;
                ArrayList<String> trackIDList = new ArrayList<String>();
                for(int i=0; i<trackResultsAdapter.getCount();i++){
                    trackIDList.add(i,trackResultsAdapter.getItem(i).id);
                }
                if (trackSelected != null) {
                    showDialog(trackIDList, position);
                }
                mPosition = position;
            }
        });

        Bundle arguments = getArguments();
        if (savedInstanceState != null && savedInstanceState.containsKey("artistID")){
            mArtistID = savedInstanceState.getString("artistID");
            getTopTracks(mArtistID);
        }
        if (arguments != null && arguments.getString("artistID") != null){
            mArtistID = arguments.getString("artistID");
            getTopTracks(mArtistID);
        }
//
//        else {
//            Intent intent = getActivity().getIntent();
//            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
//                mArtistID = intent.getStringExtra(Intent.EXTRA_TEXT);
//                getTopTracks(mArtistID);
//
//                //Do we get in here when its a phone?? Test this...
//
//            }
//        }


        //If there's instance state, mine it for useful information. Modified from Sunshine
        if (savedInstanceState != null ) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
            return  rootView;
    }
    public void showDialog(ArrayList<String> idList, int position) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        NowPlayingFragment newFragment = new NowPlayingFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("playlist", idList);
        args.putInt("position", position);
        newFragment.setArguments(args);

        if (mIsLargeLayout) {
            // The device is using a large layout, so show the fragment as a dialog
            newFragment.show(fragmentManager, "dialog");
        } else {


            Intent intent = new Intent(getActivity(),NowPlayingActivity.class);
            intent.putExtra("playlist", idList);
            intent.putExtra("position", position);
            startActivity(intent);


        }
    }

    public void onSaveInstanceState(Bundle outState){
        //Save currently selected list item
        if (mPosition != ListView.INVALID_POSITION){
            outState.putInt(SELECTED_KEY,mPosition);
        }

        super.onSaveInstanceState(outState);

    }
    private void getTopTracks (String artistID){
        //Setup spotify wrapper and get tracks

        mArtistID = artistID.substring(0, artistID.indexOf(","));
        mArtistName = artistID.substring(artistID.indexOf(",") + 2);

        SpotifyApi api = new SpotifyApi();
        SpotifyService spotify = api.getService();
        Map<String,Object> map = new TreeMap<>();
        map.put("country", "US");
        spotify.getArtistTopTrack(mArtistID,map, new Callback<Tracks>() {
            @Override
            public void failure(RetrofitError spotifyError) {
                Log.e("RetrofitError", "Error: " + spotifyError.getMessage());
                int duration = Toast.LENGTH_SHORT;
                Context context = rootView.getContext();
                CharSequence text = getString(R.string.checkConnection);
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }

            @Override
            public void success(Tracks tracks,Response response) {
                if (tracks.tracks.size()!= 0) {

                    trackResultsAdapter.clear();
                    for(Track track : tracks.tracks){
                        trackResultsAdapter.add(track);
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            trackResultsAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }
}
