package dev.cube;

import android.opengl.GLSurfaceView;
import android.content.Context;
import android.view.MotionEvent;
import java.nio.FloatBuffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import android.graphics.PixelFormat;
import android.util.Log;
import java.io.InputStream;
import java.io.IOException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class MyGLSurfaceView extends GLSurfaceView { 
    // private final float TOUCH_SCALE_FACTOR = 180.0f / 480; 
    private RayPickRenderer mRenderer; 
    private float mPreviousX, mPreviousY; 
 
    public MyGLSurfaceView(Context context, OnSurfacePickedListener onSurfacePickedListener) { 
        super(context); 
        mRenderer = new RayPickRenderer(context); 
        setZOrderOnTop(true); 
        setEGLConfigChooser(8, 8, 8, 8, 16, 0); 
        getHolder().setFormat(PixelFormat.TRANSLUCENT); 
        setRenderer(mRenderer); 
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY); 
        mRenderer.setOnSurfacePickedListener(onSurfacePickedListener); 
    } 
 
    public void onPause() { super.onPause();  } 
    public void onResume() { super.onResume();  } 

    @Override 
    public boolean onTouchEvent(MotionEvent e) { 
        float x = e.getX(); 
        float y = e.getY(); 
        AppConfig.setTouchPosition(x, y); 
        switch (e.getAction()) { 
        case MotionEvent.ACTION_MOVE: 
            float dx = y - mPreviousY; 
            float dy = x - mPreviousX; 
            float d = (float) (Math.sqrt(dx * dx + dy * dy)); 
            mRenderer.mfAngleX = dx; 
            mRenderer.mfAngleY = dy; 
            mRenderer.gesDistance = d; 

            // float dx = x - mPreviousX; 
            // float dy = y - mPreviousY; 
            // mRenderer.mfAngleY += dx * TOUCH_SCALE_FACTOR; 
            // mRenderer.mfAngleX += dy * TOUCH_SCALE_FACTOR; 
            // PickFactory.update(x, y); 
            AppConfig.gbNeedPick = false; 
            break; 
        case MotionEvent.ACTION_DOWN: 
            AppConfig.gbNeedPick = false; 
            break; 
        case MotionEvent.ACTION_UP: 
            AppConfig.gbNeedPick = true; 
            break; 
        case MotionEvent.ACTION_CANCEL: 
            AppConfig.gbNeedPick = false; 
            break; 
        } 
        mPreviousX = x; 
        mPreviousY = y; 
        return true; 
    } 
} 
