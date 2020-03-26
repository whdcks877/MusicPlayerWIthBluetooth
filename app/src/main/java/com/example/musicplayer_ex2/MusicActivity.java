package com.example.musicplayer_ex2;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MusicActivity extends AppCompatActivity implements View.OnClickListener {

    private ArrayList<MusicDto> list;
    private MediaPlayer mediaPlayer;
    private TextView title;
    private ImageView album,previous,play,pause,next;
    private SeekBar seekBar;
    boolean isPlaying = true;
    private ContentResolver res;
    private ProgressUpdate progressUpdate;
    private int position;
    private TextView receivedata;
    String receiv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        Intent intent = getIntent();
        mediaPlayer = new MediaPlayer();
        title = (TextView)findViewById(R.id.title);
        album = (ImageView)findViewById(R.id.album);
        seekBar = (SeekBar)findViewById(R.id.seekbar);

        position = intent.getIntExtra("position",0);
        list = (ArrayList<MusicDto>) intent.getSerializableExtra("playlist");
        res = getContentResolver();

        previous = (ImageView)findViewById(R.id.pre);
        play = (ImageView)findViewById(R.id.play);
        pause = (ImageView)findViewById(R.id.pause);
        next = (ImageView)findViewById(R.id.next);

        receivedata = (TextView)findViewById(R.id.RecivedData) ;

        previous.setOnClickListener(this);
        play.setOnClickListener(this);
        pause.setOnClickListener(this);
        next.setOnClickListener(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadCastReceiver, new IntentFilter("BTR"));

        playMusic(list.get(position));
        progressUpdate = new ProgressUpdate();
        progressUpdate.start();

        Intent BTintent = new Intent(MusicActivity.this, BTService.class);
        bindService(BTintent, conn1, Context.BIND_AUTO_CREATE);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mediaPlayer.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
                if(seekBar.getProgress()>0 && play.getVisibility()==View.GONE){
                    mediaPlayer.start();
                }
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                if(position+1<list.size()) {
                    position++;
                    playMusic(list.get(position));
                }
            }
        });
    }

    BTService ms1;
    boolean isService1 = false;
    ServiceConnection conn1 = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BTService.MyBinder mb1 = (BTService.MyBinder) service;
            ms1 = mb1.getService();

            isService1 = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isService1 = false;
        }
    };


    public void playMusic(MusicDto musicDto) {

        try {
            seekBar.setProgress(0);
            title.setText(musicDto.getArtist()+" - "+musicDto.getTitle());
            Uri musicURI = Uri.withAppendedPath(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, ""+musicDto.getId());
            mediaPlayer.reset();
            mediaPlayer.setDataSource(this, musicURI);
            mediaPlayer.prepare();
            mediaPlayer.start();
            seekBar.setMax(mediaPlayer.getDuration());
            if(mediaPlayer.isPlaying()){
                play.setVisibility(View.GONE);
                pause.setVisibility(View.VISIBLE);
            }else{
                play.setVisibility(View.VISIBLE);
                pause.setVisibility(View.GONE);
            }


            Bitmap bitmap = BitmapFactory.decodeFile(getCoverArtPath(Long.parseLong(musicDto.getAlbumId()),getApplication()));
            album.setImageBitmap(bitmap);

        }
        catch (Exception e) {
            Log.e("SimplePlayer", e.getMessage());
        }
    }

    //앨범이 저장되어 있는 경로를 리턴합니다.
    private static String getCoverArtPath(long albumId, Context context) {

        Cursor albumCursor = context.getContentResolver().query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums._ID + " = ?",
                new String[]{Long.toString(albumId)},
                null
        );
        boolean queryResult = albumCursor.moveToFirst();
        String result = null;
        if (queryResult) {
            result = albumCursor.getString(0);
        }
        albumCursor.close();
        return result;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.play:
                pause.setVisibility(View.VISIBLE);
                play.setVisibility(View.GONE);
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
                mediaPlayer.start();

                break;
            case R.id.pause:
                pause.setVisibility(View.GONE);
                play.setVisibility(View.VISIBLE);
                mediaPlayer.pause();
                break;
            case R.id.pre:
                if(position-1>=0 ){
                    position--;
                    playMusic(list.get(position));
                    seekBar.setProgress(0);
                }
                break;
            case R.id.next:
                if(position+1<list.size()){
                    position++;
                    playMusic(list.get(position));
                    seekBar.setProgress(0);
                }

                break;
        }
    }


    class ProgressUpdate extends Thread{
        @Override
        public void run() {
            while(isPlaying){
                try {
                    Thread.sleep(500);
                    if(mediaPlayer!=null){
                        seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    }
                } catch (Exception e) {
                    Log.e("ProgressUpdate",e.getMessage());
                }

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isPlaying = false;
        if(mediaPlayer!=null){
            mediaPlayer.release();
            mediaPlayer = null;
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadCastReceiver);
    }
    private BroadcastReceiver mBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            receiv = "";
            receiv = intent.getStringExtra("data");
            receiv = receiv.trim();
            if(receiv.equals("play".toString())){
                //sss
                pause.setVisibility(View.VISIBLE);
                play.setVisibility(View.GONE);
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
                mediaPlayer.start();
            } else if(receiv.equals("pause")){
                pause.setVisibility(View.GONE);
                play.setVisibility(View.VISIBLE);
                mediaPlayer.pause();
            } else if(receiv.equals("prev")){
                if(position-1>=0 ){
                    position--;
                    playMusic(list.get(position));
                    seekBar.setProgress(0);
                }
            } else if(receiv.equals("next")){
                if(position+1<list.size()){
                    position++;
                    playMusic(list.get(position));
                    seekBar.setProgress(0);
                }
            }
            receivedata.setText("");
            receivedata.setText(receiv);

        }
    };
}