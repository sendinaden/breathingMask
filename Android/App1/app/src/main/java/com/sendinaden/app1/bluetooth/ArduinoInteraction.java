package com.sendinaden.app1.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Project Breathe
 * Created by ejalaa on 15/07/15.
 */
public abstract class ArduinoInteraction {

    private final static String TAG = ArduinoInteraction.class.getSimpleName();
    private static final String ModelNumberStringUUID = "00002a24-0000-1000-8000-00805f9b34fb";
    private static final String SerialPortUUID = "0000dfb1-0000-1000-8000-00805f9b34fb";
    private static final String CommandUUID = "0000dfb2-0000-1000-8000-00805f9b34fb";
    private static BluetoothGattCharacteristic mSCharacteristic, mModelNumberCharacteristic, mSerialPortCharacteristic, mCommandCharacteristic;
    private Context mainContext;
    //	byte[] baudrateBuffer={0x32,0x00,(byte) (baudrate & 0xFF),(byte) ((baudrate>>8) & 0xFF),(byte) ((baudrate>>16) & 0xFF),0x00};;
    private Handler mHandler;
    private BluetoothDevice device;
    private int baudrate = 57600;    //set the default baud rate to 115200
    private String baudrateBuffer = "AT+CURRUART=" + baudrate + "\r\n";
    /*
    * *********************************************************************************************
	* All methods and variables related to bluetooth state and connection
	* *********************************************************************************************
	* */
    private BluetoothLeService mBluetoothLeService;
    /**
     * Code to manage Service lifecycle.
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            System.out.println("mServiceConnection onServiceConnected");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                ((Activity) mainContext).finish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    private connectionStateEnum mConnectionState = connectionStateEnum.isNull;
    private Runnable mConnectingOverTimeRunnable = new Runnable() {

        @Override
        public void run() {
            if (mConnectionState == connectionStateEnum.isConnecting)
                mConnectionState = connectionStateEnum.isToScan;
            onConnectionStateChange(mConnectionState);
            System.out.println("mConnectingOverTime");
            mBluetoothLeService.close();
        }
    };

    /*
	* *********************************************************************************************
	* All methods and variables related to LIFECYCLE
	* *********************************************************************************************
	* */
    private Runnable mDisconnectingOverTimeRunnable = new Runnable() {

        @Override
        public void run() {
            if (mConnectionState == connectionStateEnum.isDisconnecting)
                mConnectionState = connectionStateEnum.isToScan;
            onConnectionStateChange(mConnectionState);
            System.out.println("mDisconnectingOverTime");
            mBluetoothLeService.close();
//            mainContext.stopService(new Intent(mainContext, BluetoothLeService.class));
        }
    };
    /**
     * Handles various events fired by the Service.
     * ACTION_GATT_CONNECTED: connected to a GATT server.
     * ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
     * ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
     * ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
     * or notification operations.
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
//			System.out.println("mGattUpdateReceiver->onReceive->action="+action);
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mHandler.removeCallbacks(mConnectingOverTimeRunnable);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnectionState = connectionStateEnum.isToScan;
                onConnectionStateChange(mConnectionState);
                mHandler.removeCallbacks(mDisconnectingOverTimeRunnable);
                System.out.println("ACTION_GATT_DISCONNECTED");
                mBluetoothLeService.close();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
//                for (BluetoothGattService gattService : mBluetoothLeService.getSupportedGattServices()) {
//					System.out.println("ACTION_GATT_SERVICES_DISCOVERED  "+
//							gattService.getUuid().toString());
//                }
                getGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                if (mSCharacteristic == mModelNumberCharacteristic) {
                    if (intent.getStringExtra(BluetoothLeService.EXTRA_DATA).toUpperCase().startsWith("DF BLUNO")) {
                        mBluetoothLeService.setCharacteristicNotification(mSCharacteristic, false);
                        mSCharacteristic = mCommandCharacteristic;
                        String mPassword = "AT+PASSWOR=DFRobot\r\n";
                        mSCharacteristic.setValue(mPassword);
                        mBluetoothLeService.writeCharacteristic(mSCharacteristic);
                        mSCharacteristic.setValue(baudrateBuffer);
                        mBluetoothLeService.writeCharacteristic(mSCharacteristic);
                        // TODO put this in command method to rename
//                        String mCommand = "AT+NAME=" + "new name";
//                        mSCharacteristic.setValue(baudrateBuffer);
//                        mBluetoothLeService.writeCharacteristic(mSCharacteristic);
                        mSCharacteristic = mSerialPortCharacteristic;
                        mBluetoothLeService.setCharacteristicNotification(mSCharacteristic, true);
                        mConnectionState = connectionStateEnum.isConnected;
                        onConnectionStateChange(mConnectionState);

                    } else {
                        Toast.makeText(mainContext, "Please select DFRobot devices", Toast.LENGTH_SHORT).show();
                        mConnectionState = connectionStateEnum.isToScan;
                        onConnectionStateChange(mConnectionState);
                    }
                } else if (mSCharacteristic == mSerialPortCharacteristic) {
                    onSerialReceived(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                }


//				System.out.println("displayData "+intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };


    public ArduinoInteraction(Context mainContext, Handler mHandler, BluetoothDevice device) {
        this.mainContext = mainContext;
        this.mHandler = mHandler;
        this.device = device;
        registerAndBind();
        Intent intent = new Intent(mainContext, BluetoothLeService.class);
        mainContext.startService(intent);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public void connect() {
        // This allows the connection to happen and not be stop by a previous disconnection
        mHandler.removeCallbacks(mDisconnectingOverTimeRunnable);

        if (mBluetoothLeService.connect(device.getAddress())) {
            Log.d(TAG, "Connect request success");
            mConnectionState = connectionStateEnum.isConnecting;
            onConnectionStateChange(mConnectionState);
            mHandler.postDelayed(mConnectingOverTimeRunnable, 10000);
        } else {
            Log.d(TAG, "Connect request fail");
            mConnectionState = connectionStateEnum.isToScan;
            onConnectionStateChange(mConnectionState);
        }
    }

    public void disconnect() {
        System.out.println("disconnect in Arduino Interaction");
        mBluetoothLeService.disconnect();
        mHandler.postDelayed(mDisconnectingOverTimeRunnable, 10000);
        mConnectionState = connectionStateEnum.isDisconnecting;
        onConnectionStateChange(mConnectionState);
    }

    public void sendSerial(String theString) {
        if (mConnectionState == connectionStateEnum.isConnected) {
            mSCharacteristic.setValue(theString);
            mBluetoothLeService.writeCharacteristic(mSCharacteristic);
        }
    }

    public void sendCommand(String theCommand) {
        // TODO implement method
    }

    public void setBaudrate(int baud) {
        baudrate = baud;
        baudrateBuffer = "AT+CURRUART=" + baudrate + "\r\n"; // TODO change CURRUART to UART
    }

    /*
	* *********************************************************************************************
	* All methods and variables related to Characteristic discovery and attribution
	* *********************************************************************************************
	* */

    public void changeArduinoName(String newName) {
        sendCommand("AT+NAME=" + newName + "\r\n");
    }

    protected abstract void onSerialReceived(String stringExtra);

    private void registerAndBind() {
        Intent gattServiceIntent = new Intent(mainContext, BluetoothLeService.class);
        mainContext.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        mainContext.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    public void unregisterAndUnbind() {
        if (((Activity) mainContext).isFinishing()) {
            mainContext.stopService(new Intent(mainContext, BluetoothLeService.class));
        }
        mainContext.unbindService(mServiceConnection);
        mainContext.unregisterReceiver(mGattUpdateReceiver);
    }

    protected abstract void onConnectionStateChange(connectionStateEnum mConnectionState);

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    private void getGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid;
        mModelNumberCharacteristic = null;
        mSerialPortCharacteristic = null;
        mCommandCharacteristic = null;
        ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            uuid = gattService.getUuid().toString();
//			System.out.println("displayGattServices + uuid="+uuid);

            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> characteristics = new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                characteristics.add(gattCharacteristic);
                uuid = gattCharacteristic.getUuid().toString();
                if (uuid.equals(ModelNumberStringUUID)) {
                    mModelNumberCharacteristic = gattCharacteristic;
//					System.out.println("mModelNumberCharacteristic  " + mModelNumberCharacteristic.getUuid().toString());

                } else if (uuid.equals(SerialPortUUID)) {
                    mSerialPortCharacteristic = gattCharacteristic;
//					System.out.println("mSerialPortCharacteristic  " + mSerialPortCharacteristic.getUuid().toString());
//                    updateConnectionState(R.string.comm_establish);

                } else if (uuid.equals(CommandUUID)) {
                    mCommandCharacteristic = gattCharacteristic;
//					System.out.println("mSerialPortCharacteristic  " + mSerialPortCharacteristic.getUuid().toString());
//                    updateConnectionState(R.string.comm_establish);

                }
            }
            mGattCharacteristics.add(characteristics);
        }

        if (mModelNumberCharacteristic == null || mSerialPortCharacteristic == null || mCommandCharacteristic == null) {

            Toast.makeText(mainContext, "Please select DFRobot devices", Toast.LENGTH_SHORT).show();
            mConnectionState = connectionStateEnum.isToScan;
            onConnectionStateChange(mConnectionState);
        } else {
            mSCharacteristic = mModelNumberCharacteristic;
            mBluetoothLeService.setCharacteristicNotification(mSCharacteristic, true);
            mBluetoothLeService.readCharacteristic(mSCharacteristic);
        }

    }

    public enum connectionStateEnum {isNull, isScanning, isToScan, isConnecting, isConnected, isDisconnecting}

}
