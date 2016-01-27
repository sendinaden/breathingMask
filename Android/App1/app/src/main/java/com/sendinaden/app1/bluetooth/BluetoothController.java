package com.sendinaden.app1.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;

import com.sendinaden.app1.adapters.LeDeviceListAdapter;
import com.sendinaden.app1.dialogs.BtScanningDialog;

/**
 * Created by ejalaa on 14/09/15.
 */
public class BluetoothController {

    private static final long SCAN_PERIOD = 10000;
    private Handler mHandler;
    private boolean isScanning;
    private BluetoothAdapter mBluetoothAdapter;
    private LeDeviceListAdapter deviceListAdapter;
    private Context mainContext;
    private BluetoothInteraction mListener;
    private Runnable myRunnable;
    private boolean deviceFound;
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            ((Activity) mainContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    System.out.print("Found: " + device.getName());
                    deviceListAdapter.addDevice(device);
                    deviceListAdapter.notifyDataSetChanged();

//                    if (device == null) {
//                        System.out.println("device null");
//                    } else if (device.getName() == null) {
//                        System.out.println("device has no name");
////                    } else if (device.getName().equals("BlunoV1.8")) {
////                    } else if (device.getName().equals("David-Mask<CR+")) {
////                    } else if (device.getName().equals("MaskV1.0")) {
////                    } else if (device.getName().equals("DemoV1")) {
//                    } else if (device.getName().equals("ALAA")) {
////                    } else if (device.getName().equals("VIVIANE")) {
//                        System.out.println(" <<<< ");
//                        mListener.onDeviceFound(device);
//                        deviceFound = true;
//                        stopScan();
//                    } else {
//                        System.out.println("");
//                    }
                }
            });
        }
    };
    private BtScanningDialog btScanningDialog;

    public BluetoothController(Context mainContext, Handler mHandler) {
        this.mHandler = mHandler;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mainContext = mainContext;
        mListener = (BluetoothInteraction) mainContext;
        mBluetoothAdapter.enable();
        deviceListAdapter = new LeDeviceListAdapter(mainContext);
        deviceFound = false;
        myRunnable = new Runnable() {
            @Override
            public void run() {
                isScanning = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                System.out.println("Scan stopped after SCAN_PERIOD");
                if (!deviceFound) {
                    mListener.noDeviceFound();
                }
            }
        };
    }

    void scanLeDevices(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(myRunnable, SCAN_PERIOD);

            // check other app
            if (deviceListAdapter != null) {
                deviceListAdapter.clear();
                deviceListAdapter.notifyDataSetChanged();
            }

            if (!isScanning) {
                isScanning = true;
                mBluetoothAdapter.startLeScan(mLeScanCallback);
                System.out.println("Scan really started");
            }
        } else {
            if (isScanning) {
                isScanning = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                System.out.println("scan really stopped, and callbacks for stopping after SCAN_PERIOD removed");
                mHandler.removeCallbacks(myRunnable);
            }
        }
    }

    public void startScan() {
        System.out.println("Starting scan requested");
        scanLeDevices(true);
        deviceFound = false;
        btScanningDialog = new BtScanningDialog() {
            @Override
            public void deviceChosen(BluetoothDevice device) {
                mListener.onDeviceFound(device);
            }

            @Override
            public LeDeviceListAdapter getBtAdapter() {
                return deviceListAdapter;
            }
        };
        btScanningDialog.show(((Activity) mainContext).getFragmentManager(), "devicesFound");
    }

    public void stopScan() {
        System.out.println("Stopping scan requested");
        scanLeDevices(false);
        btScanningDialog.dismiss();
        mHandler.removeCallbacks(myRunnable);
    }
    public interface BluetoothInteraction {
        void onDeviceFound(BluetoothDevice device);

        void noDeviceFound();
    }
}
