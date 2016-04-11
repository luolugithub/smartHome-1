package com.demo.smarthome.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import com.demo.smarthome.staticString.StringRes;

/**
 * Created by leishi on 16/4/11.
 */
public class ScreenShotTools {
    /**
     * ���н�ȡ��Ļ
     * @param pActivity
     * @return bitmap
     */
    public static Bitmap takeScreenShot(Activity pActivity)
    {
        Bitmap bitmap=null;
        View view=pActivity.getWindow().getDecorView();
        // �����Ƿ���Խ��л�ͼ����
        view.setDrawingCacheEnabled(true);
        // �����ͼ�����޷���ǿ�ƹ�����ͼ����
        view.buildDrawingCache();
        // �������������ͼ
        bitmap=view.getDrawingCache();

        // ��ȡ״̬���߶�
        Rect frame=new Rect();
        // ������Ļ��͸�
        view.getWindowVisibleDisplayFrame(frame);
        int stautsHeight=frame.top;
        Log.d("jiangqq", "״̬���ĸ߶�Ϊ:"+stautsHeight);

        int width=pActivity.getWindowManager().getDefaultDisplay().getWidth();
        int height=pActivity.getWindowManager().getDefaultDisplay().getHeight();
        // ������������Ҫ�Ŀ�͸ߴ���bitmap
        bitmap=Bitmap.createBitmap(bitmap, 0, stautsHeight, width, height-stautsHeight);
        return bitmap;
    }


    /**
     * ����ͼƬ��sdcard��
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
     * ��ͼ
     * @param pActivity
     * @return ��ͼ���ұ���sdcard�ɹ�����true�����򷵻�false
     */
    public static boolean shotBitmap(Activity pActivity)
    {
        File file=new File(shotFileName());
        /*�ж�file2�ļ���·���������,�������򴴽�*/
        if(!file.exists()){
            new File(file.getParent()).mkdirs();
        }
        return  savePic(takeScreenShot(pActivity), shotFileName());
    }
    //��ͼ���ļ���
    public static String shotFileName(){
        return StringRes.screenShotFileName;
    }

}
