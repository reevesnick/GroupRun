package app.com.grouprun.Connecting;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Matt on 6/3/2015.
 */
public class ConnectThread extends Thread {

    private BluetoothSocket bTSocket;

    public boolean connect(BluetoothAdapter bTAdapter, BluetoothDevice bTDevice, UUID mUUID) {
        try {
            bTSocket = bTDevice.createRfcommSocketToServiceRecord(mUUID);
            bTSocket.connect();
        } catch(IOException e) {
            Log.d("CONNECTTHREAD", "Could not connect: " + e.toString());
            try {
                bTSocket.close();
            } catch(IOException close) {
                Log.d("CONNECTTHREAD", "Could not close connection:" + e.toString());
                return false;
            }
        }
        return true;
    }

    public boolean cancel() {
        try {
            bTSocket.close();
        } catch(IOException e) {
            Log.d("CONNECTTHREAD", "Could not close connection:" + e.toString());
            return false;
        }
        return true;
    }

}
