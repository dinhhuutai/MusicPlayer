package hcmute.edu.vn.musicplayer.Service;

import static hcmute.edu.vn.musicplayer.Activity.PlayerActivity.listSongs;
import static hcmute.edu.vn.musicplayer.MyApplication.ACTION_CLEAR;
import static hcmute.edu.vn.musicplayer.MyApplication.ACTION_NEXT;
import static hcmute.edu.vn.musicplayer.MyApplication.ACTION_PLAY;
import static hcmute.edu.vn.musicplayer.MyApplication.ACTION_PREVIOUS;
import static hcmute.edu.vn.musicplayer.MyApplication.CHANNEL_ID_2;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import hcmute.edu.vn.musicplayer.ActionPlaying;
import hcmute.edu.vn.musicplayer.Activity.PlayerActivity;
import hcmute.edu.vn.musicplayer.Model.Song;
import hcmute.edu.vn.musicplayer.NotificationReceiver;
import hcmute.edu.vn.musicplayer.R;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    IBinder mBinder = new MyBinder();
    MediaPlayer mediaPlayer;
    public static ArrayList<Song> listSong = new ArrayList<>();
    Uri uri;
    public static int position = -1;
    ActionPlaying actionPlaying;
    MediaSessionCompat mediaSessionCompat;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String ARTIST_NAME = "ARTIST NAME";
    public static final String SONG_NAME = "SONG NAME";


    @Override
    public void onCreate() {
        super.onCreate();
        mediaSessionCompat = new MediaSessionCompat(getBaseContext(), "MyAudio");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPosition = intent.getIntExtra("servicePosition", -1);
        String actionName = intent.getStringExtra("ActionName");
        if(myPosition != -1){
            playMedia(myPosition);
        }
        if(actionName!=null){
            switch (actionName){
                case "playPause":
                    playPauseClick();
                    break;
                case "next":
                    nextBtnClick();
                    break;
                case "previous":
                    previousBtnClick();
                    break;
                case "clear":
                    clearBtnClick();
                    break;
            }
        }
        return START_STICKY;
    }

    @Override
        public void onDestroy() {
        super.onDestroy();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    private void playMedia(int StartPosition) {
        listSong = listSongs;
        position = StartPosition;
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            if(listSong != null){
                createMediaPlayer(position);
                mediaPlayer.start();
            }
        }else {
            createMediaPlayer(position);
            mediaPlayer.start();
        }
    }

    public class MyBinder extends Binder{
        public MusicService getService(){
            return MusicService.this;
        }
    }

    public void start(){
        mediaPlayer.start();
    }
    public boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }
    public void stop(){
        mediaPlayer.stop();
    }
    public void pause(){
        mediaPlayer.pause();
    }
    public void release(){
        mediaPlayer.release();
    }
    public int getDuration(){
        return mediaPlayer.getDuration();
    }
    public void seekTo(int position){
        mediaPlayer.seekTo(position);
    }
    public int getCurrentPosition(){
        return mediaPlayer.getCurrentPosition();
    }
    public void createMediaPlayer(int positionInner){
        position = positionInner;
        uri = Uri.parse(listSong.get(position).getPath());
        //playing bottom
        SharedPreferences.Editor editor = getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE).edit();
        editor.putString(MUSIC_FILE, uri.toString());
        editor.putString(SONG_NAME, listSong.get(position).getTitle());
        editor.putString(ARTIST_NAME, listSong.get(position).getArtist());
        editor.apply();
        mediaPlayer = MediaPlayer.create(getBaseContext(), uri);
    }
    public void OnCompleted(){
        mediaPlayer.setOnCompletionListener(this);
    }
    @Override
    public void onCompletion(MediaPlayer mp) {
        if(actionPlaying!=null){
            actionPlaying.nextBtnClicked();
        }
    }
    public void setCallBack(ActionPlaying actionPlaying){
        this.actionPlaying = actionPlaying;
    }

    public void sendNotificationMedia(int playPauseBtn){
        Intent intent = new Intent(this, PlayerActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Intent prevIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PREVIOUS);
        PendingIntent prevPending = PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PLAY);
        PendingIntent pausePending = PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_NEXT);
        PendingIntent nextPending = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent clearIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_CLEAR);
        PendingIntent clearPending = PendingIntent.getBroadcast(this, 0, clearIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        byte[] picture = null;
        picture = getAlbumArt(listSong.get(position).getPath());
        Bitmap thumb = null;
        if(picture != null){
            thumb = BitmapFactory.decodeByteArray(picture, 0, picture.length);
        }else {
            thumb = BitmapFactory.decodeResource(getResources(), R.drawable.img_player);
        }
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_2)
                .setSmallIcon(R.drawable.ic_small_music)
                .setLargeIcon(thumb)
                .setContentTitle(listSong.get(position).getTitle())
                .setContentText(listSong.get(position).getArtist())
                .addAction(R.drawable.ic_previous, "Previous", prevPending)
                .addAction(playPauseBtn, "Pause", pausePending)
                .addAction(R.drawable.ic_next, "Next", nextPending)
                .addAction(R.drawable.ic_clear, "Clear", clearPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSound(null)
                .build();
        startForeground(1, notification);
    }
    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        return art;
    }

    public void nextBtnClick(){
        if(actionPlaying!=null){
            actionPlaying.nextBtnClicked();
        }
    }
    public void playPauseClick(){
        if(actionPlaying!=null){
            actionPlaying.playPauseBtnClicked();
        }
    }
    public void previousBtnClick(){
        if(actionPlaying!=null){
            actionPlaying.prevBtnClicked();
        }
    }
    public void clearBtnClick(){
        if(actionPlaying!=null){
            actionPlaying.clearBtnClicked();
        }
    }

}
