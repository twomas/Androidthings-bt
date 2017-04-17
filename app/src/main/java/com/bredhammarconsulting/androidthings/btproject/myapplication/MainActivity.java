package com.bredhammarconsulting.androidthings.btproject.myapplication;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bredhammar on 2017-04-07.
 */

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothLeScanner mBluetoothLeScanner = null;
    private ScanCallback mScanCallback = null;
    private static final int REQUEST_FINE_LOCATION=0;
    private ArrayList<BluetoothDevice> mDevices;
    private CountDownTimer mCountDownTimer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        mDevices = new ArrayList<BluetoothDevice>();

        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivity(enableBtIntent);

        loadPermissions(Manifest.permission.ACCESS_FINE_LOCATION,REQUEST_FINE_LOCATION);

        mScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                processResult(result);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                for (ScanResult result : results) {
                    processResult(result);
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.d(TAG, "onScanFailed " + errorCode);
            }

            private void processResult(ScanResult result) {
                //Log.i(TAG, result.toString());

                final BluetoothDevice device = result.getDevice();
                processScanResult(device, result.getRssi(), result.getScanRecord().getBytes());
            }
        };

        mCountDownTimer = new CountDownTimer(60000, 1) {
            @Override
            public void onTick(long millisUntilFinished) {
                // nothing
            }
            @Override
            public void onFinish() {
                Log.i(TAG, "onFinish");
                dumpCachedDevicesToLog();
                recreate();
            }
        };
        mCountDownTimer.start();
    }

    private void cacheDevice(BluetoothDevice device) {
        boolean found = false;
        for (BluetoothDevice dev : mDevices) {
            if (dev.getAddress().equals(device.getAddress())) {
                found = true;
                break;
            }
        }

        if (!found) {
            mDevices.add(device);
        }
    }

    private void dumpCachedDevicesToLog() {
        int i = 0;
        for (BluetoothDevice dev : mDevices) {
            String str = "cachedDevices " + mDevices.get(i).getAddress() + " " + mDevices.get(i).getName();
            writeToFile(this, "scan.txt", str + "\n");
            i++;
        }
        mDevices.clear();
    }

    private void processScanResult(final BluetoothDevice device, int rssi, byte[] scanRecord) {
        String str = "processScanResult " + device.getAddress() + " " + device.getName() + " rssi: " + Integer.valueOf(rssi);
        Log.v(TAG, str);
        cacheDevice(device);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        if (null != mBluetoothLeScanner) {
            mBluetoothLeScanner.stopScan(mScanCallback);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build();

        if (null != mBluetoothLeScanner) {
            mBluetoothLeScanner.startScan(null, settings, mScanCallback);
        } else {
            Log.e(TAG, "No bluetooth");
        }
    }

    private void loadPermissions(String perm,int requestCode) {
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, perm)) {
                ActivityCompat.requestPermissions(this, new String[]{perm},requestCode);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // granted
                }
                else{
                    // no granted
                }
                return;
            }

        }

    }

    public void writeToFile(Context context, String filename, String body) {
        try {
            String date = (DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()).toString());
            String str = date + " " + body;
            File root = new File(Environment.getExternalStorageDirectory(), "bcab-bt");
            if (!root.exists()) {
                root.mkdirs();
            }
            File file = new File(root, filename);
            FileWriter writer = new FileWriter(file,true);
            writer.append(str);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
