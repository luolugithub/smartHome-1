package com.demo.smarthome.server;

import android.util.Log;

import com.demo.smarthome.staticString.StringRes;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by leishi on 15/10/22.
 */
public class setServerURL {
    /** webservice 锟斤拷锟斤拷路锟斤拷 */
    private String URL = "";
    /** 锟斤拷锟斤拷锟秸硷拷*/
    String nameSpace = StringRes.nameSpace;

    public setServerURL() {

        String serverIp = StringRes.serverIP;
        String serverPort = StringRes.serverPort;

        URL = "http://" + serverIp + ":" + serverPort
                + "/essh/services/SmartHomeService";
    }

    public String sendParamToServer(String methodName, String[] paramsName,
                                    String[] paramsValue) {
        String ret = "";
        try {
            SoapObject request = new SoapObject(nameSpace, methodName);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);
            if (paramsName != null && !"".equals(paramsName)) {
                for (int i = 0; i < paramsName.length; i++) {
                    request.addProperty(paramsName[i], paramsValue[i]);
                }
            }
            envelope.bodyOut = request;
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE ht = new HttpTransportSE(URL);
            Log.d("setServer","enverlope:"+envelope.toString());
            ht.call("", envelope);
            ret = String.valueOf(envelope.getResponse());
        } catch (SoapFault e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return ret;
    }

}
