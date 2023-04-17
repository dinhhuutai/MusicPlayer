package hcmute.edu.vn.musicplayer.Activity;

import static hcmute.edu.vn.musicplayer.Activity.MainActivity.repeatBoolean;
import static hcmute.edu.vn.musicplayer.Activity.MainActivity.shuffleBoolean;
import static hcmute.edu.vn.musicplayer.Activity.MainActivity.songs;
import static hcmute.edu.vn.musicplayer.Adapter.AlbumDetailAdapter.mAlbumFiles;
import static hcmute.edu.vn.musicplayer.Adapter.SongAdapter.mSongs;
import static hcmute.edu.vn.musicplayer.Fragment.NowPlayingBottomFragment.playPauseBtnBottom;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Random;

import hcmute.edu.vn.musicplayer.ActionPlaying;
import hcmute.edu.vn.musicplayer.Model.Song;
import hcmute.edu.vn.musicplayer.R;
import hcmute.edu.vn.musicplayer.Service.MusicService;

public class PlayerActivity extends AppCompatActivity implements ActionPlaying, ServiceConnection {

    TextView song_name, artist_name, duration_played, duration_total;
    ImageView nextBtn, prevBtn, shuffleBtn, repeatBtn, imageviewSong, imageViewBackgroundPlayer, shareBtn;
    FloatingActionButton playPauseBtn;
    SeekBar seekBar;

    int position = -1;
    public static ArrayList<Song> listSongs = new ArrayList<>();
    public Uri uri;
    private Handler handler = new Handler();
    private Thread playThread, prevThread, nextThread;
    Animation animation;
    MusicService musicService;
    StorageReference storageReference;
    DatabaseReference databaseReference;
    UploadTask uploadTask;
    Song song = new Song();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        init();

        getIntentMethod();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (musicService != null && b) {
                    musicService.seekTo(i * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (!musicService.isPlaying()) {
                    playPauseBtn.setImageResource(R.drawable.ic_pause);
                    musicService.start();
                    seekBar.setMax(musicService.getDuration() / 1000);
                    PlayerActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (musicService != null) {
                                int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                                seekBar.setProgress(mCurrentPosition);
                            }
                            handler.postDelayed(this, 1000);
                        }
                    });
                    imageviewSong.startAnimation(animation);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (musicService != null) {
                    int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                    duration_played.setText(formattedTime(mCurrentPosition));
                }
                handler.postDelayed(this, 1000);
            }
        });

        animation = AnimationUtils.loadAnimation(this, R.anim.disc_rotate); // xoay

        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (shuffleBoolean) {
                    shuffleBoolean = false;
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle_off);
                } else {
                    shuffleBoolean = true;
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);
                }
            }
        });

        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (repeatBoolean) {
                    repeatBoolean = false;
                    repeatBtn.setImageResource(R.drawable.ic_repeat_off);
                } else {
                    repeatBoolean = true;
                    repeatBtn.setImageResource(R.drawable.ic_repeat_on);
                }
            }
        });

        storageReference = FirebaseStorage.getInstance().getReference("Song");
        databaseReference = FirebaseDatabase.getInstance().getReference("songs");

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadSong();
            }
        });
    }

    private String getExt(Uri uri){
        String mimeType = null;
        try {
            URLConnection connection = new URL(uri.toString()).openConnection();
            mimeType = connection.getContentType();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mimeType != null) {
            return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        } else {
            return null;
        }
    }

    private void uploadSong() {
        String path = listSongs.get(position).getPath();
        String localFilePath = path;
        Uri fileUri = Uri.fromFile(new File(localFilePath));

        String title = listSongs.get(position).getTitle();
        String album = listSongs.get(position).getAlbum();
        String artist = listSongs.get(position).getArtist();
        String duration = listSongs.get(position).getDuration();

        if (musicService != null) {
            final StorageReference reference = storageReference.child(System.currentTimeMillis() + "." + getExt(fileUri));
            System.out.println("Tuan" + reference);
            uploadTask = reference.putFile(fileUri);
            Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return reference.getDownloadUrl();
                }
            })
                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if(task.isSuccessful()){
                                Uri downloadUri = task.getResult();
                                Toast.makeText(PlayerActivity.this, "Upload successful", Toast.LENGTH_LONG).show();

                                song.setPath(downloadUri.toString());
                                song.setTitle(title);
                                song.setAlbum(album);
                                song.setArtist(artist);
                                song.setDuration(duration);
                                String songId = databaseReference.push().getKey();
                                song.setId(songId);
                                databaseReference.child(songId).setValue(song);
                            }else {
                                Toast.makeText(PlayerActivity.this, "Failed", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }else {
            Toast.makeText(this, "All Fields are required", Toast.LENGTH_LONG).show();
        }
    }

    private void init() {
        song_name = findViewById(R.id.song_name);
        artist_name = findViewById(R.id.song_artist);
        duration_played = findViewById(R.id.durationPlayed);
        duration_total = findViewById(R.id.durationTotal);
        playPauseBtn = findViewById(R.id.btnPlay);
        nextBtn = findViewById(R.id.btnNext);
        prevBtn = findViewById(R.id.btnPrev);
        shuffleBtn = findViewById(R.id.btnShuffle);
        repeatBtn = findViewById(R.id.btnRepeat);
        seekBar = findViewById(R.id.seekbar);
        imageviewSong = findViewById(R.id.imageviewSong);
        imageViewBackgroundPlayer = findViewById(R.id.imageViewBackgroundPlayer);
        shareBtn = findViewById(R.id.btnShare);
    }

    @Override
    protected void onResume() {
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
        playThreadBtn();
        nextThreadBtn();
        prevThreadBtn();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    private void playThreadBtn() {
        playThread = new Thread() {
            @Override
            public void run() {
                super.run();
                playPauseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        playPauseBtnClicked();
                    }
                });
            }
        };
        playThread.start();
    }

    public void playPauseBtnClicked() {
        if (musicService.isPlaying()) {
            playPauseBtn.setImageResource(R.drawable.ic_play);
            musicService.sendNotificationMedia(R.drawable.ic_play);
            musicService.pause();
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            imageviewSong.clearAnimation();
        } else {
            musicService.sendNotificationMedia(R.drawable.ic_pause);
            playPauseBtn.setImageResource(R.drawable.ic_pause);
            musicService.start();
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            imageviewSong.startAnimation(animation);
        }
    }

    private void nextThreadBtn() {
        nextThread = new Thread() {
            @Override
            public void run() {
                super.run();
                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        nextBtnClicked();
                    }
                });
            }
        };
        nextThread.start();
    }

    public void nextBtnClicked() {
        musicService.stop();
        musicService.release();
        if (shuffleBoolean && !repeatBoolean) {
            position = getRandom(listSongs.size() - 1);
        } else if (!shuffleBoolean && !repeatBoolean) {
            position = ((position + 1) % listSongs.size());
        }
        uri = Uri.parse(listSongs.get(position).getPath());
        musicService.createMediaPlayer(position);
        metaData(uri);
        song_name.setText(listSongs.get(position).getTitle());
        artist_name.setText(listSongs.get(position).getArtist());
        duration_total.setText(listSongs.get(position).getDuration());
        seekBar.setMax(musicService.getDuration() / 1000);
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (musicService != null) {
                    int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                }
                handler.postDelayed(this, 1000);
            }
        });
        musicService.sendNotificationMedia(R.drawable.ic_pause);
        playPauseBtn.setImageResource(R.drawable.ic_pause);
        musicService.start();
        musicService.OnCompleted();
        imageviewSong.startAnimation(animation);
    }

    private void prevThreadBtn() {
        prevThread = new Thread() {
            @Override
            public void run() {
                super.run();
                prevBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        prevBtnClicked();
                    }
                });
            }
        };
        prevThread.start();
    }

    public void prevBtnClicked() {
        musicService.stop();
        musicService.release();
        if (shuffleBoolean && !repeatBoolean) {
            position = getRandom(listSongs.size() - 1);
        } else if (!shuffleBoolean && !repeatBoolean) {
            position = ((position - 1) < 0 ? (listSongs.size() - 1) : (position - 1));
        }
        uri = Uri.parse(listSongs.get(position).getPath());
        musicService.createMediaPlayer(position);
        metaData(uri);
        song_name.setText(listSongs.get(position).getTitle());
        artist_name.setText(listSongs.get(position).getArtist());
        duration_total.setText(listSongs.get(position).getDuration());
        seekBar.setMax(musicService.getDuration() / 1000);
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (musicService != null) {
                    int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                }
                handler.postDelayed(this, 1000);
            }
        });
        musicService.OnCompleted();
        playPauseBtn.setImageResource(R.drawable.ic_pause);
        musicService.sendNotificationMedia(R.drawable.ic_pause);
        musicService.start();
        imageviewSong.startAnimation(animation);
    }

    private String formattedTime(int mCurrentPosition) {
        String totalout = "";
        String totalnew = "";
        String seconds = String.valueOf(mCurrentPosition % 60);
        String minutes = String.valueOf(mCurrentPosition / 60);
        totalout = minutes + ":" + seconds;
        totalnew = minutes + ":" + "0" + seconds;
        if (seconds.length() == 1) {
            return totalnew;
        } else {
            return totalout;
        }
    }

    public void getIntentMethod() {
        position = getIntent().getIntExtra("position", -1);
        String sender = getIntent().getStringExtra("sender");
        if(sender!=null && sender.equals("albumDetail")){
            listSongs = mAlbumFiles;
        }else {
            listSongs = mSongs;
        }
        if (listSongs != null) {
            playPauseBtn.setImageResource(R.drawable.ic_pause);
            uri = Uri.parse(listSongs.get(position).getPath());
        }
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra("servicePosition", position);
        startService(intent);
    }

    // mờ ảnh phía sau
    private void metaData(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        byte[] art = retriever.getEmbeddedPicture();
        Bitmap bitmap;
        if (art != null) {
            if (!isFinishing() && !isDestroyed()) {
                Glide.with(this).load(art).into(imageviewSong);
            }
            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);

            RenderScript rs = RenderScript.create(this);
            Allocation input = Allocation.createFromBitmap(rs, bitmap);
            Allocation output = Allocation.createTyped(rs, input.getType());
            ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            blurScript.setInput(input);

            float radius = 25.0f;
            blurScript.setRadius(radius);
            blurScript.forEach(output);

            output.copyTo(bitmap);

            imageViewBackgroundPlayer.setImageBitmap(bitmap);

            input.destroy();
            output.destroy();
            blurScript.destroy();
            rs.destroy();
        }
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        MusicService.MyBinder myBinder = (MusicService.MyBinder) service;
        musicService = myBinder.getService();
        musicService.setCallBack(this);
        seekBar.setMax(musicService.getDuration() / 1000);
        metaData(uri);
        song_name.setText(listSongs.get(position).getTitle());
        artist_name.setText(listSongs.get(position).getArtist());
        duration_total.setText(listSongs.get(position).getDuration());
        musicService.OnCompleted();
        imageviewSong.startAnimation(animation);
        musicService.sendNotificationMedia(R.drawable.ic_pause);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        musicService = null;
    }

    @Override
    public void onBackPressed() {
        if(musicService.isPlaying()){
            playPauseBtnBottom.setImageResource(R.drawable.ic_pause);
        }else {
            playPauseBtnBottom.setImageResource(R.drawable.ic_play);
        }
        super.onBackPressed();
    }

    public void clearBtnClicked(){
        Intent intent = new Intent(this, MusicService.class);
        stopService(intent);
    }
}