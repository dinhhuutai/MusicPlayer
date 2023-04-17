package hcmute.edu.vn.musicplayer.Fragment;

import static hcmute.edu.vn.musicplayer.Activity.MainActivity.songs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.Random;

import hcmute.edu.vn.musicplayer.Adapter.SongAdapter;
import hcmute.edu.vn.musicplayer.R;

public class SongFragment extends Fragment {

    View view;
    RecyclerView mRecyclerView;
    public static SongAdapter mSongAdapter;

    public SongFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_song, container, false);
        mRecyclerView = view.findViewById(R.id.recyclerViewSong);

        mRecyclerView.setHasFixedSize(true);
        if(!(songs.size() < 1)){
            Collections.shuffle(songs, new Random());
            mSongAdapter = new SongAdapter(getContext(), songs);
            mRecyclerView.setAdapter(mSongAdapter);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        }
        return view;
    }
}