package dev.cube;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.widget.Toast;
import android.os.Message;
import android.opengl.GLSurfaceView;
import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;  
import android.widget.SimpleAdapter;
import android.view.View;
import java.util.ArrayList;  
import java.util.HashMap;
import java.util.Timer;
import android.widget.SeekBar;
import android.content.res.AssetFileDescriptor;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import java.util.Timer;  
import java.util.TimerTask;  
import android.app.Activity;        
import java.io.IOException;
import android.media.AudioManager;
import android.content.res.AssetManager;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.view.SurfaceHolder.Callback;

public class RayPickActivity extends Activity implements OnSurfacePickedListener { 
    private MyGLSurfaceView mGLSurfaceView;
    private int prev;
    private int curr;
    private MediaPlayer [] mp; 
    private SeekBar playSeekBar;  

    //private boolean isChanging = false;       // 互斥变量，防止定时器与SeekBar拖动时进度冲突
    private boolean isPlaying = false;
    
    private SurfaceHolder surfaceHolder;
    private Timer mTimer;  
    private TimerTask mTimerTask;
    
    @Override 
    public void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.activity_ray_pick); 
        mGLSurfaceView = (MyGLSurfaceView)findViewById(R.id.myglsurfaceview);
        surfaceHolder = mGLSurfaceView.getHolder();
        //surfaceHolder.addCallback(this);    // added here
        //surfaceHolder.setFixedSize(480, 480); //显示的分辨率,不设置为视频默认 ????
        surfaceHolder.setFixedSize(320, 568); //显示的分辨率,不设置为视频默认 ???? new video size 320*568
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //mGLSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        mGLSurfaceView.requestFocus(); 
        mGLSurfaceView.setFocusableInTouchMode(true);
        GLImage.load(this.getResources());
        mGLSurfaceView.setRendererPickedListener((OnSurfacePickedListener)this); // set OnSurfacePickedListener
        
        prev = -1;
        curr = -1;
        mp = new MediaPlayer[13];    
        
        playSeekBar = (SeekBar) findViewById(R.id.seekbar_play);
        playSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                @Override  
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {  }  
                @Override  
                public void onStartTrackingTouch(SeekBar seekBar) {  
                    isPlaying = true;  
                }  
                @Override  
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (curr != -1) {
                        mp[curr].seekTo(seekBar.getProgress());  
                        isPlaying = false;     
                    }
                } 
            });
        /*
        mTimer = new Timer();  
        mTimerTask = new TimerTask() {  
                @Override  
                public void run() {   
                    if(isPlaying == true) return;
                    if (curr != -1 && mp[curr] != null)
                        playSeekBar.setProgress(mp[curr].getCurrentPosition()); // exception
                }  
            };  
        mTimer.schedule(mTimerTask, 0, 10);
        */
    } 

    private Handler myHandler = new Handler() { 
            @Override 
            public void handleMessage(Message msg) {
                Toast.makeText(RayPickActivity.this, "selected " + msg.what + " surface", Toast.LENGTH_SHORT).show(); 
            } 
        }; 

    @Override
    @SuppressWarnings("deprecation")
    public void onSurfacePicked(int which) {
        myHandler.sendEmptyMessage(which);
        boolean changeSong = false;
        
        if (curr != -1) {
            if (prev == curr) changeSong = true;
            else prev = curr;
            if (mp[curr] != null)
                //mp[curr].stop();
                mp[curr].release();   
            curr = -1;
        }
        if (which != -1) {
            curr = which;
            if (curr != -1 && mp[curr] != null)
                mp[curr].reset(); // 恢复到未初始化的状态  

            switch (curr) {
            case 0:
                mp[curr] = MediaPlayer.create(getApplicationContext(), R.raw.theme);
                break;
            case 1:
                mp[curr] = MediaPlayer.create(getApplicationContext(), R.raw.e1);
                break;
            case 2:
                mp[curr] = MediaPlayer.create(getApplicationContext(), R.raw.e2);
                break;
            case 3: 
                AssetFileDescriptor afd;
                try {
                    afd = getAssets().openFd("ilovemyfamily.mp4");
                    if (mp[curr] == null)
                        mp[curr] = new MediaPlayer();
                    /*
                    mp[curr].setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mp[curr].setOnPreparedListener(this);
                    mp[curr].setOnCompletionListener(this);
                    mp[curr].setOnErrorListener(this);
                    mp[curr].setOnBufferingUpdateListener(this);
                    mp[curr].setOnInfoListener(this);
                    mp[curr].setOnSeekCompleteListener(this);
                    */
                    System.out.println("(mp[curr] == null): " + (mp[curr] == null));

                    mp[curr].setDataSource(afd.getFileDescriptor(),afd.getStartOffset(), afd.getLength());
                    mp[curr].prepareAsync();
                    mp[curr].setOnPreparedListener(new OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                mp.start();
                            }
                        });
                    //mp[curr].setDisplay(surfaceHolder); // 为mediaplayer指定surfaceview显示图像
                } catch (Exception e) {
                    e.printStackTrace();
                }   
                    /*                
                mp[curr].setAudioStreamType(AudioManager.STREAM_MUSIC);  
                mp[curr].setDisplay(surfaceHolder); // 为mediaplayer指定surfaceview显示图像
                    */
                break;
            case 4:
                mp[curr] = MediaPlayer.create(getApplicationContext(), R.raw.e4);
                break;
            case 5:
                mp[curr] = MediaPlayer.create(getApplicationContext(), R.raw.e5);
                break;
            }
            
            try {                     
                mp[curr].prepare();    
            } catch (IllegalStateException e) {           
                e.printStackTrace();                  
            } catch (IOException e) {             
                e.printStackTrace();                  
            }
            System.out.println("before start()");
            mp[curr].start();
            System.out.println("after start()");

            playSeekBar.setMax(mp[curr].getDuration()); // 设置SeekBar的长度

            System.out.println("after getDuration()");

            mp[curr].setOnCompletionListener(new OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.stop();
                        mp.release();
                    }
                });
        }
    }
 
    @Override 
    protected void onResume() { 
        super.onResume(); 
        mGLSurfaceView.onResume();
    } 
 
    @Override 
    protected void onPause() { 
        super.onPause(); 
        mGLSurfaceView.onPause(); 
        if (curr != -1 && mp[curr] != null)
            mp[curr].release();
    }
} 
