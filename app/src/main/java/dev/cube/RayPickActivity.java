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
    private MediaPlayer mp;
    
    @Override 
    public void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 
        mGLSurfaceView = new MyGLSurfaceView(this, this); 
        mGLSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT); 
        setContentView(mGLSurfaceView);

        mp = MediaPlayer.create(getApplicationContext(), R.raw.theme);
        mp.start();
        mp.setOnCompletionListener(new OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }
            });

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
        //mp.release();
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
    } 
} 
