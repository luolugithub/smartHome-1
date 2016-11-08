package com.demo.smarthome.tools;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

/**
 * Created by leishi on 2016/10/25.
 */
public class PermissionsCheckerTools {
    /**
     * 检查权限的工具类
     * <p/>
     * 参考https://github.com/SpikeKing/wcl-permission-demo
     */
    private final Context mContext;

    public PermissionsCheckerTools(Context context) {
        mContext = context.getApplicationContext();
    }

    // 判断权限集合
    public boolean lacksPermissions(String... permissions) {
        for (String permission : permissions) {
            if (lacksPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    // 判断是否缺少权限
    private boolean lacksPermission(String permission) {
        return ContextCompat.checkSelfPermission(mContext, permission) ==
                PackageManager.PERMISSION_DENIED;
    }
}
