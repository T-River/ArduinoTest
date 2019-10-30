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

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String TAG = "ArduinoTest";
    private String readMsg = "";
    //シリアル通信関連
    private boolean mFinished;
    private UsbSerialPort port;
    UsbDeviceConnection connection;

    //permission
    PendingIntent mPermissionIntent;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";


    @Override
    protected void onCreate(Bundle savedInstanceState) {        //TODO:常にUSB接続状態を確認
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TextViewの設定
        TextView StartingMessage  = findViewById(R.id.Run);
        TextView none = findViewById(R.id.isEmpty);
        StartingMessage.setText(R.string.running);

        //----------------------------------USB---------------------------------------------------------------------------------------------------------------//

        //つながってるものから使える全部のデバイス見つける
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
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
                    port.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                    mFinished = false;

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


    /************************************Serial通信スレッド********************************** **************/

    public void start_read_thread(){
        new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    Log.d(TAG,"Thread Start");
                    while(!mFinished){
                        Message msg = Message.obtain(mHandler);
                        byte[] buff = new byte[256];
                        int num = port.read(buff,buff.length);
                        readMsg = new String(buff, 0, num);      //(byte[],offset,復号化するバイト数)
                        msg.obj = readMsg;
                        mHandler.sendMessage(msg);

                        Thread.sleep(10);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally{
                    Log.d(TAG,"Thread Finally");
                }
            }
        }).start();
    }
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            TextView data = findViewById(R.id.arduino);

            String mData = (String)msg.obj;
            data.setText(mData);
        }
    };
}

