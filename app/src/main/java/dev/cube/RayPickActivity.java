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
        mGLSurfaceView.requestFocus(); 
        mGLSurfaceView.setFocusableInTouchMode(true);
        GLImage.load(this.getResources());

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
            curr = -1;
        }
        //System.out.println("which: " + which);

        if (which != -1) {
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
            case 3:
                mp[curr] = MediaPlayer.create(getApplicationContext(), R.raw.e3);
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
} 
