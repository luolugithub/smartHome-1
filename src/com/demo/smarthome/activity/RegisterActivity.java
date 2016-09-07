package com.demo.smarthome.activity;

import com.demo.smarthome.control.ActivityControl;
import com.demo.smarthome.dao.ConfigDao;
import com.demo.smarthome.device.DeviceInformation;
import com.demo.smarthome.server.LoginServer;
import com.demo.smarthome.server.ServerReturnResult;
import com.demo.smarthome.server.setServerURL;
import com.demo.smarthome.service.Cfg;
import com.demo.smarthome.service.ConfigDevice;
import com.demo.smarthome.staticString.StringRes;
import com.demo.smarthome.tools.CheckEmailPhoneTools;
import com.demo.smarthome.R;
import com.demo.smarthome.view.MyDialogView;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.demo.smarthome.service.ConfigService;
import cn.smssdk.SMSSDK;
import cn.smssdk.EventHandler;
import android.util.Log;
import android.os.CountDownTimer;
/**
 * ?????
 * 
 * @author Administrator
 * 
 */
public class RegisterActivity extends Activity {
    final String TAG = "register";
	EditText txtName = null;
	EditText txtPassword = null;
	EditText txtrePassword = null;
    TextView sendCodeAgain = null;
	MyDialogView dialogView;

	AlertDialog.Builder failAlert;

	String userRegName = "";
	String userRegPassword = "";

    //if sending verification code is successful
    boolean isSendCodeSuccessful = false;

	ConfigService dbService;

	final static int REGISTER_SUCCESS 		= 0x10;
	final static int USER_EXISTED  			= 0x11;
	final static int SERVER_EXCEPTION   	= 0x12;
    final static int CODE_ERROR  	        = 0x13;
	final static int REGISTER_FAIL   		= 0x19;

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			dialogView.closeMyDialog();
			super.handleMessage(msg);

			switch (msg.what) {

			case REGISTER_SUCCESS:
				dbService.SaveSysCfgByKey(Cfg.KEY_USER_NAME, userRegName);
				dbService.SaveSysCfgByKey(Cfg.KEY_PASS_WORD, userRegPassword);
				dbService.SaveSysCfgByKey(Cfg.KEY_AUTO_LOGIN , "true");
				Cfg.userName = userRegName;
				Cfg.userPassword = userRegPassword;

				Bundle bundle = new Bundle();
				bundle.putString("activity", "register");
                Intent intent = new Intent();
                intent.putExtras(bundle);
                intent.setClass(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
				break;
			case USER_EXISTED:
				failAlert.setTitle("用户已经存在").setIcon(
                        R.drawable.cloud_fail).setMessage("请重新注册");
				failAlert.create().show();
				break;
            case CODE_ERROR:
                    String codeErrorMessage = (String)msg.obj;
                    failAlert.setTitle("验证码错误").setIcon(
                            R.drawable.cloud_fail).setMessage(codeErrorMessage);
                    failAlert.create().show();
                break;
			default:
				failAlert.setTitle("错误").setIcon(R.drawable.cloud_fail).setMessage("请重新注册");
				failAlert.create().show();
				break;
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); //
		ActivityControl.getInstance().addActivity(this);

		setContentView(R.layout.activity_register);
		TextView title = (TextView) findViewById(R.id.titleRegister);
		title.setClickable(true);
		title.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});

		txtName = (EditText) findViewById(R.id.registerTxtName);
		txtPassword = (EditText) findViewById(R.id.registerTxtPassword);
		txtrePassword = (EditText) findViewById(R.id.againPassword);
        sendCodeAgain = (TextView) findViewById(R.id.sendCodeAgainButton);
        sendCodeAgain.setClickable(true);
        sendCodeAgain.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerifactionCode();
            }
        });

		Button btnSetup = (Button) findViewById(R.id.registerBtnReg);
		btnSetup.setOnClickListener(new BtnRegOnClickListener());
		dbService = new ConfigDao(RegisterActivity.this.getBaseContext());
		failAlert = new AlertDialog.Builder(RegisterActivity.this);
		dialogView = new MyDialogView(RegisterActivity.this);

        //短信验证码
		SMSSDK.initSDK(RegisterActivity.this,StringRes.SMSKEY,StringRes.SMSSECRET);
		SMSSDK.registerEventHandler(eh); //注册短信回调
	}

    /*
    * send verification code
    *
    * */
    private void sendVerifactionCode()
    {
        if (txtName.getText().toString().isEmpty()||(
                !CheckEmailPhoneTools.isPhoneNumber(txtName.getText().toString()))) {
            Toast.makeText(getApplicationContext(), "请输入正确的手机号", Toast.LENGTH_SHORT).show();
            txtName.setFocusable(true);
            return;
        }
        //send Code
        SMSSDK.getVerificationCode(StringRes.ChinaCode, txtName.getText().toString());

        sendCodeAgain.setClickable(false);
        sendCodeAgain.setTextColor(ContextCompat.getColor
                (RegisterActivity.this, R.color.sbc_header_text));
        new CountDownTimer(Cfg.sendVerficationCodeInterval, 1000) {
            public void onTick(long millisUntilFinished) {
                sendCodeAgain.setText("再次发送("+ millisUntilFinished/1000+")");
            }
            public void onFinish() {
                sendCodeAgain.setClickable(true);
                sendCodeAgain.setTextColor(ContextCompat.getColor
                        (RegisterActivity.this, R.color.blue_50));
                sendCodeAgain.setText("发送验证码");
            }
        }.start();
    }

    EventHandler eh=new EventHandler(){

        @Override
        public void afterEvent(int event, int result, Object data) {
            //回调完成
            if (result == SMSSDK.RESULT_COMPLETE) {

                //提交验证码成功
                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                    new registerUserThread().start();
                    //获取验证码成功
                }else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE){
                    isSendCodeSuccessful = true;
                }
            }else{
                ((Throwable)data).printStackTrace();
                if(isSendCodeSuccessful == true)
                {
                    isSendCodeSuccessful = false;
                    Message message = new Message();
                    message.what = CODE_ERROR;
                    message.obj = ((Throwable)data).getMessage();
                    handler.sendMessage(message);
                }

            }
        }
    };
    /**
	 * ??? ?????????
	 * 
	 * @author Administrator
	 * 
	 */
	class BtnRegOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {

			userRegName = txtName.getText().toString();
            String verificationCode = ((EditText)findViewById
                    (R.id.verificationCode)).getText().toString();
			userRegPassword = txtPassword.getText().toString();
			String rePassword = txtrePassword.getText().toString();
            //trim去掉输入时两边的空字符和一些稀奇古怪的字符
            if (verificationCode.trim().isEmpty()) {
                Toast.makeText(getApplicationContext(), "请输入验证码", Toast.LENGTH_SHORT).show();
                txtPassword.setFocusable(true);
                return;
            }
			if (userRegPassword.trim().isEmpty() || (userRegPassword.length() < 6)) {
				Toast.makeText(getApplicationContext(), "密码至少为六位", Toast.LENGTH_SHORT).show();
				txtPassword.setFocusable(true);
				return;
			}

			if(!rePassword.equals(userRegPassword)){
				Toast.makeText(getApplicationContext(), "两次填写的密码不一致", Toast.LENGTH_SHORT).show();
				txtPassword.setFocusable(true);
				return;
			}
            if(isSendCodeSuccessful)
            {
                dialogView.showMyDialog("注册中", "...请等待");
                SMSSDK.submitVerificationCode(StringRes.ChinaCode,userRegName,verificationCode);
            }
            else
            {
                Toast.makeText(getApplicationContext(), "验证码验证失败", Toast.LENGTH_SHORT).show();
            }
		}
	}

	class registerUserThread extends Thread {
		@Override
		public void run() {
			Message message = new Message();
			message.what = REGISTER_FAIL;
            ServerReturnResult getResult = new ServerReturnResult();
            String jsonResult;
			Cfg.currentDeviceType = Cfg.deviceType;
			Gson gson = new Gson();

			String[] paramsName = {"userName", "userPassword"};
			String[] paramsValue = {userRegName,userRegPassword,};


			if((jsonResult = new setServerURL().sendParamToServer("registerUser", paramsName, paramsValue)).isEmpty()){
				message.what = SERVER_EXCEPTION;
				handler.sendMessage(message);
				return;
			}
			try {
				getResult = gson.fromJson(jsonResult
						, ServerReturnResult.class);
			}
			catch (JsonSyntaxException e){
				e.printStackTrace();
			}

			switch (Integer.parseInt(getResult.getCode()))
			{
				case Cfg.CODE_SUCCESS:
					message.what = REGISTER_SUCCESS;
					Cfg.currentDeviceID = "";
                    Cfg.currentDeviceType = "";
					break;
				case Cfg.CODE_USER_EXISTED:
					message.what = USER_EXISTED;
					break;
				default:
					message.what = REGISTER_FAIL;
					break;
			}
			handler.sendMessage(message);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
        //注销短信验证回调函数
        SMSSDK.unregisterEventHandler(eh);
		// 结束Activity&从栈中移除该Activity
		ActivityControl.getInstance().removeActivity(this);
	}
}
