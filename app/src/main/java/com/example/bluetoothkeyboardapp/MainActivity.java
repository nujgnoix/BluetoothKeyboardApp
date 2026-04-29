package com.example.bluetoothkeyboardapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidDevice;
import android.bluetooth.BluetoothHidDeviceAppSdpSettings;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends Activity {

    private static final int REQUEST_ENABLE_BT = 1002;

    private TextView statusText;
    private TextView deviceText;
    private TextView debugText;
    private Button scanButton;
    private Button connectButton;
    private Button clearButton;
    private Button sendButton;
    private EditText inputEditText;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothHidDevice hidDevice;
    private BluetoothDevice targetDevice;
    private boolean isConnected = false;
    private Handler handler = new Handler();

    private HidCallback hidCallback;
    private ProfileListener profileListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.status_text);
        deviceText = findViewById(R.id.device_text);
        debugText = findViewById(R.id.debug_text);
        scanButton = findViewById(R.id.scan_button);
        connectButton = findViewById(R.id.connect_button);
        clearButton = findViewById(R.id.clear_button);
        sendButton = findViewById(R.id.send_button);
        inputEditText = findViewById(R.id.input_edit_text);

        hidCallback = new HidCallback(this);
        profileListener = new ProfileListener(this);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanDevices();
            }
        });

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected) {
                    disconnectHid();
                } else {
                    connectHid();
                }
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputEditText.setText("");
                debugText.setText("");
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendText();
            }
        });

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
        }

        if (bluetoothAdapter == null) {
            statusText.setText("设备不支持蓝牙");
            scanButton.setEnabled(false);
            addDebug("错误: 设备不支持蓝牙");
            return;
        }

        addDebug("初始化完成，蓝牙适配器已就绪");
        registerBluetoothReceiver();
    }

    private void addDebug(String message) {
        final String timestamp = java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
        final String logMessage = "[" + timestamp + "] " + message;
        
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String current = debugText.getText().toString();
                if (current.isEmpty()) {
                    debugText.setText(logMessage);
                } else {
                    debugText.setText(current + "\n" + logMessage);
                }
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
        android.util.Log.d("BTKeyboard", message);
    }

    private void registerBluetoothReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothReceiver, filter);
        addDebug("蓝牙广播接收器已注册");
    }

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    String name = device.getName();
                    String address = device.getAddress();
                    if (name != null) {
                        targetDevice = device;
                        deviceText.setText("已配对设备: " + name + " (" + address + ")");
                        connectButton.setEnabled(true);
                        addDebug("发现已配对设备: " + name + " (" + address + ")");
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                scanButton.setText("扫描设备");
                if (targetDevice == null) {
                    deviceText.setText("未找到已配对设备，请先在系统设置中配对电脑");
                    addDebug("扫描完成，未找到已配对设备");
                } else {
                    addDebug("扫描完成，已找到设备");
                }
            }
        }
    };

    private void scanDevices() {
        if (bluetoothAdapter == null) return;

        if (!bluetoothAdapter.isEnabled()) {
            addDebug("蓝牙未开启，正在请求开启");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        addDebug("开始扫描蓝牙设备...");
        targetDevice = null;
        connectButton.setEnabled(false);
        deviceText.setText("正在扫描...");
        scanButton.setText("扫描中...");

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        addDebug("已配对设备数量: " + pairedDevices.size());
        
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String name = device.getName();
                String address = device.getAddress();
                addDebug("发现已配对设备: " + name + " (" + address + ")");
                if (name != null) {
                    targetDevice = device;
                    deviceText.setText("已配对设备: " + name + " (" + address + ")");
                    connectButton.setEnabled(true);
                    scanButton.setText("扫描设备");
                    return;
                }
            }
        }

        addDebug("开始蓝牙发现...");
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
    }

    private void connectHid() {
        if (targetDevice == null) {
            Toast.makeText(this, "请先选择要连接的设备", Toast.LENGTH_SHORT).show();
            addDebug("错误: 未选择目标设备");
            return;
        }

        statusText.setText("正在连接...");
        connectButton.setEnabled(false);
        addDebug("正在连接HID服务...");

        bluetoothAdapter.getProfileProxy(this, profileListener, BluetoothProfile.HID_DEVICE);
    }

    void registerHidApp() {
        if (hidDevice == null) {
            addDebug("错误: HID设备为空");
            return;
        }

        addDebug("正在注册HID应用...");

        byte[] descriptor = new byte[] {
            0x05, 0x01, 0x09, 0x06, (byte) 0xa1, 0x01,
            0x05, 0x07, 0x19, (byte) 0xe0, 0x29, (byte) 0xe7,
            0x15, 0x00, 0x25, 0x01, 0x75, 0x01, (byte) 0x95, (byte) 0x08,
            (byte) 0x81, 0x02, (byte) 0x95, 0x01, 0x75, (byte) 0x08, (byte) 0x81, 0x01,
            (byte) 0x95, 0x05, 0x75, 0x01, 0x05, (byte) 0x08, 0x19, 0x01,
            0x29, 0x05, (byte) 0x91, 0x02, (byte) 0x95, 0x01, 0x75, 0x03,
            (byte) 0x91, 0x01, (byte) 0x95, 0x06, 0x75, (byte) 0x08, 0x15, 0x00,
            0x26, (byte) 0xff, 0x00, 0x05, 0x07, 0x19, 0x00,
            0x2a, (byte) 0xff, 0x00, (byte) 0x81, 0x00, (byte) 0xc0
        };

        BluetoothHidDeviceAppSdpSettings sdpSettings = new BluetoothHidDeviceAppSdpSettings(
            "Android Keyboard",
            "Android Bluetooth Keyboard",
            "Android",
            (byte) 0x40,
            descriptor
        );

        addDebug("注册HID应用到设备...");
        hidDevice.registerApp(sdpSettings, null, null, new DirectExecutor(), hidCallback);
    }

    private void disconnectHid() {
        if (hidDevice != null && targetDevice != null) {
            addDebug("正在断开连接...");
            hidDevice.disconnect(targetDevice);
            hidDevice.unregisterApp();
        }
        isConnected = false;
        statusText.setText("未连接");
        statusText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        connectButton.setText("连接");
        sendButton.setEnabled(false);
        addDebug("已断开连接");
    }

    private void sendText() {
        addDebug("=== 发送文字按钮被点击 ===");
        
        if (!isConnected) {
            Toast.makeText(this, "请先连接设备", Toast.LENGTH_SHORT).show();
            addDebug("错误: 未连接设备 (isConnected=" + isConnected + ")");
            return;
        }
        addDebug("检查: 已连接设备");

        if (hidDevice == null) {
            Toast.makeText(this, "HID设备未初始化", Toast.LENGTH_SHORT).show();
            addDebug("错误: HID设备为空");
            return;
        }
        addDebug("检查: HID设备已就绪");

        if (targetDevice == null) {
            Toast.makeText(this, "目标设备为空", Toast.LENGTH_SHORT).show();
            addDebug("错误: 目标设备为空");
            return;
        }
        addDebug("检查: 目标设备: " + targetDevice.getName() + " (" + targetDevice.getAddress() + ")");

        final String text = inputEditText.getText().toString();
        if (text.isEmpty()) {
            Toast.makeText(this, "请输入要发送的文字", Toast.LENGTH_SHORT).show();
            addDebug("错误: 输入为空");
            return;
        }

        addDebug("开始发送文字: \"" + text + "\" (长度: " + text.length() + ")");

        new Thread(new Runnable() {
            @Override
            public void run() {
                addDebug("发送线程已启动");
                for (int i = 0; i < text.length(); i++) {
                    char c = text.charAt(i);
                    addDebug("处理字符 #" + i + ": '" + c + "' (ASCII: " + (int)c + ")");
                    boolean success = sendKey(c);
                    if (success) {
                        addDebug("字符 #" + i + " 发送成功: '" + c + "'");
                    } else {
                        addDebug("字符 #" + i + " 发送失败: '" + c + "'");
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        addDebug("发送线程被中断");
                        break;
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "文字已发送", Toast.LENGTH_SHORT).show();
                        addDebug("=== 文字发送完成 ===");
                        inputEditText.setText("");
                    }
                });
            }
        }).start();
    }

    private boolean sendKey(char c) {
        if (hidDevice == null) {
            addDebug("sendKey失败: hidDevice为空");
            return false;
        }
        if (targetDevice == null) {
            addDebug("sendKey失败: targetDevice为空");
            return false;
        }
        addDebug("sendKey: 准备发送字符 '" + c + "'");

        byte modifier = 0;
        byte key = 0;
        String keyName = "";

        if (c >= 'a' && c <= 'z') {
            key = (byte) (c - 'a' + 0x04);
            keyName = "字母键";
        } else if (c >= 'A' && c <= 'Z') {
            modifier = 0x02;
            key = (byte) (c - 'A' + 0x04);
            keyName = "大写字母键(Shift)";
        } else if (c >= '1' && c <= '9') {
            key = (byte) (c - '1' + 0x1e);
            keyName = "数字键";
        } else if (c == '0') {
            key = 0x27;
            keyName = "数字0";
        } else if (c == ' ') {
            key = 0x2c;
            keyName = "空格键";
        } else if (c == '\n') {
            key = 0x28;
            keyName = "回车键";
        } else if (c == '.') {
            key = 0x37;
            keyName = "句号";
        } else if (c == ',') {
            key = 0x36;
            keyName = "逗号";
        } else if (c == '-') {
            key = 0x2d;
            keyName = "减号";
        } else if (c == '=') {
            key = 0x2e;
            keyName = "等号";
        } else {
            key = 0x2c;
            keyName = "未知字符(替换为空格)";
            addDebug("sendKey: 未知字符 '" + c + "' (ASCII:" + (int)c + "), 替换为空格");
        }

        addDebug("sendKey: 字符='" + c + "', 修饰符=0x" + Integer.toHexString(modifier) + ", 键码=0x" + Integer.toHexString(key & 0xFF) + ", 类型=" + keyName);

        try {
            byte[] report = new byte[] {modifier, 0, key, 0, 0, 0, 0, 0};
            addDebug("sendKey: 发送按下报告: " + bytesToHex(report));
            hidDevice.sendReport(targetDevice, 0, report);

            Thread.sleep(10);

            byte[] releaseReport = new byte[] {0, 0, 0, 0, 0, 0, 0, 0};
            addDebug("sendKey: 发送释放报告: " + bytesToHex(releaseReport));
            hidDevice.sendReport(targetDevice, 0, releaseReport);
            
            addDebug("sendKey: 字符 '" + c + "' 发送成功");
            return true;
        } catch (Exception e) {
            addDebug("sendKey失败: " + e.getClass().getName() + " - " + e.getMessage());
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    void setHidDevice(BluetoothHidDevice device) {
        this.hidDevice = device;
        addDebug("HID设备已设置");
    }

    BluetoothDevice getTargetDevice() {
        return targetDevice;
    }

    void setConnected(boolean connected) {
        this.isConnected = connected;
        if (connected) {
            statusText.setText("已连接");
            statusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            connectButton.setText("断开连接");
            connectButton.setEnabled(true);
            sendButton.setEnabled(true);
            addDebug("蓝牙键盘已成功连接");
            Toast.makeText(this, "蓝牙键盘已连接", Toast.LENGTH_SHORT).show();
        } else {
            statusText.setText("未连接");
            statusText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            connectButton.setText("连接");
            connectButton.setEnabled(true);
            sendButton.setEnabled(false);
            addDebug("蓝牙已断开");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(bluetoothReceiver);
        } catch (Exception e) { }
        if (hidDevice != null) {
            try {
                hidDevice.unregisterApp();
            } catch (Exception e) { }
        }
    }
}

class DirectExecutor implements java.util.concurrent.Executor {
    @Override
    public void execute(Runnable command) {
        command.run();
    }
}

class HidCallback extends BluetoothHidDevice.Callback {
    private MainActivity activity;

    HidCallback(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onAppStatusChanged(BluetoothDevice pluggedDevice, boolean registered) {
        if (registered) {
            activity.addDebug("HID应用注册成功");
            if (activity.getTargetDevice() != null) {
                activity.addDebug("正在连接到目标设备...");
                try {
                    activity.hidDevice.connect(activity.getTargetDevice());
                } catch (Exception e) {
                    activity.addDebug("连接失败: " + e.getMessage());
                }
            }
        } else {
            activity.addDebug("HID应用未注册");
        }
    }

    @Override
    public void onConnectionStateChanged(BluetoothDevice device, int state) {
        activity.addDebug("连接状态变化: " + state);
        if (state == BluetoothProfile.STATE_CONNECTED) {
            activity.setConnected(true);
        } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
            activity.setConnected(false);
        } else if (state == BluetoothProfile.STATE_CONNECTING) {
            activity.addDebug("正在连接中...");
        } else {
            activity.addDebug("未知状态: " + state);
        }
    }

    @Override
    public void onGetReport(BluetoothDevice device, byte type, byte id, int bufferSize) {
        activity.addDebug("收到GetReport请求: type=" + type + ", id=" + id);
    }

    @Override
    public void onSetReport(BluetoothDevice device, byte type, byte id, byte[] data) {
        activity.addDebug("收到SetReport请求: type=" + type + ", id=" + id);
    }

    @Override
    public void onOutputReport(BluetoothDevice device, byte id, byte[] data) {
        activity.addDebug("收到OutputReport: id=" + id);
    }
}

class ProfileListener implements BluetoothProfile.ServiceListener {
    private MainActivity activity;

    ProfileListener(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onServiceConnected(int profile, BluetoothProfile proxy) {
        activity.addDebug("HID服务连接成功");
        if (profile == BluetoothProfile.HID_DEVICE) {
            activity.setHidDevice((BluetoothHidDevice) proxy);
            activity.registerHidApp();
        }
    }

    @Override
    public void onServiceDisconnected(int profile) {
        activity.addDebug("HID服务断开");
        if (profile == BluetoothProfile.HID_DEVICE) {
            activity.setHidDevice(null);
        }
    }
}
