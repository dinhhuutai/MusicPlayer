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
import hcmute.edu.vn.musicplayer.Activity.PlayerActivity;
import hcmute.edu.vn.musicplayer.Model.Song;
import hcmute.edu.vn.musicplayer.R;

public class AlbumDetailAdapter extends RecyclerView.Adapter<AlbumDetailAdapter.AlbumDetailViewHolder>{

    private Context mContext;
    public static ArrayList<Song> mAlbumFiles;

    public AlbumDetailAdapter(Context context, ArrayList<Song> mAlbumFiles) {
        this.mContext = context;
        this.mAlbumFiles = mAlbumFiles;
    }
    @NonNull
    @Override
    public AlbumDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.song_item, parent, false);
        return new AlbumDetailAdapter.AlbumDetailViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumDetailViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.album_name.setText(mAlbumFiles.get(position).getTitle());
        holder.album_artist.setText(mAlbumFiles.get(position).getArtist());
        byte[] image= getAlbumArt(mAlbumFiles.get(position).getPath());
        Glide.with(mContext).load(image).into(holder.album_image);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, PlayerActivity.class);
                intent.putExtra("sender", "albumDetail");
                intent.putExtra("position", position);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mAlbumFiles.size();
    }
    public class AlbumDetailViewHolder extends RecyclerView.ViewHolder{
        ImageView album_image;
        TextView album_name, album_artist;
        public AlbumDetailViewHolder(@NonNull View itemView) {
            super(itemView);
            album_image = itemView.findViewById(R.id.imgSong);
            album_name = itemView.findViewById(R.id.txtName);
            album_artist = itemView.findViewById(R.id.txtArtist);
        }
    }

    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        return art;
    }
}
