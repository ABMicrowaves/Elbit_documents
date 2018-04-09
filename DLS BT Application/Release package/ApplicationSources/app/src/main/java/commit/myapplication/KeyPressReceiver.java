package commit.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

/**
 * Created by roeez on 9/21/2017.
 */

public class KeyPressReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        private BroadcastReceiver mHomeKeyEventReceiver = new BroadcastReceiver() {
            String SYSTEM_REASON = "reason";
            String SYSTEM_HOME_KEY = "homekey";
            String SYSTEM_HOME_KEY_LONG = "recentapps";
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                    String reason = intent.getStringExtra(SYSTEM_REASON);

                }
            }
    }
}

