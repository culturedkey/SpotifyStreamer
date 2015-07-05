package com.example.christen.spotifystreamer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Christen on 7/4/2015.
 */
public class TrackAdapter extends ArrayAdapter<Track> {
    private Context context;
    private List<Track> topTracks;

    public TrackAdapter(Context context, int resource, List<Track> tracks)
    {
        super(context, resource, tracks);
        this.context = context;
        this.topTracks = tracks;

    }

    public View getView (int position, View view, ViewGroup parent)
    {

        Track track = topTracks.get(position);
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_track, parent, false);

        ImageView trackImage = (ImageView) rootView.findViewById(R.id.list_item_track_imageView);
        if (track.album.images.size() >= 1) {
            Picasso.with(context).load(track.album.images.get(track.album.images.size()-1).url).
            into(trackImage);
        }


        TextView trackName = (TextView) rootView.findViewById(R.id.list_item_track_songTextView);
        trackName.setText(track.name);

        TextView albumName = (TextView) rootView.findViewById(R.id.list_item_track_albumTextView);
        albumName.setText(track.album.name);
        return rootView;
    }
}
