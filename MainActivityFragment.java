package com.example.christen.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.client.Response;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public ArtistAdapter artistResults;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        artistResults =
                new ArtistAdapter(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_artist, // The name of the layout ID.
                        new ArrayList<Artist>());
        final View rootView =  inflater.inflate(R.layout.fragment_main, container, false);

        //Attach adapter to listview
        ListView listView = (ListView) rootView.findViewById(R.id.artist_results);
        listView.setAdapter(artistResults);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Artist artistSelected = artistResults.getItem(position);
                String artistIDSelected = artistSelected.id;
                artistIDSelected = artistIDSelected.concat(", ").concat(artistSelected.name);
                Intent intent = new Intent(getActivity(), TopTracksActivity.class).
                        putExtra(Intent.EXTRA_TEXT, artistIDSelected);
                startActivity(intent);
            }
        });
        //Code modified from http://developer.android.com/guide/topics/ui/controls/text.html
        final EditText editText = (EditText) rootView.findViewById(R.id.artist_search);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    SpotifyApi api = new SpotifyApi();
                    SpotifyService spotify = api.getService();
                    ArtistsPager results = new ArtistsPager();
                    String searchString = v.getText().toString();
                    spotify.searchArtists(searchString, new SpotifyCallback<ArtistsPager>() {
                            @Override
                            public void failure(SpotifyError spotifyError) {
                                Log.e("RetrofitError", "Error: " + spotifyError.getMessage());
                            }

                            @Override
                            public void success(ArtistsPager artistsPager, Response response) {
                                if (artistsPager.artists.total == 0) {
                                //No artists found, notify user
                                    int duration = Toast.LENGTH_SHORT;
                                    Context context = rootView.getContext();
                                    CharSequence text = getString(R.string.noArtistsFound);
                                    Toast toast = Toast.makeText(context, text, duration);
                                    toast.show();
                                }
                                else
                                {
                                                                       artistResults.clear();
                                    List<Artist> artistList = artistsPager.artists.items;
                                    for(Artist result : artistList)
                                    {
                                        artistResults.add(result);
                                    }

                                    //Hide keyboard - code from Guarav Pandey at https://discussions.udacity.com/t/imeoptions-not-showing-up-in-oneditoraction/22122
                                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

                                }
                            }
                        });

                    handled = true;
                }
                return handled;
            }
        });
        return rootView;
    }
}
