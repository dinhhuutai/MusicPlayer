package hcmute.edu.vn.musicplayer.Adapter;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.musicplayer.Activity.PlayerActivity;
import hcmute.edu.vn.musicplayer.Model.Song;
import hcmute.edu.vn.musicplayer.R;
import kotlin.Suppress;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private Context mContext;
    public static ArrayList<Song> mSongs;

    public SongAdapter(Context context, ArrayList<Song> mSongs) {
        this.mContext = context;
        this.mSongs = mSongs;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.song_item, parent, false);
        return new SongViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.mTextViewTitle.setText(mSongs.get(position).getTitle());
        holder.mTextViewArtist.setText(mSongs.get(position).getArtist());

        Uri uri = Uri.parse(mSongs.get(position).getPath());
        holder.bind(uri);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, PlayerActivity.class);
                intent.putExtra("position", position);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mSongs.size();
    }

    public class SongViewHolder extends RecyclerView.ViewHolder{

        TextView mTextViewTitle, mTextViewArtist;
        ImageView mImageViewSong;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextViewTitle = itemView.findViewById(R.id.txtName);
            mTextViewArtist = itemView.findViewById(R.id.txtArtist);
            mImageViewSong = itemView.findViewById(R.id.imgSong);
        }
        public void bind(Uri uri) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(uri.toString());
            byte[] art = retriever.getEmbeddedPicture();
            if(art != null){
                Glide.with(itemView.getContext()).load(art).into(mImageViewSong);
            }else {
                Glide.with(itemView.getContext()).load(R.drawable.image_default).into(mImageViewSong);
            }
        }
    }

    public void updateList(ArrayList<Song> songArrayList){
        mSongs = new ArrayList<>();
        mSongs.addAll(songArrayList);
        notifyDataSetChanged();
    }
}
