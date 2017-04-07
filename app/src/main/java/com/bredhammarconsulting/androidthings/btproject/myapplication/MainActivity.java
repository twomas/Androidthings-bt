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
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.List;

/**
 * Created by Bredhammar on 2017-04-07.
 */

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanCallback mScanCallback;
    private static final int REQUEST_FINE_LOCATION=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        loadPermissions(Manifest.permission.ACCESS_FINE_LOCATION,REQUEST_FINE_LOCATION);

        mScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                //Log.d(TAG, "onScanResult");
                processResult(result);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                //Log.d(TAG, "onBatchScanResults: "+results.size()+" results");
                for (ScanResult result : results) {
                    processResult(result);
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                //Log.w(TAG, "LE Scan Failed: "+errorCode);
            }

            private void processResult(ScanResult result) {
                //Log.i(TAG, "New LE Device: " + result.getDevice().getName() + " @ " + result.getRssi());

                final BluetoothDevice device = result.getDevice();
                processScanResult(device, result.getRssi(), result.getScanRecord().getBytes());
            }
        };
    }

    private void processScanResult(final BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.v(TAG, "processScanResult " + device.getAddress() + " " + device.getName() + " rssi: " + Integer.valueOf(rssi));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build();

        mBluetoothLeScanner.startScan(null, settings, mScanCallback);
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
}
