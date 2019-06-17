package com.elite.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

import static com.elite.bluetoothdemo.MainActivity.TAG;

/**
 * @Author: Wesker
 * @Date: 2019-04-15 14:40
 * @Version: 1.0
 */
public class ConnectThread extends Thread{
    private final BluetoothSocket socket;
    private final BluetoothDevice device;
    private final BluetoothAdapter bluetoothAdapter;
    private final UUID MY_UUID = UUID.fromString("6e391b46-62cc-4c38-a5a1-83629cf99c8d");

    public ConnectThread(BluetoothAdapter bluetoothAdapter, BluetoothDevice device) {
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        this.device = device;
        this.bluetoothAdapter = bluetoothAdapter;
        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e(TAG, "createRfcommSocketToServiceRecord error : " + e.getMessage());
        }
        socket = tmp;
    }

    public BluetoothSocket getBluetoothSocket() {
        return socket;
    }

    public void run() {
        // Cancel discovery because it will slow down the connection
        bluetoothAdapter.cancelDiscovery();

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            socket.connect();
            Log.d(TAG, "socket connect success : ");

        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
               socket.close();
            } catch (IOException closeException) { }
            return;
        }

        // Do work to manage the connection (in a separate thread)
        manageConnectedSocket(socket);
    }

    private void manageConnectedSocket(BluetoothSocket mmSocket) {
    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            socket.close();
        } catch (IOException e) { }
    }
}
