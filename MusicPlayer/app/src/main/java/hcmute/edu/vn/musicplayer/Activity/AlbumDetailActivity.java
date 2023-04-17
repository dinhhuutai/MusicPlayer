package hcmute.edu.vn.musicplayer.Activity;

import static hcmute.edu.vn.musicplayer.Activity.MainActivity.songs;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import hcmute.edu.vn.musicplayer.Adapter.AlbumDetailAdapter;
import hcmute.edu.vn.musicplayer.Model.Song;
import hcmute.edu.vn.musicplayer.R;

public class AlbumDetailActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageView albumPhoto;
    TextView album_name, artist_name;
    String albumName;
    ArrayList<Song> albumSongs = new ArrayList<>();
    AlbumDetailAdapter albumDetailAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_detail);
        recyclerView = findViewById(R.id.recyclerView);
        albumPhoto = findViewById(R.id.albumPhoto);
        album_name = findViewById(R.id.album_name);
        artist_name = findViewById(R.id.artist_name);
        albumName = getIntent().getStringExtra("albumName");
        int j = 0;
        for (int i = 0; i<songs.size(); i++){
            if(albumName.equals(songs.get(i).getAlbum())){
                albumSongs.add(j, songs.get(i));
                j++;
            }
        }
        album_name.setText(albumSongs.get(0).getAlbum());
        artist_name.setText(albumSongs.get(0).getArtist());
        byte[] image = getAlbumArt(albumSongs.get(0).getPath());
        Glide.with(this).load(image).into(albumPhoto);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!(albumSongs.size() < 1)){
            albumDetailAdapter = new AlbumDetailAdapter(this, albumSongs);
            recyclerView.setAdapter(albumDetailAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        }
    }

    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        return art;
    }
}