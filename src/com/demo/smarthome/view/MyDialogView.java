package com.demo.smarthome.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;

/**
 * Created by leishi on 15/12/24.
 * created dialog
 */
public class MyDialogView {
    ProgressDialog dialogView = null;
    boolean showDialogFlag = false;
    public MyDialogView(Context context) {
        dialogView = new ProgressDialog(context);
    }
    public void showMyDialog(String title,String text){
        //不能同时显示两个dialog
        if(dialogView == null || showDialogFlag == true){
            return;
        }
        dialogView.setTitle(title);
        dialogView.setMessage(text);

        dialogView.setCanceledOnTouchOutside(false);
        dialogView.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            }
        });
        dialogView.setButton(DialogInterface.BUTTON_POSITIVE,
                "请等待...", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        dialogView.show();
        dialogView.getButton(DialogInterface.BUTTON_POSITIVE)
                .setEnabled(false);

        dialogView.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return true;
                }
                return false;
            }
        });
        showDialogFlag = true;
    }
    public void closeMyDialog(){
        //关闭dialog前先判断是否关闭
        if(dialogView == null || showDialogFlag == false){
            return;
        }
        dialogView.dismiss();
        showDialogFlag = false;
    }
}
