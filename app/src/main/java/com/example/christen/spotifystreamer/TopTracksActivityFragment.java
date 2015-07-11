package com.example.christen.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
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

    private ArrayList<Track> topTracks;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        if (topTracks == null)
        {
            topTracks = new ArrayList<Track>();
        }

        final TrackAdapter trackResultsAdapter =
                new TrackAdapter(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_track, // The name of the layout ID.
                        topTracks);

        //Attach listview to Adapter
        final ListView listView = (ListView) rootView.findViewById(R.id.track_results_listView);
        listView.setAdapter(trackResultsAdapter);

        //Pull the artistID from the intent
        String artistID = null;
        String artistName = null;
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            artistID = intent.getStringExtra(Intent.EXTRA_TEXT);
            artistName = intent.getStringExtra(Intent.EXTRA_TEXT);
            artistID = artistID.substring(0, artistID.indexOf(","));
            artistName = artistName.substring(artistName.indexOf(",")+2);

            final Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
            toolbar.setSubtitle(artistName);
            //Setup spotify wrapper and get tracks



            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            Map<String,Object> map = new TreeMap<>();
            map.put("country", "US");
            spotify.getArtistTopTrack(artistID,map, new Callback<Tracks>() {
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

            return  rootView;
    }
}
