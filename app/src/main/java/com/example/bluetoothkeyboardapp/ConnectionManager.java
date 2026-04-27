package com.example.bluetoothkeyboardapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ConnectionManager {

    private static final String TAG = "ConnectionManager";

    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothHidService bluetoothHidService;

    private List<BluetoothDevice> pairedDevices = new ArrayList<>();
    private List<BluetoothDevice> discoveredDevices = new ArrayList<>();

    private OnDeviceDiscoveryListener discoveryListener;
    private OnConnectionStateChangeListener connectionListener;

    public interface OnDeviceDiscoveryListener {
        void onDeviceDiscovered(BluetoothDevice device);
        void onDiscoveryFinished();
    }

    public interface OnConnectionStateChangeListener {
        void onConnectionStateChanged(boolean connected, BluetoothDevice device);
    }

    public ConnectionManager(Context context, BluetoothHidService bluetoothHidService) {
        this.context = context;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.bluetoothHidService = bluetoothHidService;
        registerReceivers();
    }

    private void registerReceivers() {
        // Register for Bluetooth device discovery broadcasts
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(receiver, filter);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && !discoveredDevices.contains(device)) {
                    discoveredDevices.add(device);
                    if (discoveryListener != null) {
                        discoveryListener.onDeviceDiscovered(device);
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                // Discovery has finished
                if (discoveryListener != null) {
                    discoveryListener.onDiscoveryFinished();
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                // Bond state changed
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                int previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                Log.d(TAG, "Bond state changed for " + device.getName() + ": " + previousBondState + " -> " + bondState);
            }
        }
    };

    /**
     * Start discovering Bluetooth devices
     * @return true if discovery started successfully, false otherwise
     */
    public boolean startDiscovery() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Log.e(TAG, "Bluetooth is not enabled");
            return false;
        }

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        discoveredDevices.clear();
        return bluetoothAdapter.startDiscovery();
    }

    /**
     * Cancel Bluetooth device discovery
     */
    public void cancelDiscovery() {
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }

    /**
     * Get list of paired Bluetooth devices
     * @return List of paired devices
     */
    public List<BluetoothDevice> getPairedDevices() {
        if (bluetoothAdapter == null) {
            return new ArrayList<>();
        }
        pairedDevices.clear();
        pairedDevices.addAll(bluetoothAdapter.getBondedDevices());
        return pairedDevices;
    }

    /**
     * Get list of discovered Bluetooth devices
     * @return List of discovered devices
     */
    public List<BluetoothDevice> getDiscoveredDevices() {
        return discoveredDevices;
    }

    /**
     * Pair with a Bluetooth device
     * @param device The device to pair with
     * @return true if pairing started, false otherwise
     */
    public boolean pairDevice(BluetoothDevice device) {
        if (device == null) {
            Log.e(TAG, "Device is null");
            return false;
        }

        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
            Log.d(TAG, "Device is already paired");
            return true;
        }

        try {
            // Start pairing process
            device.createBond();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error pairing device: " + e.getMessage());
            return false;
        }
    }

    /**
     * Connect to a Bluetooth device
     * @param device The device to connect to
     * @return true if connection started, false otherwise
     */
    public boolean connectDevice(BluetoothDevice device) {
        if (device == null) {
            Log.e(TAG, "Device is null");
            return false;
        }

        if (bluetoothHidService == null) {
            Log.e(TAG, "BluetoothHidService is not initialized");
            return false;
        }

        // For HID device, the connection is typically initiated from the host device
        // The Android device acts as a peripheral
        Log.d(TAG, "Putting HID device in discoverable mode");
        return true;
    }

    /**
     * Disconnect from the current Bluetooth device
     */
    public void disconnectDevice() {
        if (bluetoothHidService != null) {
            bluetoothHidService.unregisterApp();
        }
    }

    /**
     * Set device discovery listener
     * @param listener The discovery listener
     */
    public void setDiscoveryListener(OnDeviceDiscoveryListener listener) {
        this.discoveryListener = listener;
    }

    /**
     * Set connection state listener
     * @param listener The connection listener
     */
    public void setConnectionListener(OnConnectionStateChangeListener listener) {
        this.connectionListener = listener;
    }

    /**
     * Check if Bluetooth is enabled
     * @return true if Bluetooth is enabled, false otherwise
     */
    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    /**
     * Enable Bluetooth
     * @return true if Bluetooth is being enabled, false otherwise
     */
    public boolean enableBluetooth() {
        if (bluetoothAdapter == null) {
            return false;
        }
        return bluetoothAdapter.enable();
    }

    /**
     * Clean up resources
     */
    public void close() {
        try {
            context.unregisterReceiver(receiver);
        } catch (Exception e) {
            Log.e(TAG, "Error unregistering receiver: " + e.getMessage());
        }
        cancelDiscovery();
    }
}
