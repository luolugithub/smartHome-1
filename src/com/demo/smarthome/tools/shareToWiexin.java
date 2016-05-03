package com.demo.smarthome.tools;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by leishi on 16/4/11.
 *
 */
public class shareToWiexin {

    static int shareSucceed     = 0;
    static int fileNotExist     = 1;
    static int screenShotFail   = 2;

    public static int shareToWeiXinTimeline(Activity context){

        if(!ScreenShotTools.shotBitmap(context)){
            return screenShotFail;
        }

        ArrayList<Uri> uris = new ArrayList<>();
        File shotFile = new File(ScreenShotTools.shotFileName());
        if(!shotFile.exists()){
            return fileNotExist;
        }

        Intent intent = new Intent();
        ComponentName comp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI");
        intent.setComponent(comp);
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("image/*");

        intent.putExtra("Kdescription", "");
        uris.add(Uri.fromFile(shotFile));
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        context.startActivity(intent);

        return shareSucceed;
    }
    /**
     *
     *
     */
    private static int shareToFriend(Activity context) {

        if(!ScreenShotTools.shotBitmap(context)){
            return screenShotFail;
        }

        ArrayList<Uri> uris = new ArrayList<>();
        File shotFile = new File(ScreenShotTools.shotFileName());
        if(!shotFile.exists()){
            return fileNotExist;
        }

        Intent intent = new Intent();
        ComponentName comp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI");
        intent.setComponent(comp);
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, uris);
        context.startActivity(intent);
        return shareSucceed;
    }
}
