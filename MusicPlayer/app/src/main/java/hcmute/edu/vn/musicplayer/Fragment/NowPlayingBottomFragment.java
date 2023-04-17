package hcmute.edu.vn.musicplayer.Fragment;

import static android.content.Context.MODE_PRIVATE;
import static hcmute.edu.vn.musicplayer.Activity.MainActivity.ARTIST_TO_FRAG;
import static hcmute.edu.vn.musicplayer.Activity.MainActivity.PATH_TO_FRAG;
import static hcmute.edu.vn.musicplayer.Activity.MainActivity.SHOW_MINI_PLAYER;
import static hcmute.edu.vn.musicplayer.Activity.MainActivity.SONG_NAME_TO_FRAG;
import static hcmute.edu.vn.musicplayer.Activity.MainActivity.songs;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import hcmute.edu.vn.musicplayer.R;
import hcmute.edu.vn.musicplayer.Service.MusicService;

public class NowPlayingBottomFragment extends Fragment implements ServiceConnection {

    public static ImageView nextBrn, albumArt;
    TextView artist, songName;
    public static FloatingActionButton playPauseBtnBottom;
    View view;
    MusicService musicService = null;
    public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String ARTIST_NAME = "ARTIST NAME";
    public static final String SONG_NAME = "SONG NAME";

    public NowPlayingBottomFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_now_playing_bottom, container, false);
        artist = view.findViewById(R.id.song_artist_miniPlayer);
        songName = view.findViewById(R.id.song_name_miniPlayer);
        albumArt = view.findViewById(R.id.bottom_album_art);
        nextBrn = view.findViewById(R.id.skip_next_bottom);
        playPauseBtnBottom = view.findViewById(R.id.play_pause_miniPlayer);


        nextBrn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(musicService != null){
                    musicService.nextBtnClick();
                    if(getActivity() != null){
                        SharedPreferences.Editor editor = getActivity().getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE).edit();
                        editor.putString(MUSIC_FILE, musicService.listSong.get(musicService.position).getPath());
                        editor.putString(SONG_NAME, musicService.listSong.get(musicService.position).getTitle());
                        editor.putString(ARTIST_NAME, musicService.listSong.get(musicService.position).getArtist());
                        editor.apply();
                        SharedPreferences preferences = getActivity().getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE);
                        String path = preferences.getString(MUSIC_FILE, null);
                        String artist_name = preferences.getString(ARTIST_NAME, null);
                        String song_name = preferences.getString(SONG_NAME, null);
                        if(path != null){
                            SHOW_MINI_PLAYER = true;
                            PATH_TO_FRAG = path;
                            ARTIST_TO_FRAG = artist_name;
                            SONG_NAME_TO_FRAG = song_name;
                        }else {
                            SHOW_MINI_PLAYER = false;
                            PATH_TO_FRAG = null;
                            ARTIST_TO_FRAG = null;
                            SONG_NAME_TO_FRAG = null;
                        }
                        if(SHOW_MINI_PLAYER){
                            if(PATH_TO_FRAG != null){
                                byte[] art = getAlbumArt(PATH_TO_FRAG);
                                if(art != null) {
                                    Glide.with(getContext()).load(art).into(albumArt);
                                }else {
                                    Glide.with(getContext()).load(R.drawable.image_default).into(albumArt);
                                }
                                songName.setText(SONG_NAME_TO_FRAG);
                                artist.setText(ARTIST_TO_FRAG);
                            }
                        }
                        playPauseBtnBottom.setImageResource(R.drawable.ic_pause);
                    }else {
                        SharedPreferences.Editor editor = getActivity().getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE).edit();
                        editor.putString(MUSIC_FILE, musicService.listSong.get(musicService.position).getPath());
                        editor.putString(SONG_NAME, musicService.listSong.get(musicService.position).getTitle());
                        editor.putString(ARTIST_NAME, musicService.listSong.get(musicService.position).getArtist());
                        editor.apply();
                        SharedPreferences preferences = getActivity().getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE);
                        String path = preferences.getString(MUSIC_FILE, null);
                        String artist_name = preferences.getString(ARTIST_NAME, null);
                        String song_name = preferences.getString(SONG_NAME, null);
                        if(path != null){
                            SHOW_MINI_PLAYER = true;
                            PATH_TO_FRAG = path;
                            ARTIST_TO_FRAG = artist_name;
                            SONG_NAME_TO_FRAG = song_name;
                        }else {
                            SHOW_MINI_PLAYER = false;
                            PATH_TO_FRAG = null;
                            ARTIST_TO_FRAG = null;
                            SONG_NAME_TO_FRAG = null;
                        }
                        if(SHOW_MINI_PLAYER){
                            if(PATH_TO_FRAG != null){
                                byte[] art = getAlbumArt(PATH_TO_FRAG);
                                if(art != null) {
                                    Glide.with(getContext()).load(art).into(albumArt);
                                }else {
                                    Glide.with(getContext()).load(R.drawable.image_default).into(albumArt);
                                }
                                songName.setText(SONG_NAME_TO_FRAG);
                                artist.setText(ARTIST_TO_FRAG);
                            }
                        }
                        playPauseBtnBottom.setImageResource(R.drawable.ic_pause);
                    }
                }
            }
        });
        playPauseBtnBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(musicService != null){
                    musicService.playPauseClick();
                    if(musicService.isPlaying()){
                        playPauseBtnBottom.setImageResource(R.drawable.ic_pause);
                    }else {
                        playPauseBtnBottom.setImageResource(R.drawable.ic_play);
                    }
                }
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(SHOW_MINI_PLAYER){
            if(PATH_TO_FRAG != null){
                byte[] art = getAlbumArt(PATH_TO_FRAG);
                if(art != null)
                {
                    Glide.with(getContext()).load(art).into(albumArt);
                }else {
                    Glide.with(getContext()).load(R.drawable.image_default).into(albumArt);
                }
                songName.setText(SONG_NAME_TO_FRAG);
                artist.setText(ARTIST_TO_FRAG);
                Intent intent = new Intent(getContext(), MusicService.class);
                if(getContext() != null){
                    getContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(getContext() != null){
            getContext().unbindService(this);
        }
    }

    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        return art;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        MusicService.MyBinder binder = (MusicService.MyBinder) service;
        musicService = binder.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        musicService = null;
    }
}