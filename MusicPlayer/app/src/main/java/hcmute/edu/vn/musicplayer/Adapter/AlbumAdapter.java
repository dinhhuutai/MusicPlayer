package hcmute.edu.vn.musicplayer.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.musicplayer.Activity.AlbumDetailActivity;
import hcmute.edu.vn.musicplayer.Model.Song;
import hcmute.edu.vn.musicplayer.R;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>{

    private Context mContext;
    private ArrayList<Song> mAlbumFiles;

    public AlbumAdapter(Context context, ArrayList<Song> mAlbumFiles) {
        this.mContext = context;
        this.mAlbumFiles = mAlbumFiles;
    }
    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.album_item, parent, false);
        return new AlbumAdapter.AlbumViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumAdapter.AlbumViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.album_name.setText(mAlbumFiles.get(position).getAlbum());
        holder.album_artist.setText(mAlbumFiles.get(position).getArtist());
        byte[] image= getAlbumArt(mAlbumFiles.get(position).getPath());
        Glide.with(mContext).load(image).into(holder.album_image);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, AlbumDetailActivity.class);
                intent.putExtra("albumName", mAlbumFiles.get(position).getAlbum());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mAlbumFiles.size();
    }
    public class AlbumViewHolder extends RecyclerView.ViewHolder{
        ImageView album_image;
        TextView album_name, album_artist;
        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            album_image = itemView.findViewById(R.id.imageViewAlbum);
            album_name = itemView.findViewById(R.id.textViewNameAlbum);
            album_artist = itemView.findViewById(R.id.textViewArtistAlbum);
        }
    }

    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        return art;
    }
}
