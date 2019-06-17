package com.elite.bluetoothdemo;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * 读写线程
 * @Author: Wesker
 * @Date: 2019-04-15 15:10
 * @Version: 1.0
 */
public class ManageThread extends Thread{

    private final BluetoothSocket socket;
    private final InputStream inStream;
    private final OutputStream outStream;

    public ManageThread(BluetoothSocket socket) {
        this.socket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        inStream = tmpIn;
        outStream = tmpOut;
    }
    public InputStream getSocketInputStream() {
        return inStream;
    }
    public OutputStream getSocketOutputStream() {
        return outStream;
    }
    @Override
    public void run() {
        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes = -1; // bytes returned from read()
        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream
                while ((bytes = inStream.read(buffer)) != -1) {
                    String msg = new String(buffer, 0, bytes, Charset.defaultCharset());
                    // Send the obtained bytes to the UI activity
                    if (serverReceiveListener != null) {
                        serverReceiveListener.serverMessage(msg);
                    }
                    Log.d(MainActivity.TAG, "receive : " + msg);
                }
            } catch (IOException e) {
                Log.e(MainActivity.TAG, "read error : " + e.getMessage());
                break;
            }
        }
    }
    public void write(byte[] bytes) {
        try {
            outStream.write(bytes);
            outStream.flush();
        } catch (IOException e) {
            Log.e(MainActivity.TAG, "write error : " + e.getMessage());
        }
    }
    private OnServerReceiveListener serverReceiveListener;
    public void setOnServerMessageReceive(OnServerReceiveListener serverReceiveListener) {
        this.serverReceiveListener = serverReceiveListener;
    }
}
