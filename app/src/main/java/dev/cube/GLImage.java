package dev.cube;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.res.Resources;

public class GLImage {
    public static Bitmap iBitmap;  
    public static Bitmap jBitmap;  
    public static Bitmap kBitmap;  
    /*public static Bitmap lBitmap;  
    public static Bitmap mBitmap;  
    public static Bitmap nBitmap;  
    public static Bitmap close_Bitmap;   */
      
    public static void load(Resources resources)   {  
        iBitmap = BitmapFactory.decodeResource(resources, R.drawable.horse);  
        jBitmap = BitmapFactory.decodeResource(resources, R.drawable.sheep);  
        kBitmap = BitmapFactory.decodeResource(resources, R.drawable.rabbit);  
        //lBitmap = BitmapFactory.decodeResource(resources, R.drawable.lmg);  
        //mBitmap = BitmapFactory.decodeResource(resources, R.drawable.mmg);  
        //nBitmap = BitmapFactory.decodeResource(resources, R.drawable.nmg);  
        //close_Bitmap = BitmapFactory.decodeResource(resources, R.drawable.close);  
    }  
}  
