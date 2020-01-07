package com.example.arduinotest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String TAG = "ArduinoTest";
    private String readMsg = "";
    private  byte[] command;
    //シリアル通信関連
    private boolean mFinished;
    //private int FirstRequest ;
    private UsbSerialPort port;
    UsbDeviceConnection connection;

    private String FileName = "dataFile.txt";

    //permission
    PendingIntent mPermissionIntent;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TextViewの設定
        TextView none = findViewById(R.id.isEmpty);
        //TextView debug =findViewById(R.id.debug1);

        //----------------------------------USB---------------------------------------------------------------------------------------------------------------//

        //つながってるものから使える全部のデバイス見つける
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            //FirstRequest = 0;
            none.setText(R.string.empty);
            Log.d(TAG,"Usb device is not connect.");
            //return;
        }else{
            //Open a connection to the first available driver.
            UsbSerialDriver driver = availableDrivers.get(0);

            //permission付与
            mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
            manager.requestPermission( driver.getDevice() , mPermissionIntent);



            connection = manager.openDevice(driver.getDevice());
            if (connection == null) {
                port = null;
            }else{
                port = driver.getPorts().get(0);
                try {
                    port.open(connection);
                    //debug.setText("接続済み");
                    port.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

                    mFinished = false;
                    command = new byte[1];
                    command[0] = 0x02;
                    port.write(command,100);
                    //debug.setText("送信完了");

                    /*if(FirstRequest == 0){
                        command = new byte[1];
                        command[0] = 0x02;
                        port.write(command,100);
                        debug.setText("送信完了");
                    }*/

                    start_read_thread();
                } catch (IOException e) {

                } finally{
                    Log.d(TAG,"finally");
                }
            }
        }
    }
    /*@Override
    protected void onPause() {
        //スレッドを終わらせる指示。終了待ちしていません。
        mFinished = true;

        //シリアル通信の後片付け ポート開けてない場合にはCloseしないこと
        if (port != null) {
            try {
                port.close();
                Log.d(TAG, "M:onDestroy() port.close()");
            } catch (IOException e) {
                Log.d(TAG, "M:onDestroy() IOException");
            }
        } else {
            Log.d(TAG, "M:port=null\n");
        }
        //-----------------------------------------

        super.onPause();
    }*/


    /************************************Serial通信スレッド************************************************/

    public void start_read_thread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //TextView debug2 = findViewById(R.id.debug2);
                    //debug2.setText("スレッド開始");
                    //FirstRequest = 1;
                    Log.d(TAG, "Thread Start");
                    while (!mFinished) {
                        //debug2.setText("message");
                        Message msg = Message.obtain(mHandler);
                        byte[] buff = new byte[256];
                        //buff = new byte[256];
                        int num = port.read(buff, buff.length);
                        readMsg = new String(buff, 0, num);      //(byte[],offset,復号化するバイト数)
                        msg.obj = readMsg;
                        mHandler.sendMessage(msg);
                        //saveFile(FileName, readMsg);

                        Thread.sleep(500);
                    }
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                    command = new byte[1];
                    command[0] = 0x02;
                    try {
                        //TextView debug2 =findViewById(R.id.debug2);
                        //debug2.setText("計測要求");
                        port.write(command,100);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    Log.d(TAG, "Thread Finish");
                }
            }
        }).start();
    }

    Handler mHandler = new Handler(){
        @Override
        public  void handleMessage(Message msg){
            //TextView debug3 = findViewById(R.id.debug3);
            //debug3.setText("Handler");
            TextView data = findViewById(R.id.arduino);
            String mData = (String)msg.obj;
            saveFile(FileName, mData);
            String rData = readfile(FileName);
            data.setText(rData);
        }
    };

    public void saveFile(String file, String str){
        try (FileOutputStream fileOutputStream = openFileOutput(file, Context.MODE_PRIVATE)){
            fileOutputStream.write(str.getBytes());
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    public String readfile(String file){
        String text = null;

        try (FileInputStream fileInputStream = openFileInput(file);
             BufferedReader reader= new BufferedReader(
                     new InputStreamReader(fileInputStream, StandardCharsets.UTF_8))){
            String lineBuffer;
            while( (lineBuffer = reader.readLine()) != null ){
                text = lineBuffer;
            }

        }catch (IOException e){
            e.printStackTrace();
        }
        return text;
    }
}

