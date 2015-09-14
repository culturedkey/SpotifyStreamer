package com.example.christen.spotifystreamer;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
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
public class NowPlayingFragment extends DialogFragment{
    private View mrootView;
    public static final int SERVICE_NOTIFICATION_ID = 15;
    public String songURL;
    public boolean isPlaying = false;
    public static int mDuration = -1;
    public SeekBar mSeekbar;
    public Bundle mInputArgs;
    public TextView elapsedTimeView;
    public static TextView totalTimeView;
    private ServiceManager musicService;

    Intent playIntent ;
    private int mPosition;
    private ArrayList<String> mTracksList;
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout to use as dialog or embedded fragment
        mrootView = inflater.inflate(R.layout.now_playing_fragment, container, false);
        // Create a service 'SomeService1' (see below) and handle incoming messages
        musicService = new ServiceManager(getActivity().getApplicationContext(), SpotifyPlayerService.class, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // Receive message from service
                switch (msg.what) {

                    case SpotifyPlayerService.MSG_SEEKPOSITION: {
                        int location = msg.getData().getInt("duration");
                        setSeekBarLocation(location);
                        break;
                    }

                    default:
                        super.handleMessage(msg);
                }
            }
        });
        musicService.start();
        mSeekbar = (SeekBar) mrootView.findViewById(R.id.seekBar);
        totalTimeView = (TextView) mrootView.findViewById(R.id.totalTimeTextView);
        totalTimeView.setText("0:29");
        mSeekbar.setMax(29);
        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    Bundle args = new Bundle();
                    Message message = new Message();
                    args.putString("action", "seek");
                    args.putInt("position", progress);
                    message.setData(args);
                    try {
                        musicService.send(message);
                    } catch (RemoteException e) {
                        Log.e("RemoteException", "Remote exception in getTrack pause message: " + e.getMessage());
                    } finally {
                        setSeekBarLocation(progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        playIntent = new Intent(getActivity(),SpotifyPlayerService.class);
        mInputArgs = getArguments();
        if (mInputArgs != null && mInputArgs.containsKey("playlist") && mInputArgs.containsKey("position")){
            mPosition = mInputArgs.getInt("position",-1);
            mTracksList = mInputArgs.getStringArrayList("playlist");

            if (mInputArgs.containsKey("elapsedTime")){

                getTrack(mTracksList.get(mPosition), mrootView, mInputArgs.getInt("elapsedTime"));
            }
            else {
                getTrack(mTracksList.get(mPosition), mrootView, -1);
            }
        }

        final ImageButton playButton = (ImageButton) mrootView.findViewById(R.id.playButton);

        playButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImageButton button = (ImageButton) mrootView.findViewById(R.id.playButton);
                if (songURL != null && !isPlaying) {

                    playSong();
                    if (button != null) {
                        Picasso.with(mrootView.getContext())
                                .load(R.drawable.ic_media_pause)
                                .into(button);
                    }
                    isPlaying = true;

                } else if (songURL != null && isPlaying) {


                    Bundle args = new Bundle();
                    Message message = new Message();
                    args.putString("action", "pause");
                    message.setData(args);
                    try{
                        musicService.send(message);
                    }
                    catch (RemoteException e){
                        Log.e("RemoteException", "Remote exception in getTrack pause message: " + e.getMessage() );
                    }

                    if (button != null) {
                        Picasso.with(mrootView.getContext())
                                .load(R.drawable.ic_media_play)
                                .into(button);
                    }
                    isPlaying = false;
                }
            }
        });

        ImageButton nextButton = (ImageButton) mrootView.findViewById(R.id.nextButton);

        nextButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (songURL != null && mPosition + 1 < mTracksList.size()) {
                    mPosition++;
                    mInputArgs.putInt("position", mPosition);
                    getTrack(mTracksList.get(mPosition), mrootView, -1);

                } else {
                    Log.d("PlayFragment", "songURL is null or mPosition is too big. " + songURL + ", " + mPosition + ", " + mTracksList.size());
                }
            }
        });

        ImageButton prevButton = (ImageButton) mrootView.findViewById(R.id.previousButton);

        prevButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (songURL != null && mPosition - 1 >= 0) {
                    mPosition--;

                    mInputArgs.putInt("position", mPosition);
                    getTrack(mTracksList.get(mPosition), mrootView, -1);

                } else {
                    Log.d("PlayFragment", "songURL is null or mPosition is messed up. " + songURL + ", " + mPosition);
                }
            }
        });

        return mrootView;
    }

    public void setSeekBarLocation (int location){
       if (location < mSeekbar.getMax()) {
           mSeekbar.setProgress(location);
           String duration = "";
           if (location <10){
               duration = String.format("%d:0%d",
                       TimeUnit.SECONDS.toMinutes(location), location);
           }
           else {
               duration = String.format("%d:%d",
                       TimeUnit.SECONDS.toMinutes(location), location);}
           mInputArgs.putInt("elapsedTime", location);


           elapsedTimeView.setText(duration);
       }
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
    private void getTrack (final String trackID, final View rootView, final int elapsedTime){
        //Setup spotify wrapper and get tracks


        SpotifyApi api = new SpotifyApi();
        SpotifyService spotify = api.getService();
        Map<String,Object> map = new TreeMap<>();
        map.put("country", "US");
        spotify.getTrack(trackID, map, new Callback<Track>() {
            @Override
            public void failure(RetrofitError spotifyError) {
                Log.e("RetrofitError", "Error in getting track: " + spotifyError.getMessage());
                int duration = Toast.LENGTH_SHORT;
                Context context = rootView.getContext();
                CharSequence text = getString(R.string.checkConnection);
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }

            @Override
            public void success(final Track track, Response response) {
                if (track != null) {
                    Activity parent = getActivity();

                    if (parent != null) {

                        parent.runOnUiThread(new Runnable() {
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

                                                     elapsedTimeView = (TextView) rootView.findViewById(R.id.elapsedTimeTextView);

                                                     if (elapsedTime > -1) {
                                                         String duration = "";
                                                         if (elapsedTime < 10) {
                                                             duration = String.format("%d:0%d",
                                                                     TimeUnit.SECONDS.toMinutes(elapsedTime), elapsedTime);
                                                         } else {
                                                             duration = String.format("%d:%d",
                                                                     TimeUnit.SECONDS.toMinutes(elapsedTime), elapsedTime);
                                                         }
                                                         elapsedTimeView.setText(duration);
                                                     } else {
                                                         elapsedTimeView.setText("0:00");
                                                     }
                                                     ImageButton button = (ImageButton) rootView.findViewById(R.id.playButton);

                                                     playSong();

                                                     isPlaying = true;
                                                     if (button != null) {
                                                         Picasso.with(mrootView.getContext())
                                                                 .load(R.drawable.ic_media_pause)
                                                                 .into(button);
                                                     }
                                                 }
                                             }
                        );
                    } else {
                        Log.e("GetTrackActivityIssue", "Parent activity is null?");
                    }
                }
            }
        });
    }

    public void playSong (){
        if (songURL != null) {
            Bundle args = new Bundle();
            Message message = new Message();
            args.putString("songURL", songURL);
            args.putString("action", "play");
            message.setData(args);
            try {
                musicService.send(message);
            } catch (RemoteException e) {
                Log.e("RemoteException", "Remote exception in getTrack play message: " + e.getMessage());
            }
        }
    }

    //Fix to override bug in V4 compat library per https://code.google.com/p/android/issues/detail?id=17423
    @Override
    public void onDestroyView() {

        Log.d("DestroyDialog", "Dialog is being destroyed.");
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }

        if(musicService != null && musicService.isRunning()){
            musicService.stop();
        }
        super.onDestroyView();
    }

    public void onDestroy (){
        super.onDestroy();
    }

    //Modified from http://developer.android.com/guide/topics/media/mediaplayer.html#mediaplayer
    //Service to asynchronously play music in the background
    public static class SpotifyPlayerService extends AbstractService implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener
    {
        //private static final String ACTION_PLAY = "com.example.action.PLAY";
        //private static final String ACTION_PAUSE = "com.example.action.PAUSE";

        private String currentURL;

        MediaPlayer mMediaPlayer = null;
        //private WifiManager.WifiLock wifiLock;


        public void playSong(String url) {
           try{
                currentURL = url;
                mMediaPlayer.setDataSource(url);
                mMediaPlayer.prepareAsync(); // prepare async to not block main thread

            }
            catch (java.io.IOException e){
                Log.e("IOException", "IO Exception Error: " + e.getMessage());
            }
            catch ( java.lang.IllegalArgumentException e){
                Log.e("IllegalArgException", "Illegal arg exception Error: " + e.getMessage());
            }
//                finally {
//                    //Set up a notification and make the service run in foreground
//                    PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
//                            new Intent(getApplicationContext(), MainActivity.class),
//                            PendingIntent.FLAG_UPDATE_CURRENT);
//                    Notification notification = new Notification();
//                    notification.tickerText = "Ticker text";
//                    notification.icon = R.drawable.ic_music_note_black_24dp;
//                    notification.flags |= Notification.FLAG_ONGOING_EVENT;
//                    notification.setLatestEventInfo(getApplicationContext(), "MusicPlayerSample",
//                            "Playing: " + songName, pi);
//
//                    startForeground(SERVICE_NOTIFICATION_ID, notification);
//                    //Set a wake lock so CPU stays awake
//                    mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
//                    //Set Wifi lock so that the wifi hardware stays on. Must be manually released
//                    //in pause and stop and in other errors.
//                    wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
//                            .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
//
//                    wifiLock.acquire();
//                }
        }
//        @Override
//        public IBinder onBind(Intent intent) {
//            return new LocalBinder();
//        }
        /** Called when MediaPlayer is ready */
        public void onPrepared(MediaPlayer player){
            player.start();

            mDuration = (int) TimeUnit.MILLISECONDS.toSeconds(mMediaPlayer.getDuration());
            TimerTask task = new TimerTask(){
                public void run() {
                    try {
                        Bundle args = new Bundle();
                        int position = (int) TimeUnit.MILLISECONDS.toSeconds(mMediaPlayer.getCurrentPosition());
                        args.putInt("duration", position);
                        Message message = new Message();
                        message.setData(args);
                        message.what = MSG_SEEKPOSITION;
                        send(message);
                    }
                    catch (Throwable t) { }
                };};
            timer.scheduleAtFixedRate(task, 0, 1000);
        }
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            //TODO: ... react appropriately ...
            //Call stopForeground and release service?
            //Release wifi locks?
            // The MediaPlayer has moved to the Error state, must be reset!
            // Full state diagram is at http://developer.android.com/reference/android/media/MediaPlayer.html
            mMediaPlayer.reset();
            return false;
        }
        /**
         * Class for clients to access.  Because we know this service always
         * runs in the same process as its clients, we don't need to deal with
         * IPC. Modified from http://developer.android.com/reference/android/app/Service.html
         */
//        public class LocalBinder extends Binder {
//            SpotifyPlayerService getService() {
//                return SpotifyPlayerService.this;
//            }
//        }

        @Override
        public void onDestroy(){
            //Clean up all resources

            //Take service out of foreground
            //stopForeground(true);
            //Release wifi lock
//            if(wifiLock != null && wifiLock.isHeld()){
//                wifiLock.release();
//            }
            //Release player
            if (mMediaPlayer != null) mMediaPlayer.release();

        }
        public static final int MSG_SEEKPOSITION = 3;
        //public static final int MSG_DURATION = 4;

        private Timer timer = new Timer();

        @Override
        public void onStartService() {

            mMediaPlayer = new MediaPlayer();// initialize it here

            Log.d("PlayerService", "Set up player");
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }

        @Override
        public void onStopService() {
            Log.d("StopService", "Service is being stopped.");
            if (timer != null) {timer.cancel();}

            if (mMediaPlayer != null) mMediaPlayer.release();

        }

        @Override
        public void onReceiveMessage(Message msg) {
            Bundle args = msg.getData();

            if (args.containsKey("action")){
                switch (args.getString("action")){
                    case "play":
                    {

                        if(args.containsKey("songURL")) {
                            if (currentURL != args.getString("songURL")) {

                                mMediaPlayer.reset();

                                playSong(args.getString("songURL"));
                            }
                            else
                            {
                                mMediaPlayer.start();
                            }
                        }
                        break;
                    }
                    case "pause":
                    {
                        mMediaPlayer.pause();
                        break;
                    }

                    case "seek":{
                        if(args.containsKey("position")){
                            if(mMediaPlayer.isPlaying()){
                                mMediaPlayer.seekTo((int)TimeUnit.SECONDS.toMillis(args.getInt("position")));
                            }
                        }
                    }

                }
            }
        }
    }


}
