package commit.myapplication;

/*************************************
 * Porject name: BT_Service
 * Created by: roeez
 * Date of creation: 9/24/2017.
 *************************************/

public class CommonLib
{
    // Logs:
    public static final String BT_LOG_RUN = "** BTS DATA **";
    public static final String BT_LOG_ERROR = "** BTS Error  **";
    public static final String BT_LOG_INIT = "** BTS Init **";
    public static final String BT_LOG_BOOT = "** BTS AFTER BOOT **";

    // Broadcast from service to actitvity:
    public static final String BroadcastStringKey = "BroadcastStringKey";

    public static final String BroadcastSystemStringAction = "broadcast.string.system.key";
    public static final String BroadcastDeviceStringAction = "broadcast.string.device.key";
    public static final String BroadcastArrayListKey = "Broadcast.ArrayList.Key";
    public static final String BroadcastSystemStringKey = "Broadcast.System.String.Key";
    public static final String BroadcastDeviceStringKey = "Broadcast.device.String.Key";
    public static final String BroadcastArrayListAction = "broadcast.List.key";

    // Broadcast from Activity to BT service
    public static final String ToServiceBroadcastAction = "ToServiceBroadcastAction";
    public static final String ToServiceBroadcastActionAppTrigger = "To.Service.Broadcast.Action.AppTrigger";
    public static final String ToServiceBroadcastActionBootTrigger = "To.Service.Broadcast.Action.BootTrigger";

    // HW Key codes:
    public static final int KEY_CODE_HOME = 188; // select
    public static final int KEY_CODE_MENU = 189; // Connect
    public static final int KEY_CODE_BACK = 190; // exit


    // Timer arguments:

    public static final int SecPeriod = 1000;
    public static final int DeviceConnectPeriod = 10*SecPeriod;

    // File configuration:
    public static final String fileConfigPath = "/data/local/tmp/bt_config_final.txt";
    public enum BT_REQUEST
    {
        BT_TURN_ON,
        BT_TURN_OFF,
        BT_DISCOVER_ON,
        BT_DISCOVER_OFF,
    }

    public enum BT_STATE
    {
        BT_START,
        LISTEN_TO_OUDSIDE_TRIGGER,
        BT_READ_CONFIGURATION,
        BT_ON,
        BT_REMOVE_ALL_PAIR_DEVICE,
        BT_START_DISCOVER_NEAR_BY_DEVICE,
        BT_GET_PAIR_DEVICES,
        BT_INFROM_ACTIVITY_INFRA,
        WAIT_FOR_SELECT_DEVICE,
        BT_START_LEGACY_CONNECT,
        BT_START_BLE_CONNECT,
        BT_OFF,
    }

    public enum STATUS_STRIP_TYPE
    {
        SYSTEM_STRIP,
        DEVICE_STRIP,
        NONE,
    }






}
