package commit.myapplication;

/*************************************
 * Porject name: BT_Service
 * Created by: roeez
 * Date of creation: 9/24/2017.
 *************************************/

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

@TargetApi(23)
public class BT_Service extends AppCompatActivity
{

//region Veriables:


    private IntentFilter mIntentFilter;
    public TextView mTbxStatus;
    private TextView mTbxDevice;
    private ListView mLvBtList;
    private int deviceCounter = 0;
    private ArrayList<String> mBtList = null;
    ArrayAdapter<String> mBtArrayAdapter;

    //User permission:
    final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 0;

    // Broadcast to service:
    Intent mServiceRecieverIntent = null;
//endregion

//region OnCreate():

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt__service);

        mTbxStatus = (TextView)findViewById(R.id.tbxStatus);
        mTbxDevice = (TextView)findViewById(R.id.tbxDevice);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(CommonLib.BroadcastSystemStringAction);
        mIntentFilter.addAction(CommonLib.BroadcastDeviceStringAction);
        mIntentFilter.addAction(CommonLib.BroadcastArrayListAction);

        mBtList = new ArrayList<String>();
        mBtArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, mBtList );
        mLvBtList = (ListView) findViewById(R.id.lsvPariedDevice);
        mLvBtList.setAdapter(mBtArrayAdapter);

        Intent serviceIntent = new Intent(this, BluetoothService.class);
        startService(serviceIntent);

        // Broadcast start trigger to service:
        mServiceRecieverIntent =  new Intent();
        BroadcastToServiceThatActivityTrigger();
    }
//endregion

//region onResume() + onPause():

    @Override
    public void onResume()
    {
        super.onResume();
        registerReceiver(myReceiver, mIntentFilter);
    }

    @Override
    protected void onPause()
    {
        unregisterReceiver(myReceiver);
        super.onPause();
    }
//endregion

//region start / stop BT service:

    public void startService(View view)
    {

        Intent intent = new Intent(this,BluetoothService.class);
        startService(intent);
    }

    public void stopService(View view)
    {
        Intent intent = new Intent(this,BluetoothService.class);
        stopService(intent);
    }
//endregion

//region Broadcast to / from BT service:

    //region Broadcast from BT service:
    private BroadcastReceiver myReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent.getAction().equals(CommonLib.BroadcastSystemStringAction))
            {
                mTbxStatus.setText(intent.getStringExtra(CommonLib.BroadcastSystemStringKey));
            }
            else if (intent.getAction().equals(CommonLib.BroadcastDeviceStringAction))
            {
                mTbxDevice.setText(intent.getStringExtra(CommonLib.BroadcastDeviceStringKey));
            }
            else if(intent.getAction().equals(CommonLib.BroadcastArrayListAction))
            {
                mBtList.add(intent.getStringExtra(CommonLib.BroadcastArrayListKey));
                mBtArrayAdapter.notifyDataSetChanged();
            }

            Intent stopIntent = new Intent(BT_Service.this, BluetoothService.class);
            stopService(stopIntent);
        }
    };
    //endregion

    //region Broadcast to BT service:

    public void BroadcastToServiceThatActivityTrigger()
    {
        mServiceRecieverIntent.setAction(CommonLib.ToServiceBroadcastActionAppTrigger);
        sendBroadcast(mServiceRecieverIntent);
    }

    public void BroadcastStringToService(String mStrVal)
    {
        mServiceRecieverIntent.setAction(CommonLib.ToServiceBroadcastAction);
        mServiceRecieverIntent.putExtra(CommonLib.BroadcastStringKey, mStrVal);
        sendBroadcast(mServiceRecieverIntent);
    }
    //endregion
//endregion

//region Handling user Press keys:

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        int scanCode = event.getScanCode();
        if (scanCode == CommonLib.KEY_CODE_HOME)
        {
            if(mBtList.size() == 0)
            {
                mTbxDevice.setText("Device not detect");
            }
            else
            {
                mTbxDevice.setText(mBtList.get(deviceCounter++ % mBtList.size()));
            }
            return true;
        }
        if (scanCode == CommonLib.KEY_CODE_MENU)
        {
            if(mBtList.size() == 0)
            {
                mTbxDevice.setText("Device not detect");
            }
            else
            {
                // Broadcast service if target supported BLE:
                mTbxDevice.setText("Connect to device");
                BroadcastStringToService(deviceCounter + "," + mBtList.get((deviceCounter - 1) % mBtList.size()));
            }
            return true;
        }
        if (scanCode == CommonLib.KEY_CODE_BACK)
        {
            //ActiviesSwitcher();
            Intent intent= getPackageManager().getLaunchIntentForPackage("com.android.settings");
            startActivity(intent);
            return true;
        }
        return true;
    }
    //endregion

//region User permisson reqeust:

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(CommonLib.BT_LOG_RUN,"Need permissons");
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void fuckMarshMallow() {
        List<String> permissionsNeeded = new ArrayList<String>();

        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
            permissionsNeeded.add("Show Location");

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {

                // Need Rationale
                String message = "App need access to " + permissionsNeeded.get(0);

                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);

                showMessageOKCancel(message,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                            }
                        });
                return;
            }
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return;
        }

        Toast.makeText(this, "No new Permission Required- Launching App .You are Awesome!!", Toast.LENGTH_SHORT)
                .show();
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean addPermission(List<String> permissionsList, String permission) {

        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }
//endregion

//region Switch between activities:

    private void ActiviesSwitcher()
    {
        PackageManager manager = getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos= manager.queryIntentActivities(mainIntent, 0);
        // Below line is new code i added to your code
        Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(manager));

        for(ResolveInfo info : resolveInfos)
        {
            ApplicationInfo applicationInfo = info.activityInfo.applicationInfo;
            Log.d(CommonLib.BT_LOG_RUN,applicationInfo.toString());
            //get package name, icon and label from applicationInfo object
        }
    }
//endregion

}
