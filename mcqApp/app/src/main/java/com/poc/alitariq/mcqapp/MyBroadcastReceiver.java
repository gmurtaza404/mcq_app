package com.poc.alitariq.mcqapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;

/**
 * Created by ali tariq on 13/03/2018.
 */
public class MyBroadcastReceiver extends BroadcastReceiver
{
    TelephonyManager tm;
    String extra_foreground_call_state;
    String sdCardRoot = Environment.getExternalStorageDirectory().toString()+ "/testing/";
    String last_State = "none";
    long time_stamp = 0;
    final SmsManager sms = SmsManager.getDefault();

    @Override
    public void onReceive(Context context, final Intent intent)
    {
        final Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {
                final Object[] pdusObj = (Object[]) bundle.get("pdus");
                String sender = "";
                for (int i = 0; i < pdusObj.length; i++) {
                    SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    String phoneNumber = sms.getDisplayOriginatingAddress();
                    sender = phoneNumber;
                    String message = sms.getDisplayMessageBody();
                    String temp_str = readFromFile("mcq_menuChoice");
                    temp_str = temp_str + '\n' + System.currentTimeMillis();
                    writeToFile("mcq_menuChoice", temp_str);

                    System.out.println("Msg received: ("+sender+"):"+ message);
                    String[] msg = message.split(":");
                    if(msg[0].compareTo("mcqApp")==0) {
                        MainActivity inst = MainActivity.instance();
                        for (String ms : msg) {
                            System.out.println(ms+" ");
                        }
                        System.out.println("\n");
                        if (msg[1].compareTo("answer")==0) {
                            inst.updateChatIndicator(msg[2]);
                        } else {
                            inst.updateQuestion(message);
                        }
                        writeToFile("mcq_phoneNo", sender);
                    } else if (msg[0].compareTo("mcqAppAnswer")==0) {
                        SenderMode inst = SenderMode.instance();
                        inst.updateAnswer(msg[1]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String readFromFile(String fileName) {
        String temp = null;
        FileInputStream fin;
        try {
            fin = new FileInputStream(sdCardRoot + fileName);
            byte[] b = new byte[fin.available()];
            fin.read(b);
            String s = new String(b);
            temp = s;
            fin.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return temp;
    }

    public void writeToFile(String fileName,String str){
        OutputStream fos;
        try {
            fos= new FileOutputStream(sdCardRoot+fileName);
            byte[] b=str.getBytes();
            fos.write(b);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

}
