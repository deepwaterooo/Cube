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
//import android.content.res.AssetFileDescriptor;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import java.util.Timer;  
import java.util.TimerTask;  
import android.app.Activity;        
import java.io.IOException;
import android.media.AudioManager;
import android.content.res.AssetManager;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class RayPickActivity extends Activity implements OnSurfacePickedListener { 
    private MyGLSurfaceView mGLSurfaceView;
    private int prev;
    private int curr;
    private MediaPlayer [] mp;
    private MediaPlayer mpVideo;
    private SeekBar playSeekBar;  

    // 适配器  
    private SimpleAdapter adapter;  
    //private ArrayList<HashMap<String, String>> list;   // don't need this much
    // 当前播放的曲目  
    private int currentPositionMusic = -1;  
    // 刷新SeekBar进度  
    private static final int UPDATE_PROGRESS = 1;  
    // 加载数据  
    private static final int LOADING_DATA = 2;  

    private boolean isChanging = false;       // 互斥变量，防止定时器与SeekBar拖动时进度冲突      
    private SurfaceHolder surfaceHolder;
    private Timer mTimer;  
    private TimerTask mTimerTask;
    
    @Override 
    public void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.activity_ray_pick); 
        mGLSurfaceView = (MyGLSurfaceView)findViewById(R.id.myglsurfaceview);
        surfaceHolder = mGLSurfaceView.getHolder();  
        surfaceHolder.setFixedSize(480, 480);  
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //mGLSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        mGLSurfaceView.requestFocus(); 
        mGLSurfaceView.setFocusableInTouchMode(true);
        
        GLImage.load(this.getResources());

        prev = -1;
        curr = -1;
        mp = new MediaPlayer[13];    // I guess I need only one
        mpVideo = new MediaPlayer();
        mpVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){  
                @Override  
                public void onCompletion(MediaPlayer arg0) {  
                    Toast.makeText(RayPickActivity.this, "ENDED!", 1000).show();  
                    mpVideo.release();  
                }  
            });
        
        playSeekBar = (SeekBar) findViewById(R.id.seekbar_play);
        playSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                @Override  
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {  }  
                @Override  
                public void onStartTrackingTouch(SeekBar seekBar) {  
                    isChanging = true;  
                }  
                @Override  
                public void onStopTrackingTouch(SeekBar seekBar) {  
                    //mpVideo.seekTo(seekBar.getProgress());  
                    //isChanging = false;     
                } 
            });

        //----------定时器记录播放进度---------//  
        mTimer = new Timer();  
        mTimerTask = new TimerTask() {  
                @Override  
                public void run() {   
                    if(isChanging == true) return;
                    if (curr == 3)
                        playSeekBar.setProgress(mpVideo.getCurrentPosition()); // audio video, I am using the same
                    else if (curr != -1 && mp[curr].isPlaying())
                        playSeekBar.setProgress(mp[curr].getCurrentPosition()); // audio video, I am using the same
                }  
            };  
        mTimer.schedule(mTimerTask, 0, 10);
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
    private Handler myHandler = new Handler() { 
            @Override 
            public void handleMessage(Message msg) { 
                Toast.makeText(RayPickActivity.this, "selected " + msg.what + " surface", Toast.LENGTH_SHORT).show(); 
            } 
        }; 
 
    @Override 
    public void onSurfacePicked(int which) { 
        myHandler.sendEmptyMessage(which);
        boolean changeSong = false;
        boolean stopped = false;
        
        if (curr != -1) {
            if (prev == curr) changeSong = true;
            else prev = curr;
            mp[curr].stop();
            stopped = true;
            //mp[curr].release();   // added this morning, not sure if I should release here
            curr = -1;
        }
        if (which != -1) {
            curr = which;
            System.out.println("curr: " + curr);   //debugging propose only
            mp[curr].reset(); // 恢复到未初始化的状态  

            switch (which) {
            case 0:
                mp[curr] = MediaPlayer.create(getApplicationContext(), R.raw.theme);
                break;
            case 1:
                mp[curr] = MediaPlayer.create(getApplicationContext(), R.raw.e1);
                break;
            case 2:
                mp[curr] = MediaPlayer.create(getApplicationContext(), R.raw.e2);
                break;
            case 3: // pig
                //mp[curr] = MediaPlayer.create(getApplicationContext(), R.raw.e3); // e3

                //AssetManager manager = getApplicationContext().getAssets();
                //fd = manager.getAssets().openFd("/raw/ilovemyfamily.mp4");  
                //mediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
                
                mp[curr].reset();
                mp[curr] = MediaPlayer.create(RayPickActivity.this, R.raw.ilovemyfamily); // 读取音频  
                playSeekBar.setMax(mpVideo.getDuration());                                // 设置SeekBar的长度  
                mp[curr].setAudioStreamType(AudioManager.STREAM_MUSIC);  
                mp[curr].setDisplay(surfaceHolder);                                       // 设置屏幕  
                try {  
                    mp[curr].prepare();  
                } catch (IllegalArgumentException e) {  
                    e.printStackTrace();  
                } catch (IllegalStateException e) {  
                    e.printStackTrace();  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
                mp[curr].start();                  
                break;
            case 4:
                mp[curr] = MediaPlayer.create(getApplicationContext(), R.raw.e4);
                break;
            case 5:
                mp[curr] = MediaPlayer.create(getApplicationContext(), R.raw.e5);
                break;
            }

            playSeekBar.setMax(mp[curr].getDuration());                       // 设置SeekBar的长度  
            try {                     
                mp[curr].prepare();    
            } catch (IllegalStateException e) {           
                e.printStackTrace();                  
            } catch (IOException e) {             
                e.printStackTrace();                  
            }         
            mp[curr].start();  // 播放
        }
    }
} 
