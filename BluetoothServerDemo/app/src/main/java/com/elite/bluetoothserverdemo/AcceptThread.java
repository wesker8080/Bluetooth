package com.elite.bluetoothserverdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * @Author: Wesker
 * @Date: 2019-04-15 14:13
 * @Version: 1.0
 */
public class AcceptThread extends Thread{
    private final BluetoothServerSocket mmServerSocket;
    private final String NAME = "eliteai";
    private ManageThread thread;
    private final UUID MY_UUID = UUID.fromString("6e391b46-62cc-4c38-a5a1-83629cf99c8d");

    public AcceptThread(BluetoothAdapter mBluetoothAdapter) {
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        BluetoothServerSocket bluetoothServerSocket = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            Log.d(MainActivity.TAG, "MY_UUID : " + MY_UUID);
            bluetoothServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
        } catch (IOException e) {
            Log.e(MainActivity.TAG, "listenUsingInsecureRfcommWithServiceRecord error : " + e.getMessage());
        }
        mmServerSocket = bluetoothServerSocket;
    }

    @Override
    public void run() {
        BluetoothSocket socket;
        // Keep listening until exception occurs or a socket is returned
        while (true) {
            try {
                socket = mmServerSocket.accept();
                Log.d(MainActivity.TAG, "accept");
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    manageConnectedSocket(socket);
                    mmServerSocket.close();
                    break;
                }
            } catch (IOException e) {
                break;
            }
        }
    }

    private void manageConnectedSocket(BluetoothSocket socket) {
        thread = new ManageThread(socket);
        thread.start();
        if (onClientConnectListener != null) {
            onClientConnectListener.onConnected();
        }
    }

    /** Will cancel the listening socket, and cause the thread to finish */
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException ignored) { }
    }

    public ManageThread getServerSocket() {
        return thread;
    }

    private OnClientConnectListener onClientConnectListener;
    public void setOnClientConnectListener(OnClientConnectListener onClientConnectListener) {
        this.onClientConnectListener = onClientConnectListener;
    }
}
