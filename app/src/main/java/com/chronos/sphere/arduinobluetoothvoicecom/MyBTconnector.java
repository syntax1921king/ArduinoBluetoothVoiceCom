package com.chronos.sphere.arduinobluetoothvoicecom;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Sphere on 3/4/2017.
 */

public class MyBTconnector {
    private UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private boolean btIsConnected = false;
    private BluetoothSocket tmpSct = null;



    void conBTexpress(String macAddr) throws IOException {
        BluetoothAdapter tmpAdptr = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice tmpDvc = tmpAdptr.getRemoteDevice(macAddr);
        tmpSct = tmpDvc.createRfcommSocketToServiceRecord(myUUID);

        try {
            if (!btIsConnected) {
                tmpAdptr.cancelDiscovery();
                tmpSct.connect();
                if (tmpSct.isConnected()) {
                    btIsConnected = true;
                }
            } else {
                btIsConnected = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    boolean getbtIsConnected() {
        return btIsConnected;
    }

    void closeConnection() {

        if (tmpSct.isConnected()) {
            try
            {
                tmpSct.close(); //close connection
                btIsConnected = false;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }


    void sendBytes(String s)
    {
        if (getbtIsConnected())
        {
            try
            {
                tmpSct.getOutputStream().write(s.getBytes());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
