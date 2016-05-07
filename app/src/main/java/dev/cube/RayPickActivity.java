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
    private MediaPlayer [] mp;
    private MediaPlayer mpVideo;
    private int prev;
    private int curr;

    // 适配器  
    private SimpleAdapter adapter;  
    // 数据源  
    private ArrayList<HashMap<String, String>> list;   // don't need this much
    // MediaPlay对象  
    private MediaPlayer mediaPlayer;  
    // 当前播放的曲目  
    private int currentPositionMusic = -1;  
    // 刷新SeekBar进度  
    private static final int UPDATE_PROGRESS = 1;  
    // 加载数据  
    private static final int LOADING_DATA = 2;  
    // 播放进度  
    private SeekBar playSeekBar;  

    private boolean isChanging=false;//互斥变量，防止定时器与SeekBar拖动时进度冲突      
    private SurfaceHolder surfaceHolder;
    //private AssetFileDescriptor fd = null;  
    private Timer mTimer;  
    private TimerTask mTimerTask;
    
    @Override 
    public void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 
        //mGLSurfaceView = new MyGLSurfaceView(this, this);
        setContentView(R.layout.activity_ray_pick); 
        mGLSurfaceView = (MyGLSurfaceView)findViewById(R.id.myglsurfaceview);

        surfaceHolder = mGLSurfaceView.getHolder();  
        //surfaceHolder.setFixedSize(100, 100);  
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        //mGLSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);

        mp = new MediaPlayer[13];
        mGLSurfaceView.requestFocus(); 
        mGLSurfaceView.setFocusableInTouchMode(true);
        GLImage.load(this.getResources());

        prev = -1;
        curr = -1;

        playSeekBar = (SeekBar) findViewById(R.id.seekbar_play);

        System.out.println("(playSeekBar == null): " + (playSeekBar == null));
        
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

        //AssetManager manager = getApplicationContext().getAssets();
        //fd = manager.getAssets().openFd("/raw/ilovemyfamily.mp4");  

        //----------定时器记录播放进度---------//  
        mTimer = new Timer();  
        mTimerTask = new TimerTask() {  
                @Override  
                public void run() {   
                    if(isChanging == true)  
                        return;  
                    /*if(m.getVideoHeight() == 0)  
                        skb_audio.setProgress(m.getCurrentPosition());  
                        else   */
                    //playSeekBar.setProgress(mpVideo.getCurrentPosition());      // problem herer
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

        //mp[0].release();
        if (mp[curr] != null)
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
        
        if (curr != -1) {
            prev = curr;
            //mp[curr].release();
            mp[curr].stop();
            curr = -1;
        }

        if (which != -1) {
            mp[curr].reset();//恢复到未初始化的状态  
            mp[curr] = MediaPlayer.create(RayPickActivity.this, R.raw.theme); // 读取音频  
            playSeekBar.setMax(mp[curr].getDuration());                 // 设置SeekBar的长度  
            try {                     
                mp[curr].prepare();    
            } catch (IllegalStateException e) {           
                e.printStackTrace();                  
            } catch (IOException e) {             
                e.printStackTrace();                  
            }         
            mp[curr].start();  //播放
            
            curr = which;
            System.out.println("curr: " + curr);
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
                playSeekBar.setMax(mpVideo.getDuration());      //设置SeekBar的长度  
                mp[curr].setAudioStreamType(AudioManager.STREAM_MUSIC);  
                mp[curr].setDisplay(surfaceHolder);       //设置屏幕  
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
            mp[curr].start();
        }
    }
    /*
    // SeekBar进度改变事件 
    private class SeekBarChangeEvent implements SeekBar.OnSeekBarChangeListener{  
        @Override  
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {  }  

        @Override  
        public void onStartTrackingTouch(SeekBar seekBar) {  
            isChanging=true;  
        }  

        @Override  
        public void onStopTrackingTouch(SeekBar seekBar) {  
            mpVideo.seekTo(seekBar.getProgress());  
            isChanging=false;     
        }  
        } */     
} 
