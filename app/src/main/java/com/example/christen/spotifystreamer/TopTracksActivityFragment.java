package com.example.christen.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private TrackAdapter trackResultsAdapter;

    private String mArtistName;
    private String mArtistID;
//
//    public void onCreate(Bundle savedInstanceState)
//    {
//        super.onCreate(savedInstanceState);
//
//        // Retain this fragment across configuration changes.
//        setRetainInstance(true);
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);


        //TODO: make a method with the code from the if statement below grabbing the top tracks
        //when a artist string is passed in

        if (topTracks == null)
        {
            topTracks = new ArrayList<Track>();
        }

         trackResultsAdapter =
                new TrackAdapter(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_track, // The name of the layout ID.
                        topTracks);

        //Attach listview to Adapter
        final ListView listView = (ListView) rootView.findViewById(R.id.track_results_listView);
        listView.setAdapter(trackResultsAdapter);

        Bundle arguments = getArguments();
        if (arguments != null && arguments.getString("artistID") != null){
            mArtistID = arguments.getString("artistID");
            getTopTracks(mArtistID);
        }

        else {
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                mArtistID = intent.getStringExtra(Intent.EXTRA_TEXT);
                getTopTracks(mArtistID);

                //Do we get in here when its a phone?? Test this...

            }
        }
            return  rootView;
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
