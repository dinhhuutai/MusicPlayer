package hcmute.edu.vn.musicplayer.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import hcmute.edu.vn.musicplayer.Adapter.MainViewPagerAdapter;
import hcmute.edu.vn.musicplayer.Fragment.AlbumFragment;
import hcmute.edu.vn.musicplayer.Fragment.SongFragment;
import hcmute.edu.vn.musicplayer.Model.Song;
import hcmute.edu.vn.musicplayer.R;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{

    TabLayout tabLayout;
    ViewPager viewPager;
    public static final int REQUEST_CODE = 1;
    public static ArrayList<Song> songs;
    public static boolean shuffleBoolean = false, repeatBoolean = false;
    public static ArrayList<Song> albums = new ArrayList<>();
    public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static boolean SHOW_MINI_PLAYER = false;
    public static String PATH_TO_FRAG = null;
    public static String ARTIST_TO_FRAG = null;
    public static String SONG_NAME_TO_FRAG = null;

    public static final String ARTIST_NAME = "ARTIST NAME";
    public static final String SONG_NAME = "SONG NAME";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.myViewPager);
        tabLayout = findViewById(R.id.myTabLayout);

        permission();
    }

    private void permission(){
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }else {
            songs = getAllAudio(this);
            initViewPagerAdapter();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                songs = getAllAudio(this);
                initViewPagerAdapter();
            }else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
        }
    }

    private void initViewPagerAdapter() {

        MainViewPagerAdapter mainViewPagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager());
        mainViewPagerAdapter.addFragment(new SongFragment(), "Bài hát");

        mainViewPagerAdapter.addFragment(new AlbumFragment(), "Album");

        viewPager.setAdapter(mainViewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);



        tabLayout.getTabAt(0).setIcon(R.drawable.ic_songs);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_albums);

        // mặc định màu icon tab Home khi chạy ctr
        TabLayout.Tab tab = tabLayout.getTabAt(0);
        tab.getIcon().setTint(ContextCompat.getColor(getApplicationContext(), R.color.teal_200));

        // đổi màu icon khi đc chọn
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.teal_200), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.white), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    public static ArrayList<Song> getAllAudio(Context context){
        ArrayList<String> duplicate = new ArrayList<>();
        ArrayList<Song> tempAudioList = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String [] projection = {
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media._ID

        };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if(cursor != null){
            while (cursor.moveToNext()){
                String album = cursor.getString(0);
                String title = cursor.getString(1);
                String artist = cursor.getString(2);
                String duration = cursor.getString(3);
                String path = cursor.getString(4);
                String id = cursor.getString(5);
                Song song = new Song(id, path, title, album, artist, duration);
                tempAudioList.add(song);
                if(!duplicate.contains(album)){
                    albums.add(song);
                    duplicate.add(album);
                }
            }
            cursor.close();
        }
        return tempAudioList;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE);
        String path = preferences.getString(MUSIC_FILE, null);
        String artist = preferences.getString(ARTIST_NAME, null);
        String song_name = preferences.getString(SONG_NAME, null);
        if(path != null){
            SHOW_MINI_PLAYER = true;
            PATH_TO_FRAG = path;
            ARTIST_TO_FRAG = artist;
            SONG_NAME_TO_FRAG = song_name;
        }else {
            SHOW_MINI_PLAYER = false;
            PATH_TO_FRAG = null;
            ARTIST_TO_FRAG = null;
            SONG_NAME_TO_FRAG = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        String userInput = s.toLowerCase();
        ArrayList<Song> song = new ArrayList<>();
        for (Song song1 : songs){
            if(song1.getTitle().toLowerCase().contains(userInput)){
                song.add(song1);
            }
        }
        SongFragment.mSongAdapter.updateList(song);
        return true;
    }
}