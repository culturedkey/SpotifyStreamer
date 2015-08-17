package com.example.christen.spotifystreamer;


import android.content.Context;
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
    public static final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    private ListView mlistView;
    private ArtistAdapter artistResults;
    private EditText editText;
    private String searchString;
    private View rootView;
    private ArrayList<Artist> artistsList;
    private int mPosition = ListView.INVALID_POSITION;

    private static final String SELECTED_KEY = "selected_position";
    private static final int MAIN_LOADER = 0;

    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(String artistIDandName);
    }
    public MainActivityFragment() {
    }
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        if (artistsList == null)
//        {
//            artistsList = new ArrayList<Artist>();
//        }
//
//        artistResults =
//                new ArtistAdapter(
//                        getActivity(), // The current context (this activity)
//                        R.layout.list_item_artist, // The name of the layout ID.
//                        artistsList);
        artistResults = new ArtistAdapter(
                            getActivity(),
                            R.layout.list_item_artist,
                            new ArrayList<Artist>());

        rootView =  inflater.inflate(R.layout.fragment_main, container, false);

        //Attach adapter to listview
        mlistView = (ListView) rootView.findViewById(R.id.artist_results);
        mlistView.setAdapter(artistResults);
        mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Artist artistSelected = artistResults.getItem(position);
                if (artistSelected != null) {
                    String artistIDSelected = artistSelected.id;
                    artistIDSelected = artistIDSelected.concat(", ").concat(artistSelected.name);
                    ((Callback) getActivity())
                            .onItemSelected(artistIDSelected);
                }
                mPosition = position;
            }
        });

        //If there's instance state, mine it for useful information. Modified from Sunshine
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        //Code modified from http://developer.android.com/guide/topics/ui/controls/text.html
        editText = (EditText) rootView.findViewById(R.id.artist_search);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    setData(v.getText().toString());

                    handled = true;
                }
                return handled;
            }
        });
        return rootView;
    }



    public void onSaveInstanceState(Bundle outState){
        //Save currently selected list item
        if (mPosition != ListView.INVALID_POSITION){
            outState.putInt(SELECTED_KEY,mPosition);
        }

        super.onSaveInstanceState(outState);

    }
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState){
//        getLoaderManager().initLoader(MAIN_LOADER, null, this);
//        super.onActivityCreated(savedInstanceState);
//    }
//    @Override
//    public Loader<Artist> onCreateLoader (int i, Bundle bundle){
//        Loader<Artist> loader = new Loader<>(getActivity());
//        return loader;
//    }
//
//    @Override
//    public void onLoadFinished(Loader<Artist> loader, Cursor data) {
//        artistResults.swapCursor(data);
//        if (mPosition != ListView.INVALID_POSITION) {
//            // If we don't need to restart the loader, and there's a desired position to restore
//            // to, do so now.
//            mlistView.smoothScrollToPosition(mPosition);
//        }
//    }
//
//    @Override
//    public void onLoaderReset(Loader<Artist> loader) {
//        artistResults.swapCursor(null);
//    }

    public void setData(String artistName){
        searchString = artistName;
        SpotifyApi api = new SpotifyApi();
        SpotifyService spotify = api.getService();
        spotify.searchArtists(searchString, new SpotifyCallback<ArtistsPager>() {
            @Override
            public void failure(SpotifyError spotifyError) {
                Log.e("RetrofitError", "Error: " + spotifyError.getMessage());
                int duration = Toast.LENGTH_SHORT;
                Context context = rootView.getContext();
                CharSequence text = getString(R.string.checkConnection);
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
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
    }
}
