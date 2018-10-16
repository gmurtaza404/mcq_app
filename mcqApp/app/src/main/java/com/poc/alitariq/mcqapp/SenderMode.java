package com.poc.alitariq.mcqapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SenderMode extends Activity{

	Button reset;
	Button checkAnswer;
	Button askQuestion;
    EditText writeQuestion;
    EditText phoneNum;

	private RadioGroup radioGroup;
	private RadioButton radioButton1;
	private RadioButton radioButton2;
	private RadioButton radioButton3;
	private RadioButton radioButton4;
	private RadioButton tempRadioButton;

    int data_state[] = {1000, 10000, 19000, 28000};
    private static SenderMode activity;


    String phoneNo = "03174305965"; //"03474406284";


    String sdCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath().toString()+"/testing/";
//	String sdCardRoot = Environment.getDataDirectory().getAbsolutePath().toString()+ "/testing/";

    public void clearFile(String fileName) {
        OutputStream fos;
        try {
            fos = new FileOutputStream(sdCardRoot + fileName);//"lastOffHook"
            String str = "";
            byte[] b = str.getBytes();
            fos.write(b);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

	public void writeToFile(String fileName,String str){
        OutputStream fos;
        try {
            fos = new FileOutputStream(sdCardRoot+fileName);
			byte[] b=str.getBytes();
			fos.write(b);
			fos.close();
			System.out.println(sdCardRoot+fileName+": " + str);
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

    @Override
    public void onStart() {
        super.onStart();
        activity = this;
    }

    BroadcastReceiver broadcastMiscallReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("Misscall receieved");
            checkAnswer();
        }
    };


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sender_mode);
		writeQuestion = (EditText) findViewById(R.id.pT_question);
        phoneNum = (EditText) findViewById(R.id.pT_phoneNumber);
        reset = (Button) findViewById(R.id.bReset);
        askQuestion = (Button) findViewById(R.id.bAskQuestion);
        checkAnswer = (Button) findViewById(R.id.bCheckAnswer);
		radioGroup = (RadioGroup) findViewById(R.id.radioSex2);
		radioButton1 = (RadioButton) findViewById(R.id.r_b_11);
		radioButton2 = (RadioButton) findViewById(R.id.r_b_22);
		radioButton3 = (RadioButton) findViewById(R.id.r_b_33);
		radioButton4 = (RadioButton) findViewById(R.id.r_b_44);

        registerReceiver(broadcastMiscallReceiver, new IntentFilter("MISCALL_RECEIVED"));


        reset.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                clearFile("mcq_lastOffHook");
                clearFile("mcq_lastIdeal");
                clearFile("mcq_lastRinging");
                clearFile("mcq_parameters");
                clearFile("mcq_connect");
                clearFile("mcq_disconnect");
                clearFile("mcq_notifications");
                clearFile("mcq_output");
                clearFile("mcq_outGoingState");
                clearFile("mcq_lastOutGoingState");
                clearFile("mcq_ringingTime");
                clearFile("mcq_menuChoice");
                clearFile("mcq_outGoingIdle");
                clearFile("mcq_outGoingDialing");
                clearFile("mcq_outGoingAlerting");
                clearFile("mcq_outGoingActive");
                clearFile("qr_confirmationMsg");
                writeToFile("mcq_lastState","ideal");
                writeToFile("mcq_lastStateChanged", System.currentTimeMillis()+"");

            }
        });


        checkAnswer.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                checkAnswer();

            }
        });

        askQuestion.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //String msg = "how many apple juices did u have?:1:2:3:4";
                String msg = writeQuestion.getText().toString();
                updateQuestion("mcqApp:"+msg);
                msg = "mcqApp:"+msg;
                phoneNo =  phoneNum.getText().toString().substring(4);
                writeToFile("mcq_phoneNo", phoneNo);
                try {
                    SmsManager sms = SmsManager.getDefault();
                    sms.sendTextMessage(phoneNo, null, msg, null, null);
                    System.out.println("Message Sent to "+phoneNo+" successfully!");
                    System.out.println("Message Sent: "+msg);

                    String temp_str = readFromFile("mcq_menuChoice");
                    temp_str = temp_str + '\n' + System.currentTimeMillis();
                    writeToFile("mcq_menuChoice", temp_str);
                    Toast.makeText(SenderMode.this, "Sms Sent", Toast.LENGTH_SHORT).show();


                } catch (Exception e) {
                    System.out.println(e);
                    Toast.makeText(SenderMode.this, "Sms not Send", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastMiscallReceiver);
        super.onDestroy();
    }

    public void checkAnswer () {

        String prev_Str = readFromFile("mcq_confirmationMsg");
        prev_Str = prev_Str + '\n' + System.currentTimeMillis();
        writeToFile("mcq_confirmationMsg", prev_Str);

        String lastIdle_time = readFromFile("mcq_lastIdeal");
        String[] tempo = lastIdle_time.split("\n");
        lastIdle_time = tempo[tempo.length-1];
        String lastRinging_time = readFromFile("mcq_lastRinging");
        tempo = lastRinging_time.split("\n");
        lastRinging_time = tempo[tempo.length-1];
        Long call_length = Long.parseLong(lastIdle_time) - Long.parseLong(lastRinging_time);
        int closest_state = 0;
        int difference = 30000;
        for (int i = 0; i < 4; i++) {
            if ((int) Math.abs(call_length - data_state[i]) < difference) {
                difference = (int) Math.abs(call_length - data_state[i]);
                closest_state = i;
            }
        }
        String msg = "mcqApp:answer:option";
        if (closest_state == 0) {
            msg = msg + "1";
            radioButton1.setChecked(true);
        } else if (closest_state == 1) {
            msg = msg + "2";
            radioButton2.setChecked(true);
        } else if (closest_state == 2) {
            msg = msg + "3";
            radioButton3.setChecked(true);
        } else {
            msg = msg + "4";
            radioButton4.setChecked(true);
        }

        // sending confirmation msg
        try {
            phoneNo = phoneNum.getText().toString().substring(4);
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNo, null, msg, null, null);

            String temp_str = readFromFile("mcq_menuChoice");
            temp_str = temp_str + '\n' + System.currentTimeMillis();
            writeToFile("mcq_menuChoice", temp_str);


            System.out.println("Message Sent to "+phoneNo+" successfully!");
            System.out.println("Message Sent: "+msg);
        } catch (Exception e) {
            System.out.println(e);
            Toast.makeText(SenderMode.this, "Sms not Send", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateQuestion(String message) {
        String[] array = message.split(":");
        writeQuestion.setText("Question: " + array[1]);
        radioButton1.setText(array[2]);
        radioButton2.setText(array[3]);
        radioButton3.setText(array[4]);
        radioButton4.setText(array[5]);
    }

    public static SenderMode instance() {
        return activity;
    }

    public void updateAnswer(String s) {

        if (s.compareTo("option1") == 0) {
            radioButton1.setChecked(true);
        } else if (s.compareTo("option2") == 0) {
            radioButton2.setChecked(true);
        } else if (s.compareTo("option3") == 0) {
            radioButton3.setChecked(true);
        } else {
            radioButton4.setChecked(true);
        }

        try {
            phoneNo = phoneNum.getText().toString().substring(4);
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNo, null, "mcqApp:answer:"+s, null, null);

        } catch (Exception e) {
            System.out.println(e);
            Toast.makeText(SenderMode.this, "Sms not Send", Toast.LENGTH_SHORT).show();
        }
    }
}
