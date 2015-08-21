package com.example.christen.spotifystreamer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Christen on 7/4/2015.
 */
public class TracksAdapter extends ArrayAdapter<ParcelableTrack> {
    private Context context;
    public ArrayList<ParcelableTrack> topTracksList;

    public TracksAdapter(Context context, int resource, ArrayList<ParcelableTrack> tracksList)
    {
        super(context, resource, tracksList);
        this.context = context;
        this.topTracksList = tracksList;

    }

    public View getView (int position, View convertView, ViewGroup parent)
    {
        //Below code is to prevent endless views from being created per my second reviewer.
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // A holder will hold the references
        // to your views.
        viewHolder holder;
        ParcelableTrack track = topTracksList.get(position);
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_track, parent, false);
            holder = new viewHolder();
            holder.trackTextView = (TextView) convertView.findViewById(R.id.list_item_track_songTextView);
            holder.trackImageView = (ImageView) convertView.findViewById(R.id.list_item_track_imageView);
            holder.albumTextView = (TextView) convertView.findViewById(R.id.list_item_track_albumTextView);
            convertView.setTag(holder);
        }
        else {
            holder = (viewHolder) convertView.getTag();
        }
        holder.trackTextView.setText(track.playlist.name);
        holder.albumTextView.setText(track.playlist.album.name);
        if (track.playlist.album.images != null) {
            if (track.playlist.album.images.size()>= 1) {
                Picasso.with(context)
                        .load(track.playlist.album.images.get(track.playlist.album.images.size()-1).url)
                        .into(holder.trackImageView);
            }
            else {
                holder.trackImageView.setImageResource(R.drawable.blankartist);
            }
        }
        else {
            holder.trackImageView.setImageResource(R.drawable.blankartist);
        }

        return convertView;
    }

    class viewHolder {
        // declare your views here
        TextView trackTextView;
        ImageView trackImageView;
        TextView albumTextView;
    }
}
