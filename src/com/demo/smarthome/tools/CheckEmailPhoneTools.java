package com.demo.smarthome.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by leishi on 15/11/17.
 */
public class CheckEmailPhoneTools {
    //验证是否为有效的邮箱名
    public static boolean isEmail(String email){
        String str="^([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)" +
                "*@([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)+[\\.][A-Za-z]{2,3}([\\.][A-Za-z]{2})?$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);
        return m.matches();
    }
    //验证是否是有效手机号
    public static boolean isPhoneNumber(String mobiles){
        Pattern p = Pattern.compile("^((13[0-9])|(15[0-9])|(17[0-9])|(18[0-9]))\\d{8}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }
}
