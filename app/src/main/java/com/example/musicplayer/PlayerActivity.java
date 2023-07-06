package com.example.musicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {

    ImageButton playButton, pauseButton, prevButton, nextButton,
                fastForwardButton, fastRewindButton;
    TextView songTextView, startTextView, stopTextView;
    SeekBar musicSeekBar;
    BarVisualizer visualizer;

    ImageView imageView;

    String songName;
    public static final String EXTRA_NAME = "SONG_NAME";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;

    Thread updateSeekBar;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (visualizer != null){
            visualizer.release();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        getSupportActionBar().setTitle("Now Playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        playButton = findViewById(R.id.playButton);
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);
        fastForwardButton = findViewById(R.id.fast_forward_Button);
        fastRewindButton = findViewById(R.id.fast_rewind_Button);

        songTextView = findViewById(R.id.songTextView);
        startTextView = findViewById(R.id.startTextView);
        stopTextView = findViewById(R.id.stopTextView);

        musicSeekBar = findViewById(R.id.seekBar);
        //visualizer = findViewById(R.id.blast);

        imageView = findViewById(R.id.imageView);


        if (mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        Intent i = getIntent();
        Bundle bundle = i.getExtras();

        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        String mySongName = i.getStringExtra("mainSongName");
        bundle.getInt("pos",0);
        songTextView.setSelected(true);
        Uri uri = Uri.parse(mySongs.get(position).toString());
        //String path = mySongs.get(position).getPath();
        songName = mySongs.get(position).getName();

        songTextView.setText(songName);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();

//        mediaPlayer = new MediaPlayer();
//        try {
//            mediaPlayer.setDataSource(path);
//            mediaPlayer.prepare();
//            mediaPlayer.start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


//        playMusic();

        updateSeekBar = new Thread(){
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currentPosition =0;

                while (currentPosition < totalDuration){
                    try {
                        sleep(500);
                        currentPosition = mediaPlayer.getCurrentPosition();
                        musicSeekBar.setProgress(currentPosition);
                    }catch (InterruptedException | IllegalStateException e){
                        e.printStackTrace();
                    }
                }
            }
        };

        musicSeekBar.setMax(mediaPlayer.getDuration());
        updateSeekBar.start();
        musicSeekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.purple_200), PorterDuff.Mode.MULTIPLY);
        musicSeekBar.getThumb().setColorFilter(getResources().getColor(R.color.purple_200), PorterDuff.Mode.SRC_IN);

        musicSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        String endTime = createTime(mediaPlayer.getDuration());
        stopTextView.setText(endTime);

        //Current time update every second, so their is need for an handler for it
        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(mediaPlayer.getCurrentPosition());
                startTextView.setText(currentTime);
                handler.postDelayed(this, delay);
            }
        }, delay);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()){
                    playButton.setBackgroundResource(R.drawable.ic_play);
                    mediaPlayer.pause();
                }else {
                    playButton.setBackgroundResource(R.drawable.ic_pause);
                    mediaPlayer.start();
                }
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                nextButton.performClick();
            }
        });


        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position +1)% mySongs.size());
                Uri u = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
                songName = mySongs.get(position).getName();
                songTextView.setText(songName);
                mediaPlayer.start();
                playButton.setBackgroundResource(R.drawable.ic_pause);
                startAnimation(imageView);

                //Visualizer
                int audioSessionId = mediaPlayer.getAudioSessionId();

                if (audioSessionId != -1){
                    visualizer.setAudioSessionId(audioSessionId);
                }
            }
        });

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = (((position -1)<0) ? (mySongs.size()-1):(position-1));
                Uri u = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
                songName = mySongs.get(position).getName();
                songTextView.setText(songName);
                mediaPlayer.start();
                playButton.setBackgroundResource(R.drawable.ic_pause);
                startAnimation(imageView);

                //Visualizer
                int audioSessionId = mediaPlayer.getAudioSessionId();

                if (audioSessionId != -1){
                    visualizer.setAudioSessionId(audioSessionId);
                }
            }
        });

        fastForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
                }
            }
        });

        fastRewindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
                }
            }
        });
    }

    public void startAnimation(View view){
        ObjectAnimator animator = ObjectAnimator.ofFloat(imageView,"rotation",0f,360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }

    public String createTime(int duration){
        String time = "";

        int min = duration/1000/60;
        int sec = duration/1000%60;

        time += min + ":";

        if (sec <10){
            time +=0;
        }
        time +=sec;
        return time;
    }

//    public void playMusic(){
//
//        new Thread(){
//
//            @Override
//            public void run() {
//
//                if (mediaPlayer != null){
//                    mediaPlayer.stop();
//                    mediaPlayer.release();
//                }
//
//                Intent i = getIntent();
//                Bundle bundle = i.getExtras();
//
//                mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
//                String mySongName = i.getStringExtra("mainSongName");
//                bundle.getInt("pos",0);
//                songTextView.setSelected(true);
//                Uri uri = Uri.parse(mySongs.get(position).toString());
//                songName = mySongs.get(position).getName();
//
//                songTextView.setText(songName);
//
//                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
//                mediaPlayer.start();
//
//
//                playButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        if (mediaPlayer.isPlaying()){
//                            playButton.setBackgroundResource(R.drawable.ic_play);
//                            mediaPlayer.pause();
//                        }else {
//                            playButton.setBackgroundResource(R.drawable.ic_pause);
//                            mediaPlayer.start();
//                        }
//                    }
//                });
////                super.run();
//            }
//        }.start();
//    }
}