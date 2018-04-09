package commit.myapplication;

/*************************************
 * Porject name: BT_Service
 * Created by: roeez
 * Date of creation: 9/24/2017.
 *************************************/

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class BootReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(CommonLib.BT_LOG_BOOT, "BOOT - Service start ");
        Intent mIntenet = new Intent(context, BluetoothService.class);
        context.startService(mIntenet);
    }
}