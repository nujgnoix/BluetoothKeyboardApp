package com.example.bluetoothkeyboardapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidDevice;
import android.bluetooth.BluetoothHidDeviceAppSdpSettings;
import android.bluetooth.BluetoothHidDeviceCallback;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BluetoothHidService {

    private static final String TAG = "BluetoothHidService";

    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothHidDevice bluetoothHidDevice;
    private BluetoothHidDeviceCallback callback;
    private ExecutorService executorService;
    private Handler handler;

    private boolean isInitialized = false;
    private boolean isConnected = false;
    private BluetoothDevice connectedDevice;

    private OnConnectionStateChangeListener connectionListener;

    public interface OnConnectionStateChangeListener {
        void onConnectionStateChanged(boolean connected, BluetoothDevice device);
    }

    public BluetoothHidService(Context context) {
        this.context = context;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.executorService = Executors.newSingleThreadExecutor();
        this.handler = new Handler(Looper.getMainLooper());
        initCallback();
    }

    private void initCallback() {
        callback = new BluetoothHidDeviceCallback() {
            @Override
            public void onAppStatusChanged(BluetoothDevice device, boolean registered) {
                super.onAppStatusChanged(device, registered);
                Log.d(TAG, "onAppStatusChanged: " + registered);
            }

            @Override
            public void onConnectionStateChanged(BluetoothDevice device, int state) {
                super.onConnectionStateChanged(device, state);
                Log.d(TAG, "onConnectionStateChanged: " + state);

                if (state == BluetoothProfile.STATE_CONNECTED) {
                    isConnected = true;
                    connectedDevice = device;
                    notifyConnectionStateChanged(true, device);
                } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                    isConnected = false;
                    connectedDevice = null;
                    notifyConnectionStateChanged(false, null);
                }
            }

            @Override
            public void onGetReport(BluetoothDevice device, byte type, byte id, int bufferSize) {
                super.onGetReport(device, type, id, bufferSize);
                // Handle get report request if needed
            }

            @Override
            public void onSetReport(BluetoothDevice device, byte type, byte id, byte[] data) {
                super.onSetReport(device, type, id, data);
                // Handle set report request if needed
            }

            @Override
            public void onOutputReport(BluetoothDevice device, byte id, byte[] data) {
                super.onOutputReport(device, id, data);
                // Handle output report if needed
            }
        };
    }

    public boolean initialize() {
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth adapter is null");
            return false;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Log.e(TAG, "Bluetooth is not enabled");
            return false;
        }

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                boolean success = bluetoothAdapter.getProfileProxy(context, new BluetoothProfile.ServiceListener() {
                    @Override
                    public void onServiceConnected(int profile, BluetoothProfile proxy) {
                        if (profile == BluetoothProfile.HID_DEVICE) {
                            bluetoothHidDevice = (BluetoothHidDevice) proxy;
                            isInitialized = true;
                            Log.d(TAG, "HID device service connected");
                        }
                    }

                    @Override
                    public void onServiceDisconnected(int profile) {
                        if (profile == BluetoothProfile.HID_DEVICE) {
                            bluetoothHidDevice = null;
                            isInitialized = false;
                            Log.d(TAG, "HID device service disconnected");
                        }
                    }
                }, BluetoothProfile.HID_DEVICE);
            }
        });

        return true;
    }

    public void registerApp() {
        if (!isInitialized || bluetoothHidDevice == null) {
            Log.e(TAG, "HID device not initialized");
            return;
        }

        // Register HID device
        BluetoothHidDeviceAppSdpSettings sdpSettings = new BluetoothHidDeviceAppSdpSettings(
                "Bluetooth Keyboard",
                "Android",
                "1.0",
                BluetoothHidDevice.SUBCLASS1_COMBO,
                getHidReportDescriptor()
        );

        bluetoothHidDevice.registerApp(sdpSettings, executorService, callback);
        Log.d(TAG, "HID app registered");
    }

    public void unregisterApp() {
        if (bluetoothHidDevice != null) {
            bluetoothHidDevice.unregisterApp();
            Log.d(TAG, "HID app unregistered");
        }
    }

    public boolean sendKeyboardEvent(String text) {
        if (!isConnected || bluetoothHidDevice == null || connectedDevice == null) {
            Log.e(TAG, "Not connected or HID device not initialized");
            return false;
        }

        // Convert text to keyboard events
        for (char c : text.toCharArray()) {
            byte[] report = createKeyboardReport(c);
            if (!bluetoothHidDevice.sendReport(connectedDevice, BluetoothHidDevice.REPORT_TYPE_INPUT, report)) {
                Log.e(TAG, "Failed to send keyboard event");
                return false;
            }

            // Send release report
            byte[] releaseReport = createReleaseReport();
            bluetoothHidDevice.sendReport(connectedDevice, BluetoothHidDevice.REPORT_TYPE_INPUT, releaseReport);

            // Small delay to simulate typing
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    private byte[] createKeyboardReport(char c) {
        // Standard keyboard report format: [modifier, reserved, key1, key2, key3, key4, key5, key6]
        byte[] report = new byte[8];

        // Map character to key code
        int keyCode = getKeyCode(c);
        if (keyCode > 0) {
            report[2] = (byte) keyCode;
        }

        return report;
    }

    private byte[] createReleaseReport() {
        // All zeros to release all keys
        return new byte[8];
    }

    private int getKeyCode(char c) {
        // Simple key code mapping
        // This is a basic implementation, you may need to expand it
        switch (c) {
            case 'a': return 0x04;
            case 'b': return 0x05;
            case 'c': return 0x06;
            case 'd': return 0x07;
            case 'e': return 0x08;
            case 'f': return 0x09;
            case 'g': return 0x0A;
            case 'h': return 0x0B;
            case 'i': return 0x0C;
            case 'j': return 0x0D;
            case 'k': return 0x0E;
            case 'l': return 0x0F;
            case 'm': return 0x10;
            case 'n': return 0x11;
            case 'o': return 0x12;
            case 'p': return 0x13;
            case 'q': return 0x14;
            case 'r': return 0x15;
            case 's': return 0x16;
            case 't': return 0x17;
            case 'u': return 0x18;
            case 'v': return 0x19;
            case 'w': return 0x1A;
            case 'x': return 0x1B;
            case 'y': return 0x1C;
            case 'z': return 0x1D;
            case '1': return 0x1E;
            case '2': return 0x1F;
            case '3': return 0x20;
            case '4': return 0x21;
            case '5': return 0x22;
            case '6': return 0x23;
            case '7': return 0x24;
            case '8': return 0x25;
            case '9': return 0x26;
            case '0': return 0x27;
            case ' ': return 0x2C;
            case '\n': return 0x28;
            case '\t': return 0x2B;
            case '-': return 0x2D;
            case '=': return 0x2E;
            case '[': return 0x2F;
            case ']': return 0x30;
            case '\\': return 0x31;
            case ';': return 0x33;
            case '\'': return 0x34;
            case '`': return 0x35;
            case ',': return 0x36;
            case '.': return 0x37;
            case '/': return 0x38;
            default: return 0;
        }
    }

    private byte[] getHidReportDescriptor() {
        // Standard keyboard HID report descriptor
        return new byte[]{
                0x05, 0x01, // Usage Page (Generic Desktop)
                0x09, 0x06, // Usage (Keyboard)
                0xA1, 0x01, // Collection (Application)
                0x05, 0x07, // Usage Page (Keyboard/Keypad)
                0x19, 0xE0, // Usage Minimum (224)
                0x29, 0xE7, // Usage Maximum (231)
                0x15, 0x00, // Logical Minimum (0)
                0x25, 0x01, // Logical Maximum (1)
                0x75, 0x01, // Report Size (1)
                0x95, 0x08, // Report Count (8)
                0x81, 0x02, // Input (Data,Var,Abs)
                0x95, 0x01, // Report Count (1)
                0x75, 0x08, // Report Size (8)
                0x81, 0x01, // Input (Const,Array,Abs)
                0x95, 0x06, // Report Count (6)
                0x75, 0x08, // Report Size (8)
                0x15, 0x00, // Logical Minimum (0)
                0x25, 0x65, // Logical Maximum (101)
                0x05, 0x07, // Usage Page (Keyboard/Keypad)
                0x19, 0x00, // Usage Minimum (0)
                0x29, 0x65, // Usage Maximum (101)
                0x81, 0x00, // Input (Data,Array,Abs)
                0xC0        // End Collection
        };
    }

    public void setConnectionListener(OnConnectionStateChangeListener listener) {
        this.connectionListener = listener;
    }

    private void notifyConnectionStateChanged(final boolean connected, final BluetoothDevice device) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (connectionListener != null) {
                    connectionListener.onConnectionStateChanged(connected, device);
                }
            }
        });
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void close() {
        unregisterApp();
        if (bluetoothAdapter != null) {
            bluetoothAdapter.closeProfileProxy(BluetoothProfile.HID_DEVICE, bluetoothHidDevice);
        }
        executorService.shutdown();
    }
}
