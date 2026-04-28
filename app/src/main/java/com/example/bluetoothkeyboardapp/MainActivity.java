package com.example.bluetoothkeyboardapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TextView statusText;
    private Button connectButton;
    private EditText inputEditText;
    private Button sendButton;
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.status_text);
        connectButton = findViewById(R.id.connect_button);
        inputEditText = findViewById(R.id.input_edit_text);
        sendButton = findViewById(R.id.send_button);

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

    private void connectBluetooth() {
        isConnected = true;
        statusText.setText(R.string.status_connected);
        statusText.setTextColor(getResources().getColor(R.color.green));
        connectButton.setText(R.string.disconnect_button);
        Toast.makeText(this, "Connected to PC/Mac", Toast.LENGTH_SHORT).show();
    }

    private void disconnectBluetooth() {
        isConnected = false;
        statusText.setText(R.string.status_not_connected);
        statusText.setTextColor(getResources().getColor(R.color.gray));
        connectButton.setText(R.string.connect_button);
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

        Toast.makeText(this, "Text sent: " + text, Toast.LENGTH_SHORT).show();
        inputEditText.setText("");
    }
}
