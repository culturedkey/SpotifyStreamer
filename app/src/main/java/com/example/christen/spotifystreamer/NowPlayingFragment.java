package com.example.christen.spotifystreamer;

import android.app.Dialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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

import com.example.christen.spotifystreamer.NowPlayingFragment.SpotifyPlayerService.MusicBinder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
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
    private static View mrootView;
    public static final int SERVICE_NOTIFICATION_ID = 15;
    public String songURL;
    public static Track selectedTrack;

    private SpotifyPlayerService spotifyPlayerService;
    private static Intent playIntent;
    private boolean musicBound=false;

    public static ArrayList<String> playList;
    public static int position;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // Retain this fragment across configuration changes.
        setRetainInstance(true);

    }
    //connect to the service
    public ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder) service;
            //get service
            spotifyPlayerService = binder.getService();
            //pass list
            spotifyPlayerService.setList(playList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    public void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(getActivity(), SpotifyPlayerService.class);
            getActivity().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(playIntent);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout to use as dialog or embedded fragment
        mrootView = inflater.inflate(R.layout.now_playing_fragment, container, false);

        //Get the URI from the bundle and populate all the views
        Bundle args;
        if (savedInstanceState == null){
            args = getArguments();
        }
        else {
            args = savedInstanceState;
        }
        if (args != null && args.containsKey("playlist") && args.containsKey("position")) {
            playList = args.getStringArrayList("playlist");
            position = args.getInt("position");
            getTrack(playList, position, mrootView);
        }

        ImageButton playButton = (ImageButton) mrootView.findViewById(R.id.playButton);

        playButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                if (songURL != null) {
//                    Intent intent = new Intent(getActivity(), SpotifyPlayerService.class);
//                    intent.putExtra("songURL", songURL);
//                    intent.setAction("com.example.action.PLAY");
//                    getActivity().startService(intent);
//                }
                songPicked();
            }
        });

        return mrootView;
    }

    @Override
    public void onDestroy() {
        getActivity().stopService(playIntent);
        spotifyPlayerService=null;
        super.onDestroy();
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

    public void songPicked (){
        spotifyPlayerService.setSong(position);
        spotifyPlayerService.playSong();
    }
    public void getTrack (final ArrayList<String> playlist, final int position, final View rootView){
        //Setup spotify wrapper and get tracks


        SpotifyApi api = new SpotifyApi();
        SpotifyService spotify = api.getService();
        Map<String,Object> map = new TreeMap<>();
        map.put("country", "US");
        if (position != -1 && position < playlist.size()) {
            String trackID = playlist.get(position);

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
                                selectedTrack = track;
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
                    else{
                        Log.d("BlankTrack", "Error: Track was blank?");
                    }
                }

            });
        }
        else {

        }
    }

    //Modified from http://developer.android.com/guide/topics/media/mediaplayer.html#mediaplayer
    //Code also written based on guide at http://code.tutsplus.com/tutorials/create-a-music-player-on-android-song-playback--mobile-22778
    //Service to asynchronously play music in the background
    public static class SpotifyPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener
    {
        private ArrayList<String> songs;
        //current position
        private int songPosn;
        private static final String ACTION_PLAY = "com.example.action.PLAY";
        MediaPlayer mMediaPlayer;
        private WifiManager.WifiLock wifiLock;
        private final IBinder musicBind = new MusicBinder();

        public void onCreate(){
            super.onCreate();
            mMediaPlayer = new MediaPlayer();
            songPosn = 0;
            initMediaPlayer();
        }

//
//        public int onStartCommand(Intent intent, int flags, int startId) {
//            int returnStatus = -1;
//
//            if (intent.getAction().equals(ACTION_PLAY)) {
//                initMediaPlayer(intent);
//                returnStatus = 1;
//            }
//            return returnStatus;
//        }
        public void initMediaPlayer() {
            //TODO: ...initialize the MediaPlayer here...
            // Code snippet from "Running as a Foreground service" in media playback guide
            String songName = "Test";
            // assign the song name to songName

            //Set a wake lock so CPU stays awake
            mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnCompletionListener(this);

        }

        public void playSong(){
            //Reset since we'll be calling this when user is playing subsequent songs
            mMediaPlayer.reset();
            Track playTrack = selectedTrack;
            String trackUrl = playTrack.uri;

            try{
                mMediaPlayer.setDataSource(trackUrl);
            }
            catch(Exception e){
                Log.e("MUSIC SERVICE", "Error setting data source", e);
            }
            finally {
                mMediaPlayer.prepareAsync();

//                //Set up a notification and make the service run in foreground
//                PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
//                        new Intent(getApplicationContext(), MainActivity.class),
//                        PendingIntent.FLAG_UPDATE_CURRENT);
//                Notification notification = new Notification();
//                notification.tickerText = "Ticker text";
//                notification.icon = R.drawable.ic_music_note_black_24dp;
//                notification.flags |= Notification.FLAG_ONGOING_EVENT;
//                notification.setLatestEventInfo(getApplicationContext(), "MusicPlayerSample",
//                        "Playing: " + songName, pi);
//
//                startForeground(SERVICE_NOTIFICATION_ID, notification);

//                    //Set Wifi lock so that the wifi hardware stays on. Must be manually released
//                    //in pause and stop and in other errors.
//                    wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
//                            .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
//
//                    wifiLock.acquire();
            }
        }

        public void setSong(int songIndex){
            songPosn=songIndex;
        }

        //Instantiate tracklist
        public void setList(ArrayList<String> theSongs){
            songs=theSongs;
        }
        @Override
        public IBinder onBind(Intent intent) {
            return musicBind;
        }
        @Override
        //Release resources when service is unbound
        public boolean onUnbind(Intent intent){
            mMediaPlayer.stop();
            mMediaPlayer.release();
            return false;
        }

        /** Called when MediaPlayer is ready */
        public void onPrepared(MediaPlayer player){
            ImageButton button = (ImageButton) mrootView.findViewById(R.id.playButton);
            if (button != null){
                Picasso.with(mrootView.getContext())
                        .load(R.drawable.ic_media_pause)
                        .into(button);
            }
            player.start();

        }
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            //TODO: ... react appropriately ...
            //Set button to show that it isn't playing
            ImageButton button = (ImageButton) mrootView.findViewById(R.id.playButton);
            if (button != null){
                Picasso.with(mrootView.getContext())
                        .load(R.drawable.ic_media_play)
                        .into(button);
            }

            //Call stopForeground and release service
            //Release wifi locks
            //Take service out of foreground
            //stopForeground(true);
            //Release wifi lock
            if(wifiLock != null && wifiLock.isHeld()){
                wifiLock.release();
            }
            // The MediaPlayer has moved to the Error state, must be reset!
            mMediaPlayer.reset();
            //TODO:Is there a way to display the error? Do we want to even?
            // Full state diagram is at http://developer.android.com/reference/android/media/MediaPlayer.html
            Log.e("SpotifyPlayerError", "Player error: " + what + ", " + extra);

            return false;
        }

        @Override
        public void onCompletion(MediaPlayer mp) {

        }

        /**
         * Class for clients to access.  Because we know this service always
         * runs in the same process as its clients, we don't need to deal with
         * IPC. Modified from http://developer.android.com/reference/android/app/Service.html
         */
        public class MusicBinder extends Binder {
            SpotifyPlayerService getService() {
                return SpotifyPlayerService.this;
            }
        }

        @Override
        public void onDestroy(){
            //Clean up all resources

            super.onDestroy();
            //Take service out of foreground
            //stopForeground(true);
//            //Release wifi lock
//            if(wifiLock != null && wifiLock.isHeld()){
//                wifiLock.release();
//            }
            //Release player
            if (mMediaPlayer != null) mMediaPlayer.release();
            super.onDestroy();

        }
    }


}
