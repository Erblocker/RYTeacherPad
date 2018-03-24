package com.pnf.bt.lib;

public class PNFDefine {
    public static final int BATTERY_INFO = 1;
    public static final int CALIBRATION_TURN_LEFT = 1;
    public static final int CALIBRATION_TURN_RIGHT = 2;
    public static final int CHANGE_DEVECE_POSITION = 4;
    public static final int CHANGE_DEVECE_POSITION_FIRST = 5;
    public static final int DIRECTION_BOTTOM = 4;
    public static final int DIRECTION_LEFT = 2;
    public static final int DIRECTION_RIGHT = 3;
    public static final int DIRECTION_TOP = 1;
    public static final int DUPLICATE_PAGE = 3;
    public static final int GESTURE_CIRCLE_CLOCKWISE = 22;
    public static final int GESTURE_CIRCLE_COUNTERCLOCKWISE = 23;
    public static final int GESTURE_CLICK = 25;
    public static final int GESTURE_DOUBLECLICK = 26;
    public static final int GESTURE_LEFT_RIGHT = 21;
    public static final int GESTURE_RIGHT_LEFT = 20;
    public static final int GESTURE_ZIGZAG = 24;
    public static final int MARKERPEN_BIG_ERASER = 92;
    public static final int MARKERPEN_BLACK_MARKER = 88;
    public static final int MARKERPEN_BLUE_MARKER = 84;
    public static final int MARKERPEN_ERASER_CAP = 89;
    public static final int MARKERPEN_GREEN_MARKER = 82;
    public static final int MARKERPEN_LOW_BATTERY = 90;
    public static final int MARKERPEN_PURPLE_MARKER = 86;
    public static final int MARKERPEN_RED_MARKER = 81;
    public static final int MARKERPEN_UP = 15;
    public static final int MARKERPEN_YELLOW_MARKER = 83;
    public static final int NEW_PAGE = 2;
    public static final int PEN_DI_ACC_DATA = 3;
    public static final int PEN_DI_DATA = 1;
    public static final int PEN_DI_DELETE = 4;
    public static final int PEN_DI_TEMPLETE = 2;
    public static final int PEN_DOWN = 1;
    public static final int PEN_E_FAIL_LISTENING = 32;
    public static final int PEN_E_INVALID_PROTOCOL = 31;
    public static final int PEN_E_NOT_CONNECTED = 30;
    public static final int PEN_HOVER = 4;
    public static final int PEN_HOVER_DOWN = 5;
    public static final int PEN_HOVER_MOVE = 6;
    public static final int PEN_MOVE = 2;
    public static final int PEN_PAGE_ADD = 12;
    public static final int PEN_PAGE_DUPLICATE = 13;
    public static final int PEN_PAGE_INSERT = 11;
    public static final int PEN_PAGE_LEFT_GOTO = 14;
    public static final int PEN_PAGE_NEW = 10;
    public static final int PEN_PAGE_RIGHT_GOTO = 15;
    public static final int PEN_UP = 3;
    public static final int PNF_DI_FAIL = 8;
    public static final int PNF_DI_FILE_LIST_COMPLETE = 11;
    public static final int PNF_DI_OK = 7;
    public static final int PNF_DI_START = 5;
    public static final int PNF_DI_STOP = 6;
    public static final int PNF_DI_TEMP_EXIST = 9;
    public static final int PNF_DI_TEMP_FILE_COMPLETE = 10;
    public static final int PNF_ENV_DATA = 1;
    public static final int PNF_MSG_CONNECTED = 5;
    public static final int PNF_MSG_CONNECTING = 4;
    public static final int PNF_MSG_CONNECTING_FAIL = 3;
    public static final int PNF_MSG_FAIL_LISTENING = 2;
    public static final int PNF_MSG_FIRST_DATA_ERROR = 8;
    public static final int PNF_MSG_FIRST_DATA_RECV = 7;
    public static final int PNF_MSG_INVALID_PROTOCOL = 1;
    public static final int PNF_MSG_PEN_RMD_ERROR = 6;
    public static final int PNF_MSG_SESSION_CLOSED = 11;
    public static final int PNF_MSG_SESSION_CONNECT = 9;
    public static final int PNF_MSG_SESSION_OPEN = 10;
    public static final boolean isDebug = false;
    public static boolean isImportMsg = false;

    public static int smPenStateToPenState(int smState) {
        switch (smState) {
            case 81:
                return 81;
            case 82:
                return 82;
            case 83:
                return 83;
            case 84:
                return 84;
            case 86:
                return 86;
            case 88:
                return 88;
            case 89:
                return 89;
            case 90:
                return 90;
            case 92:
                return 92;
            default:
                return 0;
        }
    }
}
