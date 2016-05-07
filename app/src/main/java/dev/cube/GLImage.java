package dev.cube;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.res.Resources;

public class GLImage {
    public static Bitmap[] bitmap = new Bitmap[6];

    public static void load(Resources resources)   { // horse sheep dog together
        bitmap[0] = BitmapFactory.decodeResource(resources, R.drawable.horse); // 0
        bitmap[1] = BitmapFactory.decodeResource(resources, R.drawable.butterfly);// 5  
        bitmap[2] = BitmapFactory.decodeResource(resources, R.drawable.dog);   // 2
        bitmap[3] = BitmapFactory.decodeResource(resources, R.drawable.pig);   // 3
        bitmap[4] = BitmapFactory.decodeResource(resources, R.drawable.rabbit);// 4  
        bitmap[5] = BitmapFactory.decodeResource(resources, R.drawable.sheep); // 1
    }  
}  
