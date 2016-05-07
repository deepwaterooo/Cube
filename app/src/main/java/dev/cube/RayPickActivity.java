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
import android.view.View;

public class RayPickActivity extends Activity implements OnSurfacePickedListener { 
    private GLSurfaceView mGLSurfaceView;
    private MediaPlayer [] mp;
    private int prev;
    private int curr;
    
    @Override 
    public void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 
        mGLSurfaceView = new MyGLSurfaceView(this, this); 
        mGLSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT); 
        setContentView(mGLSurfaceView);

        mp = new MediaPlayer[13];
        mp[0] = MediaPlayer.create(getApplicationContext(), R.raw.theme);
        mp[1] = MediaPlayer.create(getApplicationContext(), R.raw.e1);
        mp[2] = MediaPlayer.create(getApplicationContext(), R.raw.e2);
        mp[3] = MediaPlayer.create(getApplicationContext(), R.raw.e3);
        mp[4] = MediaPlayer.create(getApplicationContext(), R.raw.e4);
        mp[5] = MediaPlayer.create(getApplicationContext(), R.raw.e5);
        mp[6] = MediaPlayer.create(getApplicationContext(), R.raw.e6);
        mp[7] = MediaPlayer.create(getApplicationContext(), R.raw.e7);
        mp[8] = MediaPlayer.create(getApplicationContext(), R.raw.e8);
        mp[9] = MediaPlayer.create(getApplicationContext(), R.raw.e9);
        mp[10] = MediaPlayer.create(getApplicationContext(), R.raw.e10);
        mp[11] = MediaPlayer.create(getApplicationContext(), R.raw.e11);
        mp[12] = MediaPlayer.create(getApplicationContext(), R.raw.e12);

        //mp.start();
        //mp.setLooping(true);

        for (int i = 0; i < 13; i++) {
            mp[i].setOnCompletionListener(new OnCompletionListener() {
                    public void onCompletion(MediaPlayer mv) {
                        mv.release();
                    }
                });
        }

        mGLSurfaceView.requestFocus(); 
        mGLSurfaceView.setFocusableInTouchMode(true);
        GLImage.load(this.getResources());
        /*
        mGLSurfaceView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mp.start();
                    //mp.setLooping(true);
                }
            });   
        */
        prev = -1;
        curr = -1;
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
            mp[curr].release();
        }

        System.out.println("which: " + which);

        curr = which;
System.out.println("curr: " + curr);

        mp[curr].start();
    } 
} 
