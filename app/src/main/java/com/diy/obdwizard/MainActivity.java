package com.diy.obdwizard;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.diy.obdwizard.dtc.DtcHelper;
import com.diy.obdwizard.obd.ObdConnectionManager;
import com.diy.obdwizard.obd.interfaces.ObdManagerCallbacks;
import com.diy.obdwizard.obd.interfaces.TelemetryUpdateListener;
import com.diy.obdwizard.obd.models.MotorcycleTelemetry;
import com.diy.obdwizard.ui.fragments.AboutFragment;
import com.diy.obdwizard.ui.fragments.AdvancedDashFragment;
import com.diy.obdwizard.ui.fragments.DashboardFragment;
import com.diy.obdwizard.ui.fragments.DtcFragment;
import com.diy.obdwizard.ui.fragments.HomeFragment;
import com.diy.obdwizard.ui.fragments.TripHistoryFragment;
import com.google.android.material.navigation.NavigationView;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements ObdManagerCallbacks.ConnectionListener {

    private DrawerLayout drawerLayout;
    private TextView txtGlobalStatus;
    private ObdConnectionManager obdManager;
    private long backPressedTime = 0;
    private Toast backToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        txtGlobalStatus = findViewById(R.id.txtGlobalStatus);
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        toggle.getDrawerArrowDrawable().setColor(getColor(R.color.text_white));
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navView.setNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            // Handle navigation item switching between Home, Default Dashboard, Advanced Dashboard, DTCs, and About
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_dashboard) {
                // Primary Default Dashboard with central RPM arc gauge & futuristic card layout
                selectedFragment = new DashboardFragment();
            } else if (itemId == R.id.nav_advanced_dashboard) {
                // Secondary Advanced Dashboard for secondary ECU telemetry & timers
                selectedFragment = new AdvancedDashFragment();
            } else if (itemId == R.id.nav_trip_history) {
                selectedFragment = new TripHistoryFragment();
            } else if (itemId == R.id.nav_dtc) {
                selectedFragment = new DtcFragment();
            } else if (itemId == R.id.nav_about) {
                selectedFragment = new AboutFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                setTitle(item.getTitle());
            }

            drawerLayout.closeDrawers();
            return true;
        });

        obdManager = new ObdConnectionManager(this);
        obdManager.executeInBackground(() -> DtcHelper.preloadDtcMap(getApplicationContext()));

        checkPermissions();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
            navView.setCheckedItem(R.id.nav_home);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    if (backPressedTime + 2000 > System.currentTimeMillis()) {
                        if (backToast != null) {
                            backToast.cancel();
                        }
                        finish();
                    } else {
                        backToast = Toast.makeText(MainActivity.this, "Press back again to exit", Toast.LENGTH_SHORT);
                        backToast.show();
                        backPressedTime = System.currentTimeMillis();
                    }
                }
            }
        });
    }

    public ObdConnectionManager getObdManager() {
        return obdManager;
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
    public void startConnection() {
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
            Toast.makeText(this, "Paired OBD scanner not found", Toast.LENGTH_LONG).show();
            return;
        }

        obdManager.connect(targetDevice);
    }

    public void disconnect() {
        obdManager.disconnect();
    }

    private void updateGlobalStatus(int resId, int color) {
        if (txtGlobalStatus != null) {
            txtGlobalStatus.setText(resId);
            txtGlobalStatus.setTextColor(color);
        }
    }

    @Override
    public void onConnecting() {
        runOnUiThread(() -> {
            updateGlobalStatus(R.string.status_connecting, getColor(R.color.text_gray));
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof HomeFragment) {
                ((HomeFragment) currentFragment).showConnecting();
            }
        });
    }

    @Override
    public void onConnectionStateChanged(boolean isConnected) {
        runOnUiThread(() -> {
            if (isConnected) {
                updateGlobalStatus(R.string.status_connected, getColor(R.color.accent_green));
            } else {
                updateGlobalStatus(R.string.status_disconnected, getColor(R.color.accent_red));
            }
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof HomeFragment) {
                ((HomeFragment) currentFragment).updateStatus();
            }
        });
    }

    @Override
    public void onTelemetryDataReceived(MotorcycleTelemetry data) {
        runOnUiThread(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof TelemetryUpdateListener) {
                ((TelemetryUpdateListener) currentFragment).onTelemetryData(data);
            }
        });
    }

    @Override
    public void onError(String message) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
    }

    public void openDtcDialog() {
        openDtcDialog(null, null);
    }

    public void openDtcDialog(Runnable onStart, Runnable onFinish) {
        if (onStart != null) runOnUiThread(onStart);
        obdManager.readDtc(new ObdManagerCallbacks.DtcReadListener() {
            @Override
            public void onDtcRead(List<String> codes) {
                runOnUiThread(() -> {
                    if (onFinish != null) onFinish.run();
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
                            .setPositiveButton("Clear Codes", (dialog, which) ->
                                    obdManager.clearDtc(success -> runOnUiThread(() ->
                                            Toast.makeText(MainActivity.this, success ? "Codes Cleared" : "Clear Failed", Toast.LENGTH_SHORT).show()
                                    ))
                            )
                            .setNegativeButton("Close", null)
                            .show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    if (onFinish != null) onFinish.run();
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    public void showDtcDictionaryDialog() {
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
