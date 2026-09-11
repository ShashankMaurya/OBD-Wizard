package com.diy.obdwizard;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.diy.obdwizard.bluetooth.BluetoothConnection;
import com.diy.obdwizard.dtc.DtcHelper;
import com.diy.obdwizard.obd.RePidEngine;
import com.diy.obdwizard.obd.interfaces.IObdDataSource;
import com.diy.obdwizard.obd.models.MotorcycleTelemetry;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextView txtStatus, txtGear, txtSpeed, txtRpm, txtTemp, txtThrottle;
    private Button btnConnect, btnDtc;

    private IObdDataSource obdSource;
    private final ExecutorService workerThread = Executors.newSingleThreadExecutor();
    private volatile boolean isPolling = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtStatus = findViewById(R.id.txtStatus);
        txtGear = findViewById(R.id.txtGear);
        txtSpeed = findViewById(R.id.txtSpeed);
        txtRpm = findViewById(R.id.txtRpm);
        txtTemp = findViewById(R.id.txtTemp);
        txtThrottle = findViewById(R.id.txtThrottle);
        btnConnect = findViewById(R.id.btnConnect);
        btnDtc = findViewById(R.id.btnDtc);

        checkPermissions();

        btnConnect.setOnClickListener(v -> {
            if (isPolling) stopPolling();
            else startConnection();
        });

        btnDtc.setOnClickListener(v -> openDtcDialog());
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN
                }, 101);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void startConnection() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            Toast.makeText(this, "Enable Bluetooth first", Toast.LENGTH_SHORT).show();
            return;
        }

        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
        BluetoothDevice targetDevice = null;
        for (BluetoothDevice d : pairedDevices) {
            if (d.getName() != null && (d.getName().toUpperCase().contains("OBD") || d.getName().toUpperCase().contains("VLINKER"))) {
                targetDevice = d;
                break;
            }
        }

        if (targetDevice == null) {
            Toast.makeText(this, "Paired OBD scanner not found in settings", Toast.LENGTH_LONG).show();
            return;
        }

        txtStatus.setText(R.string.status_connecting);
        final BluetoothDevice finalDevice = targetDevice;

        workerThread.execute(() -> {
            BluetoothConnection bt = new BluetoothConnection(finalDevice);
            obdSource = new RePidEngine(bt);
            if (obdSource.connect()) {
                isPolling = true;
                runOnUiThread(() -> {
                    txtStatus.setText(R.string.status_connected);
                    txtStatus.setTextColor(getColor(R.color.accent_green));
                    btnConnect.setText(R.string.btn_disconnect);
                });
                runTelemetryLoop();
            } else {
                runOnUiThread(() -> {
                    txtStatus.setText("Connection Failed");
                    txtStatus.setTextColor(getColor(R.color.accent_red));
                });
            }
        });
    }

    private void runTelemetryLoop() {
        while (isPolling && obdSource != null && obdSource.isConnected()) {
            try {
                MotorcycleTelemetry data = obdSource.pollTelemetry();
                runOnUiThread(() -> {
                    txtGear.setText(data.calculatedGear);
                    txtGear.setTextColor(data.calculatedGear.equals("N") ? getColor(R.color.accent_green) : getColor(R.color.text_white));
                    txtSpeed.setText(String.valueOf(data.speedKmh));
                    txtRpm.setText(String.valueOf(data.rpm));
                    txtTemp.setText(data.engineTempC + " °C");
                    txtThrottle.setText((int) data.throttlePercent + " %");
                });
            } catch (IOException e) {
                handleUnexpectedDisconnect();
                break;
            }

            try {
                Thread.sleep(60);
            } catch (InterruptedException e) {
                break;
            }
        }
        if (isPolling && (obdSource == null || !obdSource.isConnected())) {
            handleUnexpectedDisconnect();
        }
    }

    private void handleUnexpectedDisconnect() {
        if (!isPolling) return;
        isPolling = false;
        if (obdSource != null) {
            obdSource.disconnect();
        }
        runOnUiThread(() -> {
            txtStatus.setText(R.string.status_disconnected);
            txtStatus.setTextColor(getColor(R.color.accent_red));
            btnConnect.setText(R.string.btn_connect);
            Toast.makeText(MainActivity.this, "OBD device disconnected", Toast.LENGTH_SHORT).show();
        });
    }

    private void stopPolling() {
        isPolling = false;
        workerThread.execute(() -> {
            if (obdSource != null) obdSource.disconnect();
            runOnUiThread(() -> {
                txtStatus.setText(R.string.status_disconnected);
                txtStatus.setTextColor(getColor(R.color.accent_red));
                btnConnect.setText(R.string.btn_connect);
            });
        });
    }

    private void openDtcDialog() {
        if (!isPolling || obdSource == null || !obdSource.isConnected()) {
            Toast.makeText(this, "Connect to ECU first", Toast.LENGTH_SHORT).show();
            return;
        }

        workerThread.execute(() -> {
            try {
                List<String> codes = obdSource.readDtcCodes();
                runOnUiThread(() -> {
                    StringBuilder sb = new StringBuilder();
                    if (codes.isEmpty()) {
                        sb.append("No Fault Codes Found (ECU Clean)");
                    } else {
                        for (String code : codes) {
                            sb.append("• ").append(code).append(": ")
                                    .append(DtcHelper.getDtcDescription(code)).append("\n");
                        }
                    }

                    new AlertDialog.Builder(this)
                            .setTitle("Royal Enfield DTC Diagnostics")
                            .setMessage(sb.toString())
                            .setPositiveButton("Clear Codes", (dialog, which) -> {
                                workerThread.execute(() -> {
                                    try {
                                        boolean cleared = obdSource.clearDtcCodes();
                                        runOnUiThread(() -> Toast.makeText(this, cleared ? "Codes Cleared" : "Clear Failed", Toast.LENGTH_SHORT).show());
                                    } catch (IOException e) {
                                        handleUnexpectedDisconnect();
                                    }
                                });
                            })
                            .setNegativeButton("Close", null)
                            .show();
                });
            } catch (IOException e) {
                handleUnexpectedDisconnect();
                runOnUiThread(() -> Toast.makeText(this, "Failed to read DTC codes: Device disconnected", Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPolling();
        workerThread.shutdown();
    }
}
