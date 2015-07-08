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

import kaaes.spotify.webapi.android.models.Artist;

/**
 * Created by Christen on 7/2/2015.
 */
public class ArtistAdapter extends ArrayAdapter<Artist> {
    private Context context;
    private List<Artist> artists;

    public ArtistAdapter(Context context, int resource, List<Artist> artists)
    {
        super(context, resource, artists);
        this.context = context;
        this.artists = artists;

    }

    public View getView (int position, View view, ViewGroup parent)
    {

        Artist artist = getItem(position);
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_artist, parent, false);

        ImageView artistImage = (ImageView) rootView.findViewById(R.id.list_item_artist_imageView);
        if (artist.images.size() >= 1) {
            Picasso.with(context).load(artist.images.get(artist.images.size()-1).url).into(artistImage);
        }
        else
        {
            //This image belongs to Greg Hickman and is under creative commons license Attribution-NonCommercial-NoDerivs 2.0 Generic
            //(CC BY-NC-ND 2.0) please see https://www.flickr.com/photos/greghickman/4306344519
            artistImage.setImageResource(R.drawable.blankartist);
        }


        TextView artistName = (TextView) rootView.findViewById(R.id.list_item_artist_textView);
        artistName.setText(artist.name);

        return rootView;
    }
}
