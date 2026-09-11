package com.diy.obdwizard.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothConnection {
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");  // SIG Serial Port Profile (SPP/RFCOMM)

    private final BluetoothDevice device;
    private BluetoothSocket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    public BluetoothConnection(BluetoothDevice device) {
        this.device = device;
    }

    @SuppressLint("MissingPermission")
    public synchronized boolean connect() {
        try {
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
            socket.connect();
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            return true;
        } catch (IOException e) {
            disconnect();
            return false;
        }
    }

    public synchronized String sendCommand(String command) throws IOException {
        if (outputStream == null || inputStream == null || socket == null || !socket.isConnected()) {
            disconnect();
            throw new IOException("Bluetooth socket not connected");
        }

        try {
            outputStream.write((command + "\r").getBytes());
            outputStream.flush();

            StringBuilder response = new StringBuilder();
            byte[] buffer = new byte[256];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                String chunk = new String(buffer, 0, bytesRead);
                response.append(chunk);
                if (chunk.contains(">")) {
                    break;
                }
            }

            if (bytesRead == -1 && response.length() == 0) {
                disconnect();
                throw new IOException("Bluetooth stream closed by remote device");
            }

            return response.toString().replace(">", "").trim();
        } catch (IOException e) {
            disconnect();
            throw e;
        }
    }

    public synchronized void disconnect() {
        try {
            if (inputStream != null) inputStream.close();
        } catch (IOException ignored) {
        }
        try {
            if (outputStream != null) outputStream.close();
        } catch (IOException ignored) {
        }
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
        socket = null;
        inputStream = null;
        outputStream = null;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }
}
