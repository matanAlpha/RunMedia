package com.media.matanalpha.runmedia;

import android.content.ContentUris;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;

        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.content.IntentFilter;
        import android.content.SharedPreferences;
        import android.media.AudioDeviceCallback;
        import android.media.AudioDeviceInfo;
        import android.media.AudioManager;
        import android.media.AudioRecordingConfiguration;
        import android.media.MediaMetadataRetriever;
        import android.net.Uri;
        import android.os.Build;
        import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
        import android.provider.MediaStore;
        import android.support.annotation.RequiresApi;
        import android.support.v4.media.session.MediaSessionCompat;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.KeyEvent;
        import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.List;
        import java.util.concurrent.TimeUnit;

        import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
        import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.M)
    private class EmptyDeviceCallback extends AudioDeviceCallback {
        public void onAudioDevicesAdded(AudioDeviceInfo[] addedDevices) {

            Log.i("ff","fdf");
        }
        public void onAudioDevicesRemoved(AudioDeviceInfo[] removedDevices) {
            Log.i("ff","fdf");
        }

    }

    private Handler mHandler = new Handler();
    AudioManager.OnAudioFocusChangeListener afChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                public void onAudioFocusChange(int focusChange) {
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                        // Permanent loss of audio focus
                        // Pause playback immediately
                        //mediaController.getTransportControls().pause();
                        // Wait 30 seconds before stopping playback
//                        mHandler.postDelayed(mDelayedStopRunnable,
//                                TimeUnit.SECONDS.toMillis(30));
                        Log.i("ff","ff");
                    }
                    else if (focusChange == AUDIOFOCUS_LOSS_TRANSIENT) {
                        // Pause playback
                        Log.i("ff","ff");
                    } else if (focusChange == AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                        Log.i("ff","ff");
                        // Lower the volume, keep playing
                    } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                        Log.i("ff","ff");
                        // Your app has been granted audio focus again
                        // Raise volume to normal, restart playback if necessary
                    }
                }
            };

    TextView status = null;


    public Bitmap getAlbumart(Long album_id)
    {
        Bitmap bm = null;
        try
        {
            final Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");

            Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);

            ParcelFileDescriptor pfd = getApplicationContext().getContentResolver()
                    .openFileDescriptor(uri, "r");

            if (pfd != null)
            {
                FileDescriptor fd = pfd.getFileDescriptor();
                bm = BitmapFactory.decodeFileDescriptor(fd);
            }
        } catch (Exception e) {
        }
        return bm;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String cmd = intent.getStringExtra("command");

            Log.d("Music", cmd + " : " + action);


            long songId = intent.getLongExtra("id", -1);

            //get the albumid using media/song id
            if(songId!=-1) {
                String selection = MediaStore.Audio.Media._ID + " = "+songId+"";

                Cursor cursor = getContentResolver().query(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] {
                                MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DATA},
                        selection, null, null);

                if (cursor.moveToFirst()) {
                    long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                    Bitmap albumart = getAlbumart(albumId);
                    Log.d("Album ID : ", ""+albumId);

                    if(albumart!=null)
                    {
                        ImageView mImg;
                        mImg = (ImageView) findViewById(R.id.imageView1);
                        mImg.setImageBitmap(albumart);
                    }

                    long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

                    Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                    Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);

                    //set the album art in imageview
                    //albumArt.setImageURI(albumArtUri);
                }
                cursor.close();
            }


            long id = intent.getLongExtra("id",-1);
            if(id>-1)
            {
                Uri uri = Uri.parse("content://media/external/audio/media/" + id + "");
                ParcelFileDescriptor pfd = null;
                try {
                    pfd = getApplicationContext().getContentResolver().openFileDescriptor(uri, "r");


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(fd);
                    String extractMetadata = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

                    Log.i("TT","ff");
                }

            }

            String artist = intent.getStringExtra("artist");
            String album = intent.getStringExtra("album");
            String track = intent.getStringExtra("track");
            status.setText(track);
            boolean playing = intent.getBooleanExtra("playing", false);
            long milliseconds=intent.getLongExtra(MediaStore.Audio.AudioColumns.DURATION, 0);
            MediaMetadataRetriever metaRetriever= new MediaMetadataRetriever();

            Log.d("Music", artist + " : " + album + " : " + track);


        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.status = (TextView) this.findViewById(R.id.track);


    }

    private void playKey(int key)
    {
        AudioManager am = (AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        KeyEvent downEvent = new KeyEvent(KeyEvent.ACTION_DOWN, key);
        am.dispatchMediaKeyEvent(downEvent);

        KeyEvent upEvent = new KeyEvent(KeyEvent.ACTION_UP, key);
        am.dispatchMediaKeyEvent(upEvent);

    }

    private void playKey2(int key)
    {
        AudioManager am = (AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        KeyEvent downEvent = new KeyEvent(KeyEvent.ACTION_DOWN, key);
        am.dispatchMediaKeyEvent(downEvent);

//        KeyEvent upEvent = new KeyEvent(KeyEvent.ACTION_UP, key);
//        am.dispatchMediaKeyEvent(upEvent);

    }

    public static final String CMDTOGGLEPAUSE = "togglepause";
    public static final String CMDPAUSE = "pause";
    public static final String CMDPREVIOUS = "previous";
    public static final String CMDNEXT = "next";
    public static final String SERVICECMD = "com.android.music.musicservicecommand";
    public static final String CMDNAME = "command";
    public static final String CMDSTOP = "stop";
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void clickMe(View view) {


        IntentFilter iF = new IntentFilter();
        iF.addAction("com.android.music.musicservicecommand");
        iF.addAction("com.android.music.metachanged");
        iF.addAction("com.android.music.playstatechanged");
        iF.addAction("com.android.music.updateprogress");



        registerReceiver(mReceiver, iF);



        AudioManager   mAudioManager = (AudioManager) getApplicationContext().getSystemService(
                Context.AUDIO_SERVICE);

        mAudioManager.registerAudioDeviceCallback( new EmptyDeviceCallback(), mHandler);


        mAudioManager.abandonAudioFocus(afChangeListener);

        List<AudioRecordingConfiguration> activeRecordingConfigurations = mAudioManager.getActiveRecordingConfigurations();

        Uri ss = MediaStore.Audio.Playlists.getContentUri("ss");


        if(mAudioManager.isMusicActive()){
            Log.i("TT","Play");
        }else {
            Log.i("TT","NotPlay");
        }

        final int streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Log.i("TT","streamVolume="+streamVolume);

//        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//
//        if(mAudioManager.isMusicActive()) {
//            Intent i = new Intent(SERVICECMD);
//            i.putExtra(CMDNAME, CMDSTOP);
//            getApplicationContext().sendBroadcast(i);
//        }

//        Intent intent = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
//        startActivity(intent);

        switch ( view.getId())
        {
            case R.id.Back:
                // playKey(KeyEvent.KEYCODE_MEDIA_STOP);
                playKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                break;
            case R.id.Pause:
                playKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
                break;

            case R.id.Play:
                playKey(KeyEvent.KEYCODE_MEDIA_PLAY);
                break;

            case R.id.Forward:
                //playKey(KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD);
                //playKey(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD);
                //playKey(KeyEvent.KEYCODE_MEDIA_STEP_FORWARD);
                playKey(KeyEvent.KEYCODE_MEDIA_NEXT);

                break;

            case R.id.Mute:
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,0, 0);

                break;
            case R.id.VolumeUp:

                // playKey2(KeyEvent.KEYCODE_VOLUME_UP);

//                mAudioManager.adjustStreamVolume(
//                        AudioManager.STREAM_MUSIC,
//                        KeyEvent.KEYCODE_VOLUME_UP == KeyEvent.KEYCODE_VOLUME_UP
//                                ? AudioManager.ADJUST_RAISE
//                                : AudioManager.ADJUST_LOWER,
//                        0);

                mAudioManager.adjustVolume(AudioManager.ADJUST_RAISE,0);

                //adjustVolume
                break;

            case R.id.VolumeDown:


//                mAudioManager.adjustStreamVolume(
//                        AudioManager.STREAM_MUSIC,
//                        KeyEvent.KEYCODE_VOLUME_DOWN == KeyEvent.KEYCODE_VOLUME_UP
//                                ? AudioManager.ADJUST_RAISE
//                                : AudioManager.ADJUST_LOWER,
//                        0);
                mAudioManager.adjustVolume(AudioManager.ADJUST_LOWER,0);

                break;


        }
//
//
//        //playKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
//
//
//        // Kabloey
//        Log.i("TAG","bababab "+view.getId());
    }
}
