package com.demo.smarthome.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import com.demo.smarthome.staticString.StringRes;

/**
 * Created by leishi on 16/4/11.
 */
public class ScreenShotTools {
    /**
     *
     * @param pActivity
     * @return bitmap
     */
    public static Bitmap takeScreenShot(Activity pActivity)
    {
        Bitmap bitmap=null;
        View view=pActivity.getWindow().getDecorView();
        //
        view.setDrawingCacheEnabled(true);
        //
        view.buildDrawingCache();
        //
        bitmap=view.getDrawingCache();

        //
        Rect frame=new Rect();
        //
        view.getWindowVisibleDisplayFrame(frame);
        int stautsHeight=frame.top;
        Point outSize = new Point();
        int width,height;
        pActivity.getWindowManager().getDefaultDisplay().getSize(outSize);
        width = outSize.x;
        height = outSize.y;
        bitmap=Bitmap.createBitmap(bitmap, 0, stautsHeight, width, height-stautsHeight);
        return bitmap;
    }


    /**
     *
     * @param pBitmap
     */
    private static boolean savePic(Bitmap pBitmap,String strName) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(strName);
            if (null != fos) {
                pBitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.flush();
                fos.close();
                return true;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     *
     * @param pActivity
     * @return
     */
    public static boolean shotBitmap(Activity pActivity)
    {
        File file=new File(shotFileName());

        if(!file.exists()){
            new File(file.getParent()).mkdirs();
        }
        return  savePic(takeScreenShot(pActivity), shotFileName());
    }
    //
    public static String shotFileName(){
        return StringRes.screenShotFileName;
    }

}
