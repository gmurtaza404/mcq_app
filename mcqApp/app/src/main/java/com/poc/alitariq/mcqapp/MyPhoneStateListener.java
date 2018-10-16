package com.poc.alitariq.mcqapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.CallLog;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static java.lang.Thread.sleep;

public class MyPhoneStateListener extends PhoneStateListener {
	public static Boolean phoneRinging = false;
	public static Boolean offhook = false;
	public static Boolean ideal = false;
    private AudioManager myAudioManager;

	Context c;
	String sdCardRoot = Environment.getExternalStorageDirectory().toString()+ "/testing/";
    int currentVolume = 0;
    long lastStateChanged;
	public MyPhoneStateListener(Context con) {
		// TODO Auto-generated constructor stub
		c = con;

    }

	public void writeToFile(String fileName,String str){
		OutputStream fos;
		try {
			fos = new FileOutputStream(sdCardRoot+fileName);
			byte[] b=str.getBytes();
			fos.write(b);
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	};

	public String readFromFile(String fileName){
		String temp=null;
		FileInputStream fin;
		try {
			fin = new FileInputStream(sdCardRoot+fileName);
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

	public void onCallStateChanged(int state, String incomingNumber) {

//        System.out.println("state changed : "+ state);
		myAudioManager = (AudioManager)c.getSystemService(Context.AUDIO_SERVICE);
		currentVolume = myAudioManager.getStreamVolume(AudioManager.STREAM_RING);

		if (state == TelephonyManager.CALL_STATE_RINGING) {

            String phone_num = readFromFile("mcq_phoneNo");
            System.out.println(phone_num);
            phone_num = phone_num.substring(phone_num.length() - 10, phone_num.length());

            System.out.println("incoming number: " + incomingNumber + " stored number: "+ phone_num);
            if (phone_num.compareTo(incomingNumber.substring(incomingNumber.length() - 10, incomingNumber.length())) != 0) {
                writeToFile("mcq_lastState", "ideal");
                return;
            }
        }
        switch (state) {
		case TelephonyManager.CALL_STATE_IDLE:
			ideal = true;
			phoneRinging = false;
			offhook = false;
			break;
		case TelephonyManager.CALL_STATE_OFFHOOK:
			ideal = false;
			phoneRinging = false;
			offhook = true;
			break;
		case TelephonyManager.CALL_STATE_RINGING:
			ideal = false;
			phoneRinging = true;
			offhook = false;
			break;
		}

		if (offhook){
            long lastOffHook=System.currentTimeMillis();
			String strLastOffHook=Long.toString(lastOffHook);
			String str=readFromFile("mcq_lastState");
            String str2=readFromFile("mcq_lastStateChanged");
            if (str2.equalsIgnoreCase("")) {
                str2 = "0";
            }
            lastStateChanged = Long.parseLong(str2);
			if(str!=null && str.equals("offHook")) {}
			else{
			    if (lastOffHook-lastStateChanged >1000) {
                    System.out.println("str: " + str + " lastStateChanged: " + lastStateChanged + " difference: " + (lastOffHook - lastStateChanged));
                    lastStateChanged = lastOffHook;
                    System.out.println(sdCardRoot + "lastOffHook: " + lastOffHook);
                    writeToFile("mcq_lastOffHook", strLastOffHook);
                    writeToFile("mcq_lastState", "offHook");
                    writeToFile("mcq_lastStateChanged", lastStateChanged + "");
                }
			}


		}

		if (ideal){
			long lastIdeal=System.currentTimeMillis();
			String strLastIdeal=Long.toString(lastIdeal);
			String str=readFromFile("mcq_lastState");
            String str2=readFromFile("mcq_lastStateChanged");
            if (str2.equalsIgnoreCase("")) {
                str2 = "0";
            }
            lastStateChanged = Long.parseLong(str2);
			if(str!=null && str.equals("ideal")){
            }else{
			    if (lastIdeal-lastStateChanged >1000) {
                    System.out.println("str: " + str + " lastStateChanged: " + lastStateChanged + " difference: " + (lastIdeal - lastStateChanged));
                    lastStateChanged = lastIdeal;
                    System.out.println(sdCardRoot + "lastIdeal: " + lastIdeal);
                    String prev_state = readFromFile("mcq_lastState");
                    if (prev_state.compareTo("ringing")== 0) {
                        try {
                            sleep(4000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (callLogs(c) == 5) {
                            c.sendBroadcast(new Intent("CALL_RECEIVED"));
                        } else {
                            c.sendBroadcast(new Intent("MISCALL_RECEIVED"));
                        }
                    }

                    writeToFile("mcq_lastIdeal", strLastIdeal);
                    writeToFile("mcq_lastState", "ideal");
                    writeToFile("mcq_lastStateChanged", lastStateChanged + "");
                }
			}
//            myAudioManager.setStreamMute(AudioManager.STREAM_RING, false);
            myAudioManager.setStreamVolume(AudioManager.STREAM_RING, currentVolume, 0);
		}

		if(phoneRinging){
//            myAudioManager.setStreamMute(AudioManager.STREAM_RING, true);
            myAudioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0);
			long lastRinging=System.currentTimeMillis();
			String strLastRinging=Long.toString(lastRinging);
			String str=readFromFile("mcq_lastState");
            String str2=readFromFile("mcq_lastStateChanged");
            if (str2.equalsIgnoreCase("")) {
                str2 = "0";
            }
            lastStateChanged = Long.parseLong(str2);
			if(str!=null && str.equals("ringing")){}
			else {
			    if (lastRinging-lastStateChanged >1000) {
                    System.out.println("str: " + str + " lastStateChanged: " + lastStateChanged + " difference: " + (lastRinging - lastStateChanged));
                    lastStateChanged = lastRinging;
                    System.out.println(sdCardRoot + "lastRinging: " + lastRinging);
                    writeToFile("mcq_lastRinging", strLastRinging);
                    writeToFile("mcq_lastState", "ringing");
                    writeToFile("mcq_lastStateChanged", lastStateChanged + "");
                }
            }
		}
	}
	public int callLogs(Context con) {
		Uri allCalls = Uri.parse("content://call_log/calls");
		Cursor cur = con.getContentResolver().query(allCalls, null, null, null, null);
		int type = 0;
		while (cur.moveToNext()) {
			cur.moveToFirst();
			String num = cur.getString(cur.getColumnIndex(CallLog.Calls.NUMBER));// for  number
			String name = cur.getString(cur.getColumnIndex(CallLog.Calls.CACHED_NAME));// for name
			String duration = cur.getString(cur.getColumnIndex(CallLog.Calls.DURATION));// for duration
			type = Integer.parseInt(cur.getString(cur.getColumnIndex(CallLog.Calls.TYPE)));// for call type, Incoming or out going
			System.out.println("Call Type received: " + Integer.toString(type));
			break;
		}
		return type;
	}

}
