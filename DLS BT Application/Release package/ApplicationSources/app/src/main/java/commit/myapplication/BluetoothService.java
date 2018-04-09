package commit.myapplication;

/*************************************
 * Porject name: BT_Service
 * Created by: roeez
 * Date of creation: 9/24/2017.
 *************************************/

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class BluetoothService extends Service
{

//region Variables:

    // BT-BLE verbs:

    ArrayAdapter<String> PairDeviceInfo;

    private static StringTokenizer mStreamSDevicetokens = null;
    private static boolean mSelectedBtDeviceFlag = false;
    private static ArrayList<String> mSelectedBtDeviceInfo;
    BluetoothAdapter mBluetoothAdapter = null;
    ArrayList<BluetoothDevice> arrayListPairedBluetoothDevices;
    ArrayList<String> arrayListpaired;
    ArrayAdapter<String> adapter;
    BluetoothConnector mBluetoothConnector;
    BluetoothLeConnector mBluetoothLeConnector;
    BluetoothTimeManager mBluetoothTimeManager;

    // Time class service verbs:
    ArrayList<Integer> mTimeVerbs;

    // Data, process info verbs:
    Intent mBroadcastIntent = null;
    MyServiceReceiver myServiceReceiver;
    private boolean stopThread;

    // Broadcast verbs:
    final static String ACTION_MSG_TO_SERVICE = "MSG_TO_SERVICE";
    private static boolean mTriggerFromActivity = false;
    private static boolean mTriggerFromBoot = false;

    // BT state machine verbs:
    private CommonLib.BT_STATE mBtState = CommonLib.BT_STATE.BT_START;
//endregion

//region onCreate():

    @Override
    public void onCreate()
    {
        super.onCreate();

        mBroadcastIntent = new Intent();

        // Init BT objects:
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Start BT / BLE class services:
        mBluetoothLeConnector = new BluetoothLeConnector(this, mBluetoothAdapter);

        // BT / BLE arguments:
        arrayListpaired = new ArrayList<String>();  // Store BT pair devices strings + MAC address.
        arrayListPairedBluetoothDevices = new ArrayList<BluetoothDevice>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayListpaired);
        mSelectedBtDeviceInfo = new ArrayList<String>();

        // Service to activity broadcast arguments:
        stopThread = false;

        // Broadcast activity to service:
        myServiceReceiver = new MyServiceReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_MSG_TO_SERVICE);
        registerReceiver(myServiceReceiver, intentFilter);

        // Init timer class:
        mBluetoothTimeManager = new BluetoothTimeManager();
        mTimeVerbs = new ArrayList<Integer>();
    }
//endregion

//region bluetooth FSM (onStartCommand() function):

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        LogData(CommonLib.BT_LOG_RUN,"START BLUETOOTH FSM", true, true, CommonLib.STATUS_STRIP_TYPE.SYSTEM_STRIP);

        BtFsmTrhead.start();
        return START_STICKY;
    }
//endregion

//region BT-API:

    private void getPairedDevices()
    {
        Set<BluetoothDevice> pairedDevice = mBluetoothAdapter.getBondedDevices();

        LogData(CommonLib.BT_LOG_RUN,"Try to find BT devices", true, true, CommonLib.STATUS_STRIP_TYPE.SYSTEM_STRIP);
        int numberOfPariredDevice = pairedDevice.size();
        if(numberOfPariredDevice>0)
        {
            LogData(CommonLib.BT_LOG_RUN,"Found " + numberOfPariredDevice + " bonded device", true, true, CommonLib.STATUS_STRIP_TYPE.SYSTEM_STRIP);
            for(BluetoothDevice device : pairedDevice)
            {
                LogData(CommonLib.BT_LOG_RUN, "Device name is = " + device.getName(),true, false, CommonLib.STATUS_STRIP_TYPE.DEVICE_STRIP);
                LogData(CommonLib.BT_LOG_RUN,"Device MAC address = " + device.getAddress(), true, false, CommonLib.STATUS_STRIP_TYPE.DEVICE_STRIP);
                arrayListpaired.add(device.getName()+"\n"+device.getAddress());
                arrayListPairedBluetoothDevices.add(device);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void OnOffBluetooth(CommonLib.BT_REQUEST bt_request)
    {
        try
        {
            if(bt_request == CommonLib.BT_REQUEST.BT_TURN_ON)
            {
                if (false == mBluetoothAdapter.isEnabled())
                {
                    mBluetoothAdapter.enable();
                    LogData(CommonLib.BT_LOG_INIT, "Bluetooth is Enabled", true, true, CommonLib.STATUS_STRIP_TYPE.SYSTEM_STRIP);
                }
                else
                {
                    LogData(CommonLib.BT_LOG_INIT, "Cant turn BT / BLE on, it is already ON", true, true, CommonLib.STATUS_STRIP_TYPE.SYSTEM_STRIP);
                }
            }
            else if (bt_request == CommonLib.BT_REQUEST.BT_TURN_OFF)
            {
                if (true == mBluetoothAdapter.isEnabled())
                {
                    mBluetoothAdapter.disable();
                    LogData(CommonLib.BT_LOG_INIT, "Bluetooth is disable", true, true, CommonLib.STATUS_STRIP_TYPE.SYSTEM_STRIP);
                }
                else
                {
                    LogData(CommonLib.BT_LOG_INIT, "Cant turn BT / BLE on, it is already ON", true, true, CommonLib.STATUS_STRIP_TYPE.SYSTEM_STRIP);
                }
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    private void OnOffBtDiscover(CommonLib.BT_REQUEST bt_request)
    {
        try
        {
            if(bt_request == CommonLib.BT_REQUEST.BT_DISCOVER_ON)
            {

                mBluetoothAdapter.startDiscovery();
            }
            else if(bt_request == CommonLib.BT_REQUEST.BT_DISCOVER_OFF)
            {
                LogData(CommonLib.BT_LOG_INIT, "BT stop discovering", true, true, CommonLib.STATUS_STRIP_TYPE.SYSTEM_STRIP);
                mBluetoothAdapter.cancelDiscovery();
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    boolean ConnectBtDevice() // Selected BT/BLE device is class member.
    {
        return true;
    }

    private boolean ConnectBleDevice()
    {
        // Turn on device Bluetooth adapter:
        OnOffBluetooth(CommonLib.BT_REQUEST.BT_TURN_ON);
        if(false == mBluetoothAdapter.isEnabled())
        {
            Log.e(CommonLib.BT_LOG_ERROR, "BT adapter is off");
            return false;
        }
        if(false == mBluetoothLeConnector.IsTargetSupportBLE(this))
        {
            return false;
        }
        mBluetoothLeConnector.GetBLEScanner(mBluetoothAdapter);
        while(true);


    }

    private boolean RemovePairDevice()
    {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0)
        {
            for (BluetoothDevice device : pairedDevices)
            {
                try
                {
                    Method m = device.getClass()
                            .getMethod("removeBond", (Class[]) null);
                    m.invoke(device, (Object[]) null);
                    LogData(CommonLib.BT_LOG_RUN, "Remove all devices succeeded", true, true, CommonLib.STATUS_STRIP_TYPE.DEVICE_STRIP);
                }
                catch (Exception e)
                {
                    LogData(CommonLib.BT_LOG_ERROR, "Remove all devices has benn failed", true, true, CommonLib.STATUS_STRIP_TYPE.DEVICE_STRIP);

                }
                return false;
            }
        }
        return true;
    }

//endregion:

//region onDestroy():

    @Override
    public void onDestroy()
    {
        Toast.makeText(this, "BT Service Stop", Toast.LENGTH_LONG).show();
        unregisterReceiver(myServiceReceiver);
    }
//endregion

//region IBinder():

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
//endregion

//region LOG APIs:

    public void LogData(String logType, String logMsg, boolean logConsole, boolean logToast, CommonLib.STATUS_STRIP_TYPE mInfraStripType)
    {
        if(true == logConsole) // Write message to console / ADB.
        {
            Log.d(logType, logMsg);
        }

        if(mInfraStripType == CommonLib.STATUS_STRIP_TYPE.SYSTEM_STRIP) // Write message to status strip.
        {
            SendBroadcastString(mInfraStripType, logMsg);
        }

        else if(mInfraStripType == CommonLib.STATUS_STRIP_TYPE.DEVICE_STRIP) // Write message to status strip.
        {
            SendBroadcastString(mInfraStripType, logMsg);
        }

        if(true == logToast) // Toast message to screen.
        {
            Toast.makeText(this, logMsg, Toast.LENGTH_SHORT).show();
        }
    }

//endregion

//region SYSTEM APIs:

private void Sleep(int mTimeToSleep)
{
    try
    {
        Thread.sleep(mTimeToSleep);
    }
    catch (InterruptedException ex)
    {
        LogData(CommonLib.BT_LOG_ERROR, ex.getMessage(), true, false, CommonLib.STATUS_STRIP_TYPE.SYSTEM_STRIP);
    }

}

//region Load configuration file

    public boolean ReadConfigurationFile()
    {
        String[] tempTimeRow;
        try
        {
            File myFile = new File(CommonLib.fileConfigPath);
            FileInputStream fIn = new FileInputStream(myFile);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
            String aDataRow = "", aDataRowTemp = "";
            while ((aDataRow = myReader.readLine()) != null)
            {
                aDataRowTemp = aDataRow.split(":")[1].trim();
                if(aDataRow.contains("Remote device name:"))
                {
                    Log.e(CommonLib.BT_LOG_RUN,"Pair device: " + aDataRow);
                }
                else
                {
                    mTimeVerbs.add(Integer.parseInt(aDataRowTemp));
                }
            }
            myReader.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        mBluetoothTimeManager.SetTimeConfiguration(mTimeVerbs);
        mBluetoothTimeManager.StartTimerSessionA();
        return true;
    }

//endregion
//endregion

//region Broadcast info:

    //region Broadcast from activity to service:

    public static class MyServiceReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if(intent.getAction().contains(CommonLib.ToServiceBroadcastActionAppTrigger))
            {
                mTriggerFromActivity = true;
            }
            else if(intent.getAction().contains(CommonLib.ToServiceBroadcastAction))
            {
                String mSelectedBtDeviceStream = intent.getStringExtra(CommonLib.BroadcastStringKey);
                mStreamSDevicetokens = new StringTokenizer(mSelectedBtDeviceStream, "\n");

                // Get String with name + Index:
                mSelectedBtDeviceInfo.add(mStreamSDevicetokens.nextToken());

                // Get MAC address:
                mSelectedBtDeviceInfo.add(mStreamSDevicetokens.nextToken());

                mStreamSDevicetokens = new StringTokenizer(mSelectedBtDeviceInfo.get((0)), ",");

                // Get device index:
                mSelectedBtDeviceInfo.add(mStreamSDevicetokens.nextToken());

                // Get device name (only):
                mSelectedBtDeviceInfo.set(0, mStreamSDevicetokens.nextToken());

                Log.d(CommonLib.BT_LOG_RUN, "Device name = " + mSelectedBtDeviceInfo.get(0) + " Device Mac "+ mSelectedBtDeviceInfo.get(1) + " Device Idx " + mSelectedBtDeviceInfo.get(2));
                // Update BT FSM that user have selected BT device.
                mSelectedBtDeviceFlag = true;
            }
        }


    }
    //endregion

    //region Broadcast from activity:

    private void SendBroadcastString(CommonLib.STATUS_STRIP_TYPE mInfraStripType, String mString)
    {
        if(mInfraStripType == CommonLib.STATUS_STRIP_TYPE.SYSTEM_STRIP)
        {
            mBroadcastIntent.setAction(CommonLib.BroadcastSystemStringAction);
            mBroadcastIntent.putExtra(CommonLib.BroadcastSystemStringKey, mString);
        }
        else if (mInfraStripType == CommonLib.STATUS_STRIP_TYPE.DEVICE_STRIP)
        {
            mBroadcastIntent.setAction(CommonLib.BroadcastDeviceStringAction);
            mBroadcastIntent.putExtra(CommonLib.BroadcastDeviceStringKey, mString);
        }
        sendBroadcast(mBroadcastIntent);
    }

    private void SendBroadcastArrayList(ArrayList<String> mList)
    // Broadcast string array list as serial string each after each.
    {
        for (String str:mList)
        {
            mBroadcastIntent.setAction(CommonLib.BroadcastArrayListAction);
            mBroadcastIntent.putExtra(CommonLib.BroadcastArrayListKey, str);
            sendBroadcast(mBroadcastIntent);
        }
    }
    //endregion
//endregion

//region Timer services:

//endregion

//region BT FSM thread

    Thread BtFsmTrhead = new Thread()
    {
        @Override
        public void run()
        {
            Looper.prepare();
            try
            {
                super.run();
                while(mBtState != CommonLib.BT_STATE.BT_OFF)
                {
                    switch (mBtState)
                    {
                        case BT_START:
                            mBtState = CommonLib.BT_STATE.LISTEN_TO_OUDSIDE_TRIGGER;
                            break;

                        case LISTEN_TO_OUDSIDE_TRIGGER:
                            while(true != mTriggerFromActivity);
                            mBtState = CommonLib.BT_STATE.BT_READ_CONFIGURATION;
                            break;

                        case BT_READ_CONFIGURATION:
                            ReadConfigurationFile();
                            if(mTriggerFromActivity == true)
                            {
                                mBtState = CommonLib.BT_STATE.BT_ON;
                            }
                            else if(mTriggerFromBoot == true)
                            {
                                mBtState = CommonLib.BT_STATE.BT_ON;
                            }
                            break;

                        case BT_ON:
                            OnOffBluetooth(CommonLib.BT_REQUEST.BT_TURN_ON);
                            mBtState = CommonLib.BT_STATE.BT_START_DISCOVER_NEAR_BY_DEVICE;
                            break;

                        case BT_START_DISCOVER_NEAR_BY_DEVICE:
                            OnOffBtDiscover(CommonLib.BT_REQUEST.BT_DISCOVER_ON);
                            mBtState = CommonLib.BT_STATE.BT_GET_PAIR_DEVICES;
                            break;

                        case BT_GET_PAIR_DEVICES:
                            getPairedDevices();
                            Log.d(CommonLib.BT_LOG_RUN,"STATE: BT_GET_PAIR_DEVICES");
                            mBtState = CommonLib.BT_STATE.BT_INFROM_ACTIVITY_INFRA;
                            break;

                        case BT_INFROM_ACTIVITY_INFRA:
                            if(mTriggerFromActivity == true)
                            {
                                Log.d(CommonLib.BT_LOG_RUN,"STATE: BT_INFROM_INFRA");
                                SendBroadcastArrayList(arrayListpaired);
                                mBtState = CommonLib.BT_STATE.WAIT_FOR_SELECT_DEVICE;
                            }
                            break;

                        case WAIT_FOR_SELECT_DEVICE:
                            LogData(CommonLib.BT_LOG_RUN,"Please select device to connect", true, false, CommonLib.STATUS_STRIP_TYPE.SYSTEM_STRIP);
                            while(false == mSelectedBtDeviceFlag); // Loop until user select device.
                            mBtState = CommonLib.BT_STATE.BT_REMOVE_ALL_PAIR_DEVICE;
                            break;

                        case BT_REMOVE_ALL_PAIR_DEVICE:
                            RemovePairDevice();
                            mBtState = CommonLib.BT_STATE.BT_START_DISCOVER_NEAR_BY_DEVICE;
                            break;

                        case BT_START_BLE_CONNECT:
                            Log.d(CommonLib.BT_LOG_RUN, "Start scanning BLE device");
                            ConnectBleDevice();
                            Sleep(11000);
                            mBtState = CommonLib.BT_STATE.BT_START_LEGACY_CONNECT;
                            break;

                        case BT_START_LEGACY_CONNECT:
                            if(false == ConnectBtDevice())  // Fail to connect to BT device
                            {
                                mSelectedBtDeviceFlag = false;
                                mBtState = CommonLib.BT_STATE.WAIT_FOR_SELECT_DEVICE;
                            }
                            else
                            {
                                mBtState = CommonLib.BT_STATE.BT_OFF;
                            }
                            break;

                        case BT_OFF:
                            LogData(CommonLib.BT_LOG_RUN,"Service Off", true, true, CommonLib.STATUS_STRIP_TYPE.SYSTEM_STRIP);
                            OnOffBtDiscover(CommonLib.BT_REQUEST.BT_DISCOVER_OFF);
                            OnOffBluetooth(CommonLib.BT_REQUEST.BT_TURN_OFF);
                            break;
                    }
                }
            }
            catch (Exception e)
            {
                Log.d(CommonLib.BT_LOG_ERROR, e.getMessage());
            }
        }
    };
//endregion



}





