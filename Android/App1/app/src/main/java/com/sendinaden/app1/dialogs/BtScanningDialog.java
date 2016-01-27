package com.sendinaden.app1.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;

import com.sendinaden.app1.R;
import com.sendinaden.app1.adapters.LeDeviceListAdapter;

/**
 * Project Breathe
 * Created by ejalaa on 12/07/15.
 * Simple Bluetooth dialog that shows the list of bluetooth devices found during a scan
 */
public abstract class BtScanningDialog extends DialogFragment {

    private LeDeviceListAdapter devicesFound = null;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        devicesFound = mListener.getBtAdapter();
        devicesFound = getBtAdapter();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_available_bt_devices)
                .setAdapter(devicesFound, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.out.println(devicesFound.getDevice(which).getName());
//                        mListener.deviceChosen();
                        deviceChosen(devicesFound.getDevice(which));
                    }
                })
        ;
        return builder.create();
    }

    public abstract void deviceChosen(BluetoothDevice device);
    public abstract LeDeviceListAdapter getBtAdapter();

}
