package hcmute.edu.vn.musicplayer.Fragment;

import static hcmute.edu.vn.musicplayer.Activity.MainActivity.albums;
import static hcmute.edu.vn.musicplayer.Activity.MainActivity.songs;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hcmute.edu.vn.musicplayer.Adapter.AlbumAdapter;
import hcmute.edu.vn.musicplayer.Adapter.SongAdapter;
import hcmute.edu.vn.musicplayer.R;
public class AlbumFragment extends Fragment {

    View view;
    RecyclerView mRecyclerView;
    AlbumAdapter mAlbumAdapter;
    public AlbumFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_album, container, false);
        mRecyclerView = view.findViewById(R.id.recyclerViewAlbum);
        mRecyclerView.setHasFixedSize(true);
        if(!(albums.size() < 1)){
            mAlbumAdapter = new AlbumAdapter(getContext(), albums);
            mRecyclerView.setAdapter(mAlbumAdapter);
            mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        }
        return view;
    }

}