package commit.myapplication;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/*************************************
 * Porject name: BT_Service
 * Created by: roeez
 * Date of creation: 9/24/2017.
 * Description: This class implement legacy BT services.
 *************************************/

public final class BluetoothTimeManager
{
    private int mConnectionSessionATime = 0;
    private int mConnectionSessionAAttempt = 0;
    private int mConnectionSessionBTime = 0;
    private int mConnectionSessionBAttempt = 0;
    private int mKeepAliveTime = 0;
    private int mKeepAliveAttempt = 0;
    private static Timer timerSessionA = new Timer();
    private boolean timerAExpand = false;

    public void SetTimeConfiguration(ArrayList<Integer> iTimeVerbsList)
    {
        mConnectionSessionATime = iTimeVerbsList.get(0);
        mConnectionSessionAAttempt = iTimeVerbsList.get(1);
        mConnectionSessionBTime = iTimeVerbsList.get(2);
        mConnectionSessionBAttempt = iTimeVerbsList.get(3);
        mKeepAliveTime = iTimeVerbsList.get(4);
        mKeepAliveAttempt = iTimeVerbsList.get(5);
    }

//region Timer connection session A:

    public void StartTimerSessionA()
    {
        timerSessionA.scheduleAtFixedRate(new mTimerTaskA(), 0, mConnectionSessionATime*CommonLib.SecPeriod);
    }

    public void StopTimerThread()
    {
        timerSessionA.cancel();
    }

    private class mTimerTaskA extends TimerTask
    {
        public void run()
        {
            timerAExpand = true;
            StopTimerThread();
        }
    }

//endregion

    private void InformServiceTimeCome()
    {
        // Broadcasting service about timer tick:

    }
}
