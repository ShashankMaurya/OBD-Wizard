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

import com.diy.obdwizard.dtc.DtcHelper;
import com.diy.obdwizard.obd.ObdConnectionManager;
import com.diy.obdwizard.obd.interfaces.ObdManagerCallbacks;
import com.diy.obdwizard.obd.models.MotorcycleTelemetry;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements ObdManagerCallbacks.ConnectionListener {

    private TextView txtStatus, txtGear, txtSpeed, txtRpm, txtTemp, txtThrottle;
    private TextView txtMap, txtIat, txtLoad, txtO2, txtBatt, txtTiming, txtRuntime, txtMilDist;
    private Button btnConnect, btnDtc, btnDtcDictionary;

    private ObdConnectionManager obdManager;

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

        txtMap = findViewById(R.id.txtMap);
        txtIat = findViewById(R.id.txtIat);
        txtLoad = findViewById(R.id.txtLoad);
        txtO2 = findViewById(R.id.txtO2);
        txtBatt = findViewById(R.id.txtBatt);
        txtTiming = findViewById(R.id.txtTiming);
        txtRuntime = findViewById(R.id.txtRuntime);
        txtMilDist = findViewById(R.id.txtMilDist);

        btnConnect = findViewById(R.id.btnConnect);
        btnDtc = findViewById(R.id.btnDtc);
        btnDtcDictionary = findViewById(R.id.btnDtcDictionary);

        obdManager = new ObdConnectionManager(this);

        // Pre-load the DTC map in a background thread so it's instantly ready when requested
        obdManager.executeInBackground(() -> DtcHelper.preloadDtcMap(getApplicationContext()));

        checkPermissions();

        btnConnect.setOnClickListener(v -> {
            if (obdManager.isConnected()) {
                obdManager.disconnect();
            } else {
                startConnection();
            }
        });

        btnDtc.setOnClickListener(v -> openDtcDialog());
        btnDtcDictionary.setOnClickListener(v -> showDtcDictionaryDialog());
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
        obdManager.connect(targetDevice);
    }

    @Override
    public void onConnectionStateChanged(boolean isConnected) {
        runOnUiThread(() -> {
            if (isConnected) {
                txtStatus.setText(R.string.status_connected);
                txtStatus.setTextColor(getColor(R.color.accent_green));
                btnConnect.setText(R.string.btn_disconnect);
            } else {
                txtStatus.setText(R.string.status_disconnected);
                txtStatus.setTextColor(getColor(R.color.accent_red));
                btnConnect.setText(R.string.btn_connect);
            }
        });
    }

    @Override
    public void onTelemetryDataReceived(MotorcycleTelemetry data) {
        runOnUiThread(() -> {
            txtGear.setText(data.calculatedGear);
            txtGear.setTextColor(data.calculatedGear.equals("N") ? getColor(R.color.accent_green) : getColor(R.color.text_white));
            txtSpeed.setText(String.valueOf(data.speedKmh));

            txtRpm.setText(String.valueOf(data.rpm));
            txtTemp.setText(data.engineTempC + " °C");
            txtThrottle.setText((int) data.throttlePercent + " %");

            txtMap.setText(String.valueOf(data.mapKpa));
            txtIat.setText(data.iatTempC + " °C");
            txtLoad.setText((int) data.engineLoadPercent + " %");

            txtO2.setText(String.format(Locale.US, "%.2f V", data.o2Voltage));
            txtBatt.setText(String.format(Locale.US, "%.1f V", data.controlModuleVoltage));
            txtTiming.setText(String.format(Locale.US, "%.1f°", data.timingAdvance));

            txtRuntime.setText(data.runTimeSeconds + " s");
            txtMilDist.setText(data.distanceWithMilKm + " km");
        });
    }

    @Override
    public void onError(String message) {
        runOnUiThread(() -> {
            if (message.equals("Connection Failed")) {
                txtStatus.setText("Connection Failed");
                txtStatus.setTextColor(getColor(R.color.accent_red));
            } else {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openDtcDialog() {
        obdManager.readDtc(new ObdManagerCallbacks.DtcReadListener() {
            @Override
            public void onDtcRead(List<String> codes) {
                runOnUiThread(() -> {
                    StringBuilder sb = new StringBuilder();
                    if (codes.isEmpty()) {
                        sb.append("No Fault Codes Found (ECU Clean)");
                    } else {
                        for (String code : codes) {
                            sb.append("• ").append(code).append(": ")
                                    .append(DtcHelper.getDtcDescription(MainActivity.this, code)).append("\n");
                        }
                    }

                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Royal Enfield DTC Diagnostics")
                            .setMessage(sb.toString())
                            .setPositiveButton("Clear Codes", (dialog, which) -> {
                                obdManager.clearDtc(success -> runOnUiThread(() ->
                                        Toast.makeText(MainActivity.this, success ? "Codes Cleared" : "Clear Failed", Toast.LENGTH_SHORT).show()
                                ));
                            })
                            .setNegativeButton("Close", null)
                            .show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void showDtcDictionaryDialog() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : DtcHelper.getAllKnownDtcs(this).entrySet()) {
            sb.append("• ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n\n");
        }
        new AlertDialog.Builder(this)
                .setTitle("DTC Dictionary")
                .setMessage(sb.toString().trim())
                .setPositiveButton("Close", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        obdManager.destroy();
    }
}
