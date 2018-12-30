package com.poc.alitariq.mcqapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static java.lang.Thread.sleep;

//import android.telecom.*;

public class MainActivity extends Activity {
    //UI handlers
    TextView userNotification;
    Button senderMode;
    Button sendAnswer;
    ProgressBar progress;


    //parameters
    int callDisconnectTime = 4000;
    int waitForCall = 6000;
    int stateTime = 1500;
    int max_wait_for_ringing = 200;
    String phoneNo = "03174305965"; //"03474406284";

    //variables
    int closed = 0;
    int waiting_for_call_connect;
    Method telephonyEndCall;
    Object telephonyObject;
    int receiverState = 0;
    long constant_disconnect_time = 1000;

    private static MainActivity activity;

    private ImageView imgView;
    private RadioGroup radioGroup;
    private RadioButton radioButton1;
    private RadioButton radioButton2;
    private RadioButton radioButton3;
    private RadioButton radioButton4;
    private RadioButton tempRadioButton;

    int data_state[] = {1000, 10000, 19000, 28000};

    //Encoding
    Map<Object, String> charToBinary = null;
    Map<String, String> binaryToString = null;
    Map<Integer, String> callLengthToBinary = null;
    String suggestions;

    String sdCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath()+"/testing/";
//    String sdCardRoot = Environment.getDataDirectory().getAbsolutePath().toString()+ "/testing/";


    //utility functions
    public void askPermissions() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.MODIFY_AUDIO_SETTINGS);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS)) {
                //showExplanation("Permission Needed", "Rationale", Manifest.permission.READ_PHONE_STATE, 1);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.MODIFY_AUDIO_SETTINGS},
                        1);
            }
        } else {
            Toast.makeText(MainActivity.this, "Permission (already) Granted!", Toast.LENGTH_SHORT).show();
        }

    }

    public int waitForCallUsingVisualizer() {
        waiting_for_call_connect = 0;
        Visualizer mVisualizer = new Visualizer(0);
        mVisualizer.setEnabled(false);
        int capRate = Visualizer.getMaxCaptureRate();
        int capSize = Visualizer.getCaptureSizeRange()[1];
        mVisualizer.setCaptureSize(capSize);
        Visualizer.OnDataCaptureListener captureListener = new Visualizer.OnDataCaptureListener() {
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes,
                                              int samplingRate) {
//                System.out.println("input on visualizer received!");
                for (int i = 0; i < bytes.length; i++) {
//                    System.out.println(bytes[i]);
                    if (bytes[i] != -128) {
                        //yes detected
                        waiting_for_call_connect = 1;
                        break;
                    }
                }
            }

            public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
            }
        };

        int status2 = mVisualizer.setDataCaptureListener(captureListener, capRate, true/*wave*/, false/*no fft needed*/);
        mVisualizer.setEnabled(true);
        while (true) {
            if (waiting_for_call_connect == 1) {
                break;
            }
        }
        mVisualizer.setEnabled(false);
        mVisualizer.release();
        return 1;

    }

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

    public void makeFile(String fileName, File myDir) {
        //make dir
        String temp = null;
        FileInputStream fin;
        try {
            fin = new FileInputStream(sdCardRoot + fileName);
            byte[] b = new byte[fin.available()];
            fin.read(b);
            String s = new String(b);
            temp = s;
            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        File file = new File(myDir, fileName );
        try {
            FileOutputStream out = new FileOutputStream(file);
            out.write(temp.getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File makeDir() {
        long name = System.currentTimeMillis();
        File myDir = new File(sdCardRoot );
        myDir.mkdirs();

        return myDir;
    }

    public Method disconnectInitializer() {

        try {
            Class<?> telephonyClass = Class.forName("com.android.internal.telephony.ITelephony");
            Class<?> telephonyStubClass = telephonyClass.getClasses()[0];
            Class<?> serviceManagerClass = Class.forName("android.os.ServiceManager");
            Class<?> serviceManagerNativeClass = Class.forName("android.os.ServiceManagerNative");

            Object serviceManagerObject;
            Method getService = // getDefaults[29];
                    serviceManagerClass.getMethod("getService", String.class);
            Method tempInterfaceMethod = serviceManagerNativeClass.getMethod(
                    "asInterface", IBinder.class);
            Binder tmpBinder = new Binder();
            tmpBinder.attachInterface(null, "fake");
            serviceManagerObject = tempInterfaceMethod.invoke(null, tmpBinder);
            IBinder retbinder = (IBinder) getService.invoke(
                    serviceManagerObject, "phone");
            Method serviceMethod = telephonyStubClass.getMethod("asInterface",
                    IBinder.class);
            telephonyObject = serviceMethod.invoke(null, retbinder);
            telephonyEndCall = telephonyClass.getMethod("endCall");


        } catch (Exception e) {
            e.printStackTrace();

        }
        return telephonyEndCall;
    }

    public void disconnectCall() {
        try {
            telephonyEndCall.invoke(telephonyObject);
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
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
//            System.out.println(sdCardRoot+fileName+": " + str);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    };

    public void appendToFile(String fileName,String str){
        OutputStream fos;
        try {
//            System.out.println(sdCardRoot+fileName+" : "+str);
            String prev = readFromFile(fileName);
            prev = prev + "\n" + str;
            fos= new FileOutputStream(sdCardRoot+fileName);
            byte[] b=prev.getBytes();
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
    public void writeParametersInSeparateFiles() {
        File myDir = makeDir();
    }

    public void myToast(String str) {
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
    }

    public void send_func_Msg(final String symbols) {

        try {
            phoneNo = readFromFile("mcq_phoneNo");
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNo, null, "mcqAppAnswer:"+symbols, null, null);

            String prev_str = readFromFile("mcq_lastMsgOneTick");
            writeToFile("mcq_lastMsgOneTick", prev_str + "\n" + System.currentTimeMillis());

        } catch (Exception e) {
            System.out.println(e);
            Toast.makeText(MainActivity.this, "Sms not Send", Toast.LENGTH_SHORT).show();
        }
//        progress.setProgress(0);
//        progress.setMax(20);
//        int count = 0;
//        while (count < progress.getMax()) {
//            count++;
//            int progressInt = progress.getProgress();
//            if (progressInt >= progress.getMax()) {
//
//            } else {
//                progress.setProgress(progress.getProgress()+1);
//            }
//            try {
//                sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

    }

    public void send_func(final String symbols) {
        writeToFile("mcq_lastState", "r");
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                phoneNo = readFromFile("mcq_phoneNo");
                callIntent.setData(Uri.parse("tel:" + phoneNo));

                System.out.println("before: " + System.currentTimeMillis());
                long l = System.currentTimeMillis();
                try {
                    startActivity(callIntent);
                } catch (SecurityException e) {
                    System.out.println("Exception in starting call");
                    e.printStackTrace();
                }
                //waitForCallUsingVisualizer();
                waitForCall();
                System.out.println("connect: " + System.currentTimeMillis());
                String str0 = readFromFile("mcq_connect");
                writeToFile("mcq_connect", str0 + System.currentTimeMillis() + "\n");
                try {
                    System.out.println("Option received: "+symbols);
                    if (symbols.compareTo("option1") == 0) {
                        System.out.println("1");
                        sleep(data_state[0]);
                    } else if (symbols.compareTo("option2") == 0) {
                        System.out.println("2");
                        sleep(data_state[1]);
                    } else if (symbols.compareTo("option3") == 0) {
                        System.out.println("3");
                        sleep(data_state[2]);
                    } else {
                        System.out.println("4");
                        sleep(data_state[3]);
                    }
                    //Thread.sleep(data_time[count]);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                String str = readFromFile("mcq_disconnect");
                writeToFile("mcq_disconnect", str + System.currentTimeMillis() + "\n");
                disconnectCall();
                System.out.println("disconnect: " + System.currentTimeMillis());
                String prev_str = readFromFile("mcq_lastMsgOneTick");
                writeToFile("mcq_lastMsgOneTick", prev_str + "\n" + System.currentTimeMillis());

//                progress.setProgress(0);
//                progress.setMax(20);
//                int count = 0;
//                while (count < progress.getMax()) {
//                    count++;
//                    int progressInt = progress.getProgress();
//                    if (progressInt >= progress.getMax()) {
//
//                    } else {
//                        progress.setProgress(progress.getProgress()+2);
//                    }
//                    try {
//                        sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
            }
        });
        t.start();
    }

    private void waitForCall() {
        try {
            sleep(waitForCall);
        } catch (InterruptedException e) {
            System.out.println("Exception in waiting for call");
            e.printStackTrace();
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        activity = this;
    }

    public void sendAnswer() {
        imgView.setImageResource(R.drawable.sent);

        int selectedId = radioGroup.getCheckedRadioButtonId();
        // find the radiobutton by returned id
        tempRadioButton = (RadioButton) findViewById(selectedId);
//            Toast.makeText(MainActivity.this, tempRadioButton.getText(), Toast.LENGTH_SHORT).show();
//          String str = text.getText().toString();
        //getTrimmedString(str);
        CheckBox cBox = (CheckBox) findViewById(R.id.checkBoxSMS);
        String prev_str = readFromFile("mcq_sendingTime");
        writeToFile("mcq_sendingTime", prev_str + "\n" + System.currentTimeMillis());
        if (cBox.isChecked()) {
            send_func_Msg(tempRadioButton.getTag().toString());
        } else {
            send_func(tempRadioButton.getTag().toString());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        writeParametersInSeparateFiles();
        disconnectInitializer();
        askPermissions();
        makeFiles();

        //for main activity
        userNotification = (TextView) findViewById(R.id.tvUserNotification);
        sendAnswer = (Button) findViewById(R.id.b_send);
        senderMode = (Button) findViewById(R.id.bSender);
        progress = (ProgressBar) findViewById(R.id.progressBar);
        progress.setMax(20);

        radioGroup = (RadioGroup) findViewById(R.id.sendOption);
        radioButton1 = (RadioButton) findViewById(R.id.r_b_1);
        radioButton2 = (RadioButton) findViewById(R.id.r_b_2);
        radioButton3 = (RadioButton) findViewById(R.id.r_b_3);
        radioButton4 = (RadioButton) findViewById(R.id.r_b_4);
        imgView = (ImageView) findViewById(R.id.chatStatusIndicator);
//        imgView.setImageResource(R.drawable.clearBackground);

        sendAnswer.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendAnswer();
            }
        });

        // Uncomment the below code for sender mode screen - (clicking the main screen Title in app will take you to sender screen)
//        senderMode.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//               Runnable showDialogRun = new Runnable() {
//                  public void run() {
//                    Intent showDialogIntent = new Intent(getBaseContext(), SenderMode.class);
//                    startActivity(showDialogIntent);
//                  }};
//               Handler h = new Handler(Looper.getMainLooper());
//               h.postDelayed(showDialogRun, 100);
//            }
//        });

    }

    private void makeFiles() {
        createFile("mcq_menuChoice");
        createFile("mcq_phoneNo");
        createFile("mcq_connect");
        createFile("mcq_disconnect");
        createFile("mcq_lastIdeal");
        createFile("mcq_lastOffHook");
        createFile("mcq_lastRinging");
        createFile("mcq_lastState");
        createFile("mcq_lastStateChanged");
        createFile("mcq_menuChoice");
        createFile("mcq_notifications");
        createFile("mcq_output");
        createFile("mcq_phoneNo");
        createFile("mcq_ringingTime");
        createFile("mcq_lastMsgBlueTick");
        createFile("mcq_lastMsgOneTick");
        createFile("mcq_sendingTime");
    }

    private void createFile(String filename) {
        File yourFile = new File(sdCardRoot + filename);
        try {
            yourFile.createNewFile(); // if file already exists will do nothing
        } catch (IOException e) {
            System.out.println("unable to create new file");
            e.printStackTrace();
        }
        try {
            FileOutputStream oFile = new FileOutputStream(yourFile, false);
        } catch (FileNotFoundException e) {
            System.out.println("file couldnot be opened for writing");
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        closed = 1;
    }

    public void updateQuestion(String message) {
        imgView = (ImageView) findViewById(R.id.chatStatusIndicator);
        imgView.setImageResource(R.drawable.clearbackground);
        String[] array = message.split(":");
        userNotification.setText("Question: " + array[1]);
        radioButton1.setText(array[2]);
        radioButton2.setText(array[3]);
        radioButton3.setText(array[4]);
        radioButton4.setText(array[5]);

    }

    public static MainActivity instance() {
        return activity;
    }


    public void updateChatIndicator(String str) {
        imgView = (ImageView) findViewById(R.id.chatStatusIndicator);
        int selectedId = radioGroup.getCheckedRadioButtonId();
        tempRadioButton = (RadioButton) findViewById(selectedId);
        String tag = tempRadioButton.getTag().toString();
        if (tag.compareTo(str)==0) {
            imgView.setImageResource(R.drawable.read);
            String prev_str = readFromFile("mcq_lastMsgBlueTick");
            writeToFile("mcq_lastMsgBlueTick", prev_str + "\n" + System.currentTimeMillis());
//            progress.setProgress(progress.getMax());
        } else {
            imgView.setImageResource(R.drawable.delivered);
            sendAnswer();
        }
    }

}
