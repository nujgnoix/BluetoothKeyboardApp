package com.example.bluetoothkeyboardapp;

import android.util.Log;

public class InputHandler {

    private static final String TAG = "InputHandler";

    private BluetoothHidService bluetoothHidService;

    public InputHandler(BluetoothHidService bluetoothHidService) {
        this.bluetoothHidService = bluetoothHidService;
    }

    /**
     * Process and send text via Bluetooth HID
     * @param text The text to send
     * @return true if sending was successful, false otherwise
     */
    public boolean sendText(String text) {
        if (text == null || text.isEmpty()) {
            Log.e(TAG, "Empty text to send");
            return false;
        }

        if (bluetoothHidService == null) {
            Log.e(TAG, "BluetoothHidService is not initialized");
            return false;
        }

        if (!bluetoothHidService.isConnected()) {
            Log.e(TAG, "Not connected to any device");
            return false;
        }

        Log.d(TAG, "Sending text: " + text);
        return bluetoothHidService.sendKeyboardEvent(text);
    }

    /**
     * Process special keys and send them
     * @param keyCode The special key code
     * @return true if sending was successful, false otherwise
     */
    public boolean sendSpecialKey(int keyCode) {
        if (bluetoothHidService == null) {
            Log.e(TAG, "BluetoothHidService is not initialized");
            return false;
        }

        if (!bluetoothHidService.isConnected()) {
            Log.e(TAG, "Not connected to any device");
            return false;
        }

        // Convert key code to appropriate string representation
        String keyString = getSpecialKeyString(keyCode);
        if (keyString == null) {
            Log.e(TAG, "Unknown special key code: " + keyCode);
            return false;
        }

        Log.d(TAG, "Sending special key: " + keyString);
        return bluetoothHidService.sendKeyboardEvent(keyString);
    }

    /**
     * Get string representation of special keys
     * @param keyCode The key code
     * @return String representation of the key
     */
    private String getSpecialKeyString(int keyCode) {
        switch (keyCode) {
            case android.view.KeyEvent.KEYCODE_ENTER:
                return "\n";
            case android.view.KeyEvent.KEYCODE_TAB:
                return "\t";
            case android.view.KeyEvent.KEYCODE_SPACE:
                return " ";
            default:
                return null;
        }
    }

    /**
     * Clear the input field (if needed)
     */
    public void clearInput() {
        // This method can be used to clear the input field in the UI
        // Implementation depends on the UI component
    }

    /**
     * Set the BluetoothHidService instance
     * @param bluetoothHidService The BluetoothHidService instance
     */
    public void setBluetoothHidService(BluetoothHidService bluetoothHidService) {
        this.bluetoothHidService = bluetoothHidService;
    }

    /**
     * Check if the input handler is ready to send text
     * @return true if ready, false otherwise
     */
    public boolean isReady() {
        return bluetoothHidService != null && bluetoothHidService.isConnected();
    }
}
