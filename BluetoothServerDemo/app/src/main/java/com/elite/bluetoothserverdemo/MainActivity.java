package com.elite.bluetoothserverdemo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.lang.reflect.Field;

public class MainActivity extends Activity {

    public static final String TAG = "BluetoothServerDebug";

    private BluetoothAdapter bluetoothAdapter;
    private final int REQUEST_ENABLE = 1;
    private Button btnSend;
    private ManageThread serverSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnSend = findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serverSocket != null) {
                    String info = "data";
                    serverSocket.write(info.getBytes());
                } else {
                    Toast.makeText(MainActivity.this, "连接未完成", Toast.LENGTH_SHORT).show();
                }
            }
        });
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // 不支持蓝牙
            Toast.makeText(this, "该设备不支蓝牙", Toast.LENGTH_SHORT).show();
            return;
        }
        openBluetooth();
        setBluetoothDiscoverable();
        acceptClientMessage();
    }

    private void openBluetooth() {
        if(!bluetoothAdapter.isEnabled()){
            //不做提示，直接打开，不建议用下面的方法，有的手机会有问题。
            bluetoothAdapter.enable();
            //弹出对话框提示用户是后打开
            //Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(enabler, REQUEST_ENABLE);
        }
        String address = getBluetoothAddress(bluetoothAdapter);
        Log.d(TAG, "bluetooth address : " + address);
    }
    private void acceptClientMessage() {
        while (!bluetoothAdapter.isEnabled()) {

        }
        final AcceptThread acceptThread = new AcceptThread(bluetoothAdapter);
        acceptThread.start();
        acceptThread.setOnClientConnectListener(new OnClientConnectListener() {
            @Override
            public void onConnected() {
                serverSocket = acceptThread.getServerSocket();
            }
        });
    }



    private void setBluetoothDiscoverable() {
        if (bluetoothAdapter.isEnabled()) {
            if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                Intent discoverableIntent = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(
                        BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivity(discoverableIntent);
            }
        }
    }
    private String getBluetoothAddress(BluetoothAdapter adapter) {
        if (adapter == null) {
            return null;
        }
        try {
            Field mServiceField = adapter.getClass().getDeclaredField("mService");
            mServiceField.setAccessible(true);
            Object btManagerService = mServiceField.get(adapter);
            if (btManagerService != null) {
                return (String) btManagerService.
                        getClass().getMethod("getAddress").invoke(btManagerService);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
