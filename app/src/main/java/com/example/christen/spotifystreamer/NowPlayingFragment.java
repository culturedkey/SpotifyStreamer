package com.example.christen.spotifystreamer;

import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Christen on 8/16/2015.
 */
public class NowPlayingFragment extends DialogFragment {
    private View mrootView;
    public static final int SERVICE_NOTIFICATION_ID = 15;
    public String songURL;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout to use as dialog or embedded fragment
        mrootView = inflater.inflate(R.layout.now_playing_fragment, container, false);

        Bundle args = getArguments();
        if (args.containsKey("trackID")){
            getTrack(args.getString("trackID"), mrootView);
        }

        ImageButton playButton = (ImageButton) mrootView.findViewById(R.id.playButton);

        playButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (songURL != null){
                    Intent intent = new Intent(getActivity(),SpotifyPlayerService.class);
                    intent.putExtra("songURL",songURL);
                    intent.setAction("com.example.action.PLAY");
                    getActivity().startService(intent);
                }
            }
        });

        return mrootView;
    }

    /** The system calls this only when creating the layout in a dialog. */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
    private void getTrack (final String trackID, final View rootView){
        //Setup spotify wrapper and get tracks


        SpotifyApi api = new SpotifyApi();
        SpotifyService spotify = api.getService();
        Map<String,Object> map = new TreeMap<>();
        map.put("country", "US");
        spotify.getTrack(trackID, map, new Callback<Track>() {
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
            public void success(final Track track, Response response) {
                if (track != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            songURL = track.preview_url;
                            TextView artistView = (TextView) rootView.findViewById(R.id.artistTextview);
                            String artists = "";
                            for (int i = 0; i < track.artists.size(); i++) {
                                ArtistSimple artist = track.artists.get(i);
                                artists.concat(artist.name).concat(", ");
                            }
                            artistView.setText(artists);

                            TextView albumView = (TextView) rootView.findViewById(R.id.albumTextview);
                            albumView.setText(track.album.name);

                            ImageView albumArtImage = (ImageView) rootView.findViewById(R.id.albumArtImageview);
                            if (track.album.images.size() >= 1) {
                                Picasso.with(rootView.getContext())
                                        .load(track.album.images.get(1).url)
                                        .into(albumArtImage);
                            }

                            TextView songView = (TextView) rootView.findViewById(R.id.songTextview);
                            songView.setText(track.name);

                            TextView elapsedTimeView = (TextView) rootView.findViewById(R.id.elapsedTimeTextView);
                            elapsedTimeView.setText("0:00");

                            TextView totalTimeView = (TextView) rootView.findViewById(R.id.totalTimeTextView);
                            String duration = String.format("%d:%d",
                                    TimeUnit.MILLISECONDS.toMinutes(track.duration_ms),
                                    TimeUnit.MILLISECONDS.toSeconds(track.duration_ms) -
                                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(track.duration_ms)));
                            totalTimeView.setText(duration);
                        }
                    });
                }
            }
        });
        TextView successText = (TextView) rootView.findViewById(R.id.artistTextview);
    }

    //Modified from http://developer.android.com/guide/topics/media/mediaplayer.html#mediaplayer
    //Service to asynchronously play music in the background
    public static class SpotifyPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener
    {
        private static final String ACTION_PLAY = "com.example.action.PLAY";
        MediaPlayer mMediaPlayer = null;
        private WifiManager.WifiLock wifiLock;


        public int onStartCommand(Intent intent, int flags, int startId) {
            int returnStatus = -1;

            if (intent.getAction().equals(ACTION_PLAY)) {
                mMediaPlayer = new MediaPlayer();// initialize it here
                initMediaPlayer(intent);
                returnStatus = 1;
            }
            return returnStatus;
        }
        public void initMediaPlayer(Intent intent) {
            //TODO: ...initialize the MediaPlayer here...
            // Code snippet from "Running as a Foreground service" in media playback guide
            String songName = "Test";
            // assign the song name to songName

            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnErrorListener(this);
            if(intent.hasExtra("songURL")){
                String url = intent.getStringExtra("songURL");
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try{
                    mMediaPlayer.setDataSource(url);
                    mMediaPlayer.prepareAsync(); // prepare async to not block main thread
                }
                catch (java.io.IOException e){
                    Log.e("IOException", "Error: " + e.getMessage());
                }
                catch ( java.lang.IllegalArgumentException e){
                    Log.e("IllegalArgException", "Error: " + e.getMessage());
                }
                finally {
                    //Set up a notification and make the service run in foreground
                    PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                            new Intent(getApplicationContext(), MainActivity.class),
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    Notification notification = new Notification();
                    notification.tickerText = "Ticker text";
                    notification.icon = R.drawable.ic_music_note_black_24dp;
                    notification.flags |= Notification.FLAG_ONGOING_EVENT;
                    notification.setLatestEventInfo(getApplicationContext(), "MusicPlayerSample",
                            "Playing: " + songName, pi);

                    startForeground(SERVICE_NOTIFICATION_ID, notification);
                    //Set a wake lock so CPU stays awake
                    mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
                    //Set Wifi lock so that the wifi hardware stays on. Must be manually released
                    //in pause and stop and in other errors.
                    wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                            .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

                    wifiLock.acquire();
                }
            }

        }
        @Override
        public IBinder onBind(Intent intent) {
            return new LocalBinder();
        }
        /** Called when MediaPlayer is ready */
        public void onPrepared(MediaPlayer player){
            player.start();
        }
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            //TODO: ... react appropriately ...
            //Call stopForeground and release service?
            //Release wifi locks?
            // The MediaPlayer has moved to the Error state, must be reset!
            // Full state diagram is at http://developer.android.com/reference/android/media/MediaPlayer.html
            return false;
        }
        /**
         * Class for clients to access.  Because we know this service always
         * runs in the same process as its clients, we don't need to deal with
         * IPC. Modified from http://developer.android.com/reference/android/app/Service.html
         */
        public class LocalBinder extends Binder {
            SpotifyPlayerService getService() {
                return SpotifyPlayerService.this;
            }
        }

        @Override
        public void onDestroy(){
            //Clean up all resources

            //Take service out of foreground
            stopForeground(true);
            //Release wifi lock
            if(wifiLock != null && wifiLock.isHeld()){
                wifiLock.release();
            }
            //Release player
            if (mMediaPlayer != null) mMediaPlayer.release();

        }
    }


}
