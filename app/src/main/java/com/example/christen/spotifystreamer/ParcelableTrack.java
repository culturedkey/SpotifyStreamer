package com.example.christen.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Christen on 8/20/2015.
 */
public class ParcelableTrack extends Track implements Parcelable {
    public Track playlist;

    ParcelableTrack(Track inTrack){
        this.playlist = inTrack;
    }

    protected ParcelableTrack(Parcel in) {
        playlist = (Track) in.readValue(Track.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return playlist.toString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(playlist);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ParcelableTrack> CREATOR = new Parcelable.Creator<ParcelableTrack>() {
        @Override
        public ParcelableTrack createFromParcel(Parcel in) {
            return new ParcelableTrack(in);
        }

        @Override
        public ParcelableTrack[] newArray(int size) {
            return new ParcelableTrack[size];
        }
    };
}
