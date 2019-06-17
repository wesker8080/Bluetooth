package com.elite.bluetoothdemo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "BluetoothDebug";
    private final int REQUEST_ENABLE = 1;
    //private final String ServerAddress = "FA:35:46:65:80:F6";
    private final String ServerAddress = "FE:A5:46:65:80:75";

    @BindView(R.id.btn_find)
    Button btnFind;
    @BindView(R.id.btn_connect)
    Button btnConnect;
    @BindView(R.id.et_message)
    EditText etMessage;
    @BindView(R.id.btn_send)
    Button btnSend;
    @BindView(R.id.textView)
    TextView textView;
    @BindView(R.id.tv_result)
    TextView tvResult;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice remoteDevice;
    private BluetoothSocket socket;
    private ManageThread manageThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        registerBluetoothReceiver();
        //检查权限（NEED_PERMISSION）是否被授权 PackageManager.PERMISSION_GRANTED表示同意授权
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //用户已经拒绝过一次，再次弹出权限申请对话框需要给用户一个解释
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
                    .WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "请开通相关权限，否则无法正常使用本应用！", Toast.LENGTH_SHORT).show();
            }
            //申请权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        } else {
           // Toast.makeText(this, "授权成功！", Toast.LENGTH_SHORT).show();
        }
       /* Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            KeyguardManager km= (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");//参数是LogCat里用的Tag
            kl.disableKeyguard();
            Log.e("zzk", "aaa");
        }, 5000);*/
    }
    private void registerServerReceive() {
        if (manageThread != null) {
            manageThread.setOnServerMessageReceive(msg -> runOnUiThread(() -> tvResult.setText(msg)));
        }
    }
    @OnClick({R.id.btn_find, R.id.btn_connect, R.id.btn_send})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_find:
                openBluetooth();
                break;
            case R.id.btn_connect:
                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(() -> {

                }, 500);
                break;
            case R.id.btn_send:
                if (socket == null) {
                    Toast.makeText(this, "socket is null ", Toast.LENGTH_SHORT).show();
                    return;
                }
                String msg = etMessage.getText().toString();
                writeMessageToServer(msg);
                registerBluetoothReceiver();
                break;
                default: break;
        }
    }

    private void updateMTP(String path, String name) {
        String filePath = path + File.separator + name + File.separator + "tempFile.txt";
        Log.d("wesker", "filePath : " + filePath);
        File tempFile = new File(filePath);
        boolean createTempFileState = false;
        try {
            createTempFileState = tempFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //如果创建失败，直接放弃扫描
        if (createTempFileState) {
            //在设备连接电脑时，4.4以上不能用Intent.ACTION_MEDIA_MOUNTED扫描SD卡的广播
            //可使用MediaScannerConnection.scanFile()方法，但是这个方法在创建空文件夹时要特殊处理
            // 1.创建文件夹
            // 2.在文件夹下创建一个临时文件
            // 3.扫描并等到扫描结束删除临时文件
            MediaScannerConnection.scanFile(
                    this,
                    new String[]{filePath},
                    null,
                    new MediaScannerConnection.MediaScannerConnectionClient() {
                        @Override
                        public void onMediaScannerConnected() {}

                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            File fileForDelete = new File(path);
                            if (fileForDelete.exists() && fileForDelete.delete()) {
                                //文件存在且删除成功的情况下，删除对应的媒体库数据
                                getContentResolver().delete(uri, null, null);
                            }
                        }
                    }
            );
        }
    }
    private void writeMessageToServer(String msg) {
        manageThread.write(msg.getBytes());
    }
    private void connectToDevice() {
        cancelDiscovery();
        if (remoteDevice == null) {
            remoteDevice = bluetoothAdapter.getRemoteDevice(ServerAddress);
        }
        ConnectThread thread = new ConnectThread(bluetoothAdapter, remoteDevice);
        thread.start();
        socket = thread.getBluetoothSocket();
        manageThread = new ManageThread(socket);
        SystemClock.sleep(200);
        manageThread.start();
        registerServerReceive();

    }
    private void openBluetooth() {
        if(!bluetoothAdapter.isEnabled()){
            //弹出对话框提示用户是后打开
            Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enabler, REQUEST_ENABLE);
            //不做提示，直接打开，不建议用下面的方法，有的手机会有问题。
             //bluetoothAdapter.enable();
        }
        getBluetoothInfo();
        startDiscovery();
    }
    private void getBluetoothInfo() {
        //获取本机蓝牙名称
        String name = bluetoothAdapter.getName();
        //获取本机蓝牙地址
        String address = getBluetoothAddress(bluetoothAdapter);
        Log.d(TAG,"bluetooth name ="+name+" address ="+address);
        //获取已配对蓝牙设备
        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
        Log.d(TAG, "bonded device size ="+devices.size());
        for(BluetoothDevice bonddevice:devices){
            Log.d(TAG, "bonded device name ="+bonddevice.getName()+" address"+bonddevice.getAddress());
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
    private void startDiscovery() {
        bluetoothAdapter.startDiscovery();
    }
    private void cancelDiscovery() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }

    private void registerBluetoothReceiver() {
        if (bluetoothReceiver == null) {
            Log.e(TAG, "bluetoothReceiver null ");
        }
        IntentFilter filter = new IntentFilter();
        //发现设备
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        //设备连接状态改变
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        //蓝牙设备状态改变
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothReceiver, filter);
    }
    private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG,"mBluetoothReceiver action ="+action);
            //每扫描到一个设备，系统都会发送此广播。
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                //获取蓝牙设备
                BluetoothDevice scanDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(scanDevice == null || scanDevice.getName() == null) {return;}
                Log.d(TAG, "name="+scanDevice.getName()+"address="+scanDevice.getAddress());
                //蓝牙设备名称
                String name = scanDevice.getName();
                String scanAddress = scanDevice.getAddress();
                if (Objects.equals(scanAddress, ServerAddress)) {
                    Log.d(TAG, "fined ServerBluetooth ");
                    connectToDevice();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){

            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE) {
            if (resultCode == RESULT_OK) {
             Log.d(TAG, "成功开启蓝牙");
            } else {
                Log.d(TAG, "用户没有开启蓝牙");
            }
        }
    }
}
