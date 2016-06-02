package com.demo.smarthome.control;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于保存和处置activity的一个stack
 * Created by leishi on 16/5/11.
 */
public class ActivityControl {
    private ActivityControl(){
    }
    private static ActivityControl instance = new ActivityControl();
    private static List<Activity> activityStack = new ArrayList<Activity>();

    public static ActivityControl getInstance() {
        return instance;
    }

    public void addActivity(Activity aty) {
        activityStack.add(aty);
    }

    public void removeActivity(Activity aty) {
        activityStack.remove(aty);
    }

    /**
     * 结束所有Activity
     */
    public void finishAllActivity() {
        for (int i = 0, size = activityStack.size(); i < size; i++) {
            if (null != activityStack.get(i)) {
                activityStack.get(i).finish();
            }
        }
        activityStack.clear();
    }
}
