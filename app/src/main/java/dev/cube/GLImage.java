package dev.cube;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.res.Resources;

public class GLImage {
    /*
    public static Bitmap horseBitmap;  
    public static Bitmap sheepBitmap;  
    public static Bitmap dogBitmap;  
    public static Bitmap pigBitmap;  
    public static Bitmap rabbitBitmap;  
    public static Bitmap butterflyBitmap;  
    */
    public static Bitmap[] bitmap;
    public static void load(Resources resources)   {
        bitmap[0] = BitmapFactory.decodeResource(resources, R.drawable.horse);  
        bitmap[1] = BitmapFactory.decodeResource(resources, R.drawable.sheep);  
        bitmap[2] = BitmapFactory.decodeResource(resources, R.drawable.dog);  
        bitmap[3] = BitmapFactory.decodeResource(resources, R.drawable.pig);  
        bitmap[4] = BitmapFactory.decodeResource(resources, R.drawable.rabbit);  
        bitmap[5] = BitmapFactory.decodeResource(resources, R.drawable.butterfly);  
    }  
}  
