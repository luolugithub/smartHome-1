package com.demo.smarthome.control;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.demo.smarthome.service.Cfg;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 *  初始化手机一些硬件配置
 * Created by leishi on 16/5/11.
 */
public class initPhoneConfig {
    initPhoneConfig(){
    }
    //手机屏幕大小,不算下面的的虚拟按键
    public static void initPhoneScreen(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        Cfg.widthPixels = dm.widthPixels;
        Cfg.heightPixels = dm.heightPixels;
    }
    //判断屏幕下方是否有虚拟按键
    public static boolean IsNavigationBar(Context context) {
        //Android 5.0以下没有虚拟按键
//        if(android.os.Build.VERSION.SDK_INT<21) {
//            return false;
//        }
        //fuck meizu!!!
        final boolean isMeiZu = Build.MANUFACTURER.equals("Meizu");
        if (isMeiZu) {
            if (Settings.System.getInt(context.getContentResolver(),
                    "mz_smartbar_auto_hide", 0) == 1) {
                return false;
            } else {
                if(android.os.Build.VERSION.SDK_INT<22) {
                    return true;
                }else {
                    return false;
                }
            }
        }
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            return rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            @SuppressWarnings("unchecked")
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                return false;
            } else if ("0".equals(navBarOverride)) {
                return true;
            }
        } catch (Exception e) {

        }
        return false;
    }
    //获取NavigationBar的高度：
    public static int getNavigationBarHeight(Context context) {
        //fuck meizu!!!
        final boolean isMeiZu = Build.MANUFACTURER.equals("Meizu");
        final boolean autoHideSmartBar = Settings.System.getInt(context.getContentResolver(),
                "mz_smartbar_auto_hide", 0) == 1;

        if (isMeiZu) {
            if (autoHideSmartBar) {
                return 0;
            } else {
                try {
                    Class c = Class.forName("com.android.internal.R$dimen");
                    Object obj = c.newInstance();
                    Field field = c.getField("mz_action_button_min_height");
                    int height = Integer.parseInt(field.get(obj).toString());
                    return context.getResources().getDimensionPixelSize(height);
                } catch (Throwable e) { // 不自动隐藏smartbar同时又没有smartbar高度字段供访问，取系统navigationbar的高度
                }
            }
        }

        int navigationBarHeight = 0;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("navigation_bar_height", "dimen", "android");
        if (id > 0 && IsNavigationBar(context)) {
            navigationBarHeight = rs.getDimensionPixelSize(id);
        }
        return navigationBarHeight;
    }
}
