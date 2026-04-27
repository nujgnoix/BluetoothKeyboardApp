package com.example.bluetoothkeyboardapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements BluetoothHidService.OnConnectionStateChangeListener {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSIONS = 2;

    private TextView statusText;
    private Button connectButton;
    private EditText inputEditText;
    private Button sendButton;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothHidService bluetoothHidService;
    private InputHandler inputHandler;
    private ConnectionManager connectionManager;
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.status_text);
        connectButton = findViewById(R.id.connect_button);
        inputEditText = findViewById(R.id.input_edit_text);
        sendButton = findViewById(R.id.send_button);

        // Initialize Bluetooth
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // Check if Bluetooth is supported
        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.bluetooth_not_available, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize services
        bluetoothHidService = new BluetoothHidService(this);
        bluetoothHidService.setConnectionListener(this);
        inputHandler = new InputHandler(bluetoothHidService);
        connectionManager = new ConnectionManager(this, bluetoothHidService);

        // Request permissions
        requestPermissions();

        // Set button click listeners
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isConnected) {
                    connectBluetooth();
                } else {
                    disconnectBluetooth();
                }
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendText();
            }
        });
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, REQUEST_PERMISSIONS);
        }
    }

    private void connectBluetooth() {
        // Check if Bluetooth is enabled
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        statusText.setText(R.string.status_connecting);
        statusText.setTextColor(getResources().getColor(R.color.orange));
        connectButton.setEnabled(false); // Disable button during connection

        // Initialize and register HID service
        if (bluetoothHidService.initialize()) {
            bluetoothHidService.registerApp();
            Toast.makeText(this, "Bluetooth keyboard service started. Please pair from your PC/Mac", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Failed to initialize Bluetooth HID service", Toast.LENGTH_SHORT).show();
            statusText.setText(R.string.status_not_connected);
            statusText.setTextColor(getResources().getColor(R.color.gray));
            connectButton.setEnabled(true); // Re-enable button
        }
    }

    private void disconnectBluetooth() {
        connectButton.setEnabled(false); // Disable button during disconnection
        if (bluetoothHidService != null) {
            bluetoothHidService.unregisterApp();
        }
        isConnected = false;
        statusText.setText(R.string.status_not_connected);
        statusText.setTextColor(getResources().getColor(R.color.gray));
        connectButton.setText(R.string.connect_button);
        connectButton.setEnabled(true); // Re-enable button
        Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
    }

    private void sendText() {
        if (!isConnected) {
            Toast.makeText(this, "Not connected to PC", Toast.LENGTH_SHORT).show();
            return;
        }

        String text = inputEditText.getText().toString();
        if (text.isEmpty()) {
            Toast.makeText(this, "Please enter text", Toast.LENGTH_SHORT).show();
            return;
        }

        if (inputHandler.sendText(text)) {
            Toast.makeText(this, "Text sent successfully", Toast.LENGTH_SHORT).show();
            // Clear input after sending
            inputEditText.setText("");
        } else {
            Toast.makeText(this, "Failed to send text", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionStateChanged(boolean connected, BluetoothDevice device) {
        isConnected = connected;
        if (connected) {
            statusText.setText(R.string.status_connected);
            statusText.setTextColor(getResources().getColor(R.color.green));
            connectButton.setText(R.string.disconnect_button);
            connectButton.setEnabled(true); // Re-enable button
            Toast.makeText(this, "Connected to " + (device != null ? device.getName() : "PC"), Toast.LENGTH_SHORT).show();
        } else {
            statusText.setText(R.string.status_not_connected);
            statusText.setTextColor(getResources().getColor(R.color.gray));
            connectButton.setText(R.string.connect_button);
            connectButton.setEnabled(true); // Re-enable button
            Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                connectBluetooth();
            } else {
                Toast.makeText(this, "Bluetooth is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothHidService != null) {
            bluetoothHidService.close();
        }
        if (connectionManager != null) {
            connectionManager.close();
        }
    }
}
