package com.example.usbserial;

import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    Context context;
    //Log用のTAG
    private final String TAG = MainActivity.class.getSimpleName();

    //TextViewの宣言
    TextView TitleTextView;
    TextView errorTextView;

    TextView countTextView;
    TextView timesTextView;

    ScrollView textScroll;
    TextView mDumpTextView;

    private String messageLine;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private static UsbSerialPort myPort = null;
    private SerialInputOutputManager mySerialIOManager;

    private final SerialInputOutputManager.Listener myListener =
            new SerialInputOutputManager.Listener() {
                @Override
                public void onNewData(final byte[] data) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.this.updateReceivedData(data);
                        }
                    });
                }

                @Override
                public void onRunError(Exception e) {
                    Log.d(TAG,"Runner stopped.");
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        //TextViewの設定
        setContentView(R.layout.activity_main);
        TitleTextView = findViewById(R.id.TitleText);
        errorTextView = findViewById(R.id.errorText);
        countTextView = findViewById(R.id.countText);
        timesTextView = findViewById(R.id.timesText);
        textScroll = findViewById(R.id.textScroll);
        mDumpTextView = findViewById(R.id.consoleText);

        // 接続されている全デバイスの検出
        UsbManager manager = (UsbManager) getSystemService(context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            errorTextView.setText("No Device is found");
            return;
        }

        // 使用可能なドライバーの最初のデバイスとの通信を開く
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            // add UsbManager.requestPermission(driver.getDevice(), ..) handling here
            return;
        }

        myPort = driver.getPorts().get(0);      // デバイスはいつも一つ！
        try {
            myPort.open(connection);
            myPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
        }catch(IOException e) {
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopIoManager();
        if (myPort != null) {
            try {
                myPort.close();
            } catch (IOException e) {
                // 無視
            }
            myPort = null;
        }
        finish();
    }

    @Override
    protected void onResume(){
        super.onResume();

        Log.d(TAG,"Resumed, port=" + myPort);
        if(myPort == null){
            errorTextView.setText("No Device is found");
        }else{
            final UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

            UsbDeviceConnection connection = usbManager.openDevice(myPort.getDriver().getDevice());
            if (connection == null){
                errorTextView.setText("Opening device failed.");
                return;
            }
            TitleTextView.setText("Serial device: " + myPort.getClass().getSimpleName());
        }

        //初回計測要求
        try{
            myPort.write("getData".getBytes(),1000);
        }catch(IOException e){
        }
        //onDeviceStateChange();

    }

    //受信時の処理
    private void updateReceivedData(byte[] data){
        Log.d(TAG,"received message.");
        messageLine += HexDump.dumpHexString(data);
        if(data[data.length - 1] == 0x0A){      //改行文字を読み込んだ時のみ表示
            mDumpTextView.append(messageLine);
            textScroll.smoothScrollTo(0, mDumpTextView.getBottom());

            displayReceivedData(messageLine);   //受信データを文字列として表示

            messageLine = "";                   //表示したのでmessageLineをリセット
            try{
                String str = "getData";
                myPort.write(str.getBytes(), 1000);
            }catch(IOException e){
            }
        }
    }

    //受信データの表示
    private void displayReceivedData(String s){
        String[] values = s.split(",",-1);
        countTextView.setText(values[0]);
        timesTextView.setText(values[1]);

    }

    private void onDeviceStateChange(){
        stopIoManager();
        startIoManager();
    }

    private void stopIoManager(){
        if (mySerialIOManager != null) {
            Log.i(TAG, "Stopping io manager ..");
            mySerialIOManager.stop();
            mySerialIOManager = null;
        }
    }

    private void startIoManager(){
        if (myPort != null) {
            Log.i(TAG, "Starting io manager ..");
            mySerialIOManager = new SerialInputOutputManager(myPort, myListener);
            mExecutor.submit(mySerialIOManager);    //TODO:理解
        }
    }
}