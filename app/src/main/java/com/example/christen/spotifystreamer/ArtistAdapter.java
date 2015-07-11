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

    public View getView (int position, View convertView, ViewGroup parent)
    {

        //Below code is to prevent endless views from being created per my second reviewer.
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // A holder will hold the references
        // to your views.
        viewHolder holder;
        Artist artist = getItem(position);

        if(convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_artist, parent, false);
            holder = new viewHolder();
            holder.artistTextView = (TextView) convertView.findViewById(R.id.list_item_artist_textView);
            holder.artistImageView = (ImageView) convertView.findViewById(R.id.list_item_artist_imageView);
            convertView.setTag(holder);
        }
        else {
            holder = (viewHolder) convertView.getTag();
        }
        holder.artistTextView.setText(artist.name);
        if (artist.images != null) {
            if (artist.images.size()>= 1) {
                Picasso.with(context)
                        .load(artist.images.get(artist.images.size()-1).url)
                        .into(holder.artistImageView);
            }
            else {
                holder.artistImageView.setImageResource(R.drawable.blankartist);
            }
        }
        else {
            holder.artistImageView.setImageResource(R.drawable.blankartist);
        }

        return convertView;
    }

    class viewHolder {
        // declare your views here
        TextView artistTextView;
        ImageView artistImageView;
    }

}
