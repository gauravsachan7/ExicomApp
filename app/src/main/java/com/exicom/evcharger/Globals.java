package com.exicom.evcharger;

import android.content.Context;
import android.content.SharedPreferences;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

public class Globals {
    public static Context context;
    public static WebAccessLib webAccessLibObj;
    public static Globals globalsObj;
    public static String userloginSP = "login";
    public static String SP_TAG_USERTOKEN="USER_TOKEN";
    public static String SP_TAG_USER_ID="USER_ID";
    public static String SP_TAG_EMAIL = "EMAIL_ID";
    public static String SP_TAG_NAME = "USER_NAME";
    public static String SP_TAG_PASSWORD = "USER_PASSWORD";

    public static final String URL = "http://150.129.250.119:3005/";
    public static final String SIGNUP_TAG = "api/v1/register";
    public static final String LOGIN_TAG = "users/apilogin";
    public static final String ADD_DEVICE_TAG = "api/v1/createdevice";
    public static final String GET_DEVICES_TAG = "api/v1/devices/";
    public static final String CREATE_ACCESS_REQUEST_TAG = "api/v1/requestAccess";
    public static final String GUEST_ACCESS_LIST = "api/v1/guestAccessList";
    public static final String GET_SOFTWARE = "api/v1/getSoftwareVersion";
    public static final String GET_SOFTWARE_ONE = "api/v1/getSoftwareVersionOne";
    public static final String GRANT_ACCESS = "api/v1/grantGuestAccess";
    public static final String DENY_ACCESS = "api/v1/denyGuestAccess";
    public static final String DELETE_DEVICE = "api/v1/deleteDevice/";
    public static final String REQUEST_LIST = "api/v1/requestsList";
    public static final String CREATE_COMPLAINT = "api/v1/createComplaint";
    public static final String UPDATE_DEVICE_NICKNAME = "api/v1/updateNickname";
    public static final String REMOVE_DEVICE_USER = "api/v1/removeUser";
    public static final String DEFAULT_PASS = "12D687";

    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID RX_SERVICE_UUID = UUID.fromString("49535343-FE7D-4AE5-8FA9-9FAFD205E455");     //microchip traperent profile
    public static final UUID TX_CHAR_UUID  = UUID.fromString("49535343-1E4D-4BD9-BA61-23C647249616");       //microchip traperent profile
    public static final UUID RX_CHAR_UUID = UUID.fromString("49535343-8841-43F4-A8D4-ECBE34729BB3");        //microchip traperent profile

    public static final String SIGNAL_AC_VOLTAGE = "0A";
    public static final String SIGNAL_CURRENT = "14";
    public static final String SIGNAL_SESSION_TIME = "59";
    public static final String SIGNAL_SESSION_UNIT = "35";
    public static final String SIGNAL_KWH = "65";
    public static final String SIGNAL_TOTAL_TIME = "6A";
    public static final String SIGNAL_NE_VOLTAGE = "43";
    public static final String SIGNAL_EARTH_LEAKAGE_CURRENT = "16";
    public static final String SIGNAL_CHARGER_STATUS = "67";
    public static final String SIGNAL_CO2_STATUS = "38";
    public static final String SIGNAL_GET_PASSWORD = "32";
    public static final String SIGNAL_ALARM = "39";
    public static final String SIGNAL_ALARM_LOG_COUNT = "6F";
    public static final String SIGNAL_SESSION_LOG_COUNT = "69";

    public static final String SIGNAL_LOW_VOLT_CUTOFF = "11";
    public static final String SIGNAL_LOW_VOLT_CUTIN_HYS = "55";
    public static final String SIGNAL_HIGH_VOLT_CUTOFF = "10";
    public static final String SIGNAL_HIGH_VOLT_CUTIN_HYS = "54";
    public static final String SIGNAL_RATED_CURRENT = "4F";
    public static final String SIGNAL_MAX_OUT_CURR_PERCENT = "17";
    public static final String SIGNAL_MIN_OUT_CURRENT = "18";

    public static final String SIGNAL_CARD_SERIAL_NUMBER = "4A";
    public static final String SIGNAL_CARD_PART_NUMBER = "4C";
    public static final String SIGNAL_SYSTEM_SERIAL_NUMBER = "4B";
    public static final String SIGNAL_SYSTEM_PART_NUMBER = "4D";
    public static final String SIGNAL_COMPLETE_SYSTEM_VERSION = "52";
    public static final String SIGNAL_BOOT_MODE = "41";

    public static final String SIGNAL_WIFI_SSID = "63";
    public static final String SIGNAL_WIFI_PASSWORD = "61";

    public static final String SIGNAL_SERVER_IP = "64";
    public static final String SIGNAL_SERVER_PORT = "5E";
    public static final String SIGNAL_SERVER_PATH = "60";
    public static final String SIGNAL_CHARGER_ID = "62";
    public static final String SIGNAL_CHARGE_KWH = "4E";
    public static final String SIGNAL_CHARGE_TIME = "58";
    public static final String SIGNAL_APPOINTMENT_CHARGE_TIME = "6C";
    public static final String SIGNAL_APPOINTMENT_CHARGE_DATE = "6D";
    public static final String SIGNAL_ALARM_SETTING = "50";
    public static final String SIGNAL_NE_CUTOFF = "46";
    public static final String SIGNAL_RFID_AUTH = "34";
    public static final String SIGNAL_FACTORY_RESET = "53";
    public static final String SIGNAL_RTC_TIME = "3F";
    public static final String SIGNAL_RTC_DATE = "40";
    public static final String SIGNAL_PUBLIC_PRIVATE = "5D";

    public static final String SET_PASSWORD_PREFIX = "10AC320100";


    public static final String START_CHARGING = "10AC3C0101";
    public static final String STOP_CHARGING = "10AC3C0110";
    public static final String GET_AC_VOLTAGE = "10AC0A0000000000";
    public static final String GET_CURRENT = "10AC140000000000";
    public static final String GET_SESSION_TIME = "10AC590000000000";
    public static final String GET_SESSION_UNIT = "10AC350000000000";
    public static final String GET_TOTAL_TIME = "10AC6A0000000000";
    public static final String GET_NE_VOLTAGE = "10AC430000000000";
    public static final String GET_EARTH_LEAKAGE_CURRENT = "10AC160000000000";
    public static final String GET_CHARGING_STATUS = "10AC670000000000";
    public static final String GET_KWH = "10AC650000000000";
    public static final String GET_CO2_SAVED = "10AC380000000000";
    public static final String GET_PASSWORD_COMMAND = "10AC320000000000";
    public static final String GET_ALARMS = "10AC390000000000";
    public static final String GET_ALARM_LOG_COUNT = "10AC6F0000000000";
    public static final String GET_ALARM_LOGS_PREFIX = "10AC7000";
    public static final String GET_SESSION_LOGS_COUNT = "10AC690000000000";
    public static final String GET_SESSION_LOGS_PREFIX = "10AC6800";

    public static final String GET_LOW_VOLTAGE_CUTOFF = "10AC110000000000";
    public static final String SET_LOW_VOLTAGE_CUTOFF = "10AC1101";
    public static final String GET_LOW_VOLTAGE_CUTIN_HYS = "10AC550000000000";
    public static final String GET_HIGH_VOLTAGE_CUTOFF = "10AC100000000000";
    public static final String SET_HIGH_VOLTAGE_CUTOFF = "10AC1001";
    public static final String GET_HIGH_VOLTAGE_CUTIN_HYS = "10AC540000000000";
    public static final String GET_RATED_CURRENT = "10AC4F0000000000";
    public static final String SET_RATED_CURRENT = "10AC4F01";
    public static final String GET_MAX_OUTPUT_CURRENT_PERCENT = "10AC170000000000";
    public static final String SET_MAX_OUTPUT_CURRENT = "10AC1701";
    public static final String GET_MIN_OUTPUT_CURRENT = "10AC180000000000";
    public static final String SET_MIN_OUTPUT_CURRENT = "10AC1801";

    public static final String GET_CARD_SERIAL_NUMBER_01 = "10AC4A0000000000";
    public static final String GET_CARD_SERIAL_NUMBER_02 = "10AC4A1000000000";
    public static final String GET_CARD_SERIAL_NUMBER_03 = "10AC4A2000000000";
    public static final String GET_CARD_SERIAL_NUMBER_04 = "10AC4A3000000000";
    public static final String GET_CARD_PART_NUMBER_01 = "10AC4C0000000000";
    public static final String GET_CARD_PART_NUMBER_02 = "10AC4C1000000000";
    public static final String GET_SYSTEM_SERIAL_NUMBER_01 = "10AC4B0000000000";
    public static final String GET_SYSTEM_SERIAL_NUMBER_02 = "10AC4B1000000000";
    public static final String GET_SYSTEM_SERIAL_NUMBER_03 = "10AC4B2000000000";
    public static final String GET_SYSTEM_SERIAL_NUMBER_04 = "10AC4B3000000000";
    public static final String GET_SYSTEM_PART_NUMBER_01 = "10AC4D0000000000";
    public static final String GET_SYSTEM_PART_NUMBER_02 = "10AC4D1000000000";
    public static final String GET_COMPLETE_SYSTEM_VERSION = "10AC520000000000";

    public static final String GET_WIFI_SSID = "10AC630000000000";
    public static final String GET_WIFI_PASSWORD = "10AC610000000000";
    public static final String SET_WIFI_SSID_PREFIX = "10AC630";
    public static final String SET_WIFI_PASS_PREFIX = "10AC610";
    public static final String SET_CHARGE_TIME_PREFIX = "10AC58010000";
    public static final String SET_CHARGE_KWH_PREFIX = "10AC4E01";
    public static final String SET_APPOINTMENT_CHARGE_TIME_PREFIX = "10AC6C010000";
    public static final String SET_APPOINTMENT_CHARGE_DATE_PREFIX = "10AC6D0100";
    public static final String ENABLE_APPOINTMENT = "10AC6E0100000001";
    public static final String DISABLE_APPOINTMENT = "10AC6E0100000000";

    public static final String GET_SERVER_IP = "10AC640000000000";
    public static final String GET_SERVER_PORT = "10AC5E0000000000";
    public static final String GET_SERVER_PATH = "10AC600000000000";
    public static final String SET_SERVER_IP_PREFIX = "10AC64";
    public static final String SET_SERVER_PORT_PREFIX = "10AC5E";
    public static final String SET_SERVER_PATH_PREFIX = "10AC60";
    public static final String GET_CHARGER_ID = "10AC620000000000";
    public static final String SET_CHARGER_ID_PREFIX = "10AC62";
    public static final String GET_CHARGE_KWH = "10AC4E0000000000";
    public static final String GET_CHARGE_TIME = "10AC580000000000";
    public static final String GET_APPOINTMENT_CHARGE_TIME = "10AC6C0000000000";
    public static final String GET_APPOINTMENT_CHARGE_DATE = "10AC6D0000000000";
    public static final String GET_PUBLIC_PRIVATE = "10AC5D0000000000";
    public static final String SET_PUBLIC_MODE_PREFIX = "10AC5D01000000";

    public static final String GET_ALARM_SETTINGS = "10AC500000000000";
    public static final String GET_NE_VOLTAGE_CUTOFF = "10AC460000000000";
    public static final String SET_ALARM_SETTINGS_PREFIX = "10AC5001";
    public static final String SET_NE_VOLT_CUTOFF_PREFIX = "10AC4601";

    public static final String GET_RFID_AUTH = "10AC340000000000";
    public static final String SET_RFID_AUTH_PREFIX = "10AC3401";
    public static final String FACTORY_RESET_CMD = "10AC530100000000";

    public static final String SET_RTC_TIME_PREFIX = "10AC3F0100";
    public static final String SET_RTC_DATE_PREFIX = "10AC400100";

    public static final String ENTER_CARD_TO_BOOT_MODE = "10AC410100000000";
    public static final String PING_COMMAND = "20";
    public static final String DOWNLOAD_COMMAND = "21";
    public static final String RUN_COMMAND = "22";
    public static final String GET_STATUS_COMMAND = "23";
    public static final String SEND_DATA_COMMAND = "24";
    public static final String RESET_COMMAND = "25";
    public static final String ACK_COMMAND = "00CC";
    public static final String NACK_COMMAND = "0033";
    public static final String START_ADDRESS = "00002800";

    public static final Integer MIN_LOW_VOLT_CUTOFF_VAL = 90;
    public static final Integer MAX_LOW_VOLT_CUTOFF_VAL = 210;
    public static final Integer MIN_HIGH_VOLT_CUTOFF = 240;
    public static final Integer MAX_HIGH_VOLT_CUTOFF = 300;
    public static final Integer MIN_RATED_CURRENT = 10;
    public static final Integer MAX_RATED_CURRENT = 63;
    public static final Integer MIN_OUTPUT_CURRENT_PERCENT = 100;
    public static final Integer MAX_OUTPUT_CURRENT_PERCENT = 150;
    public static final Integer MAX_MIN_OUTPUT_CURRENT = 0;
    public static final Integer MIN_MIN_OUTPUT_CURRENT = 10;

    public final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public ArrayList<HashMap<String, String>> alarmArray = new ArrayList<>();
    String[] alarmName = {
            "SPD Fault",
            "Emergency Detect",
            "Reserve SC",
            "N-E Volt High",
            "Exception",
            "LED Board Communication Fault",
            "Connectivity Board Fault",
            "RFID Communication Fault",
            "Energy Meter Communication Fault",
            "PWM Fault",
            "Earth Leakage",
            "Earth Disconnect",
            "Output Current High",
            "Mains High",
            "Mains Low",
            "Mains Fail",
    };

    public ArrayList<HashMap<String, String>> liveDataArry = new ArrayList<>();

    String[] liveData = {
            "AC Voltage",
            "AC Current",
            "CO2 Saved",
            "Session kWh",
            "Session Time",
            "Cummulative kWh",
            "Cummulative Time",
            "N-E Voltage",
            "Earth Leakage Current",
            "Connector Status"
    };

    public ArrayList<HashMap<String, String>> alarmLogArray = new ArrayList<>();
    public ArrayList<HashMap<String, String>> sessionLogArray = new ArrayList<>();

    public Globals()
    {
        webAccessLibObj = new WebAccessLib(context);
    }

    public static Globals getInstance(Context mycontext) {
        if (context == null) {
            context = mycontext;
        }
        if (globalsObj == null) {
            globalsObj = new Globals();
        }
        return globalsObj;
    }

    public static String getUserToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(userloginSP, context.MODE_PRIVATE);
        String userToken = prefs.getString(SP_TAG_USERTOKEN, "0");
        return userToken;
    }

    public static String getUserId(Context context){
        SharedPreferences prefs = context.getSharedPreferences(userloginSP, context.MODE_PRIVATE);
        String userId = prefs.getString(SP_TAG_USER_ID, "0");
        return userId;
    }

    public static String getUserEmail(Context context){
        SharedPreferences prefs = context.getSharedPreferences(userloginSP, context.MODE_PRIVATE);
        String userId = prefs.getString(SP_TAG_EMAIL, "");
        return userId;
    }

    public static String getUserName(Context context){
        SharedPreferences prefs = context.getSharedPreferences(userloginSP, context.MODE_PRIVATE);
        String userId = prefs.getString(SP_TAG_NAME, "0");
        return userId;
    }

    public static String getPassword(Context context){
        SharedPreferences prefs = context.getSharedPreferences(userloginSP, context.MODE_PRIVATE);
        String password = prefs.getString(SP_TAG_PASSWORD, "");
        return password;
    }

    public void clearAllPreferences(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(userloginSP, context.MODE_PRIVATE).edit();
        editor.putString(SP_TAG_USERTOKEN,"0");
        editor.putString(SP_TAG_USER_ID, "0");
        editor.putString(SP_TAG_NAME, "");
        editor.putString(SP_TAG_EMAIL, "");
        editor.putString(SP_TAG_PASSWORD, "");
        editor.apply();
    }

    public void setAlarmList(String bitArr){
        alarmArray.clear();

        for(int i = bitArr.length()-1; i >= 0; i--){
            HashMap<String, String> alarmItem = new HashMap<>();
            String alarmChar = String.valueOf(bitArr.charAt(i));
            if(alarmChar.equals("1")) {
                alarmItem.put("name", alarmName[i]);
                alarmItem.put("value", alarmChar);
                alarmArray.add(alarmItem);
            }
        }
    }

    public void setInitialLiveData(){
        liveDataArry.clear();
        for(int i = 0; i < liveData.length; i++){
            HashMap<String, String> liveHash = new HashMap<>();
            liveHash.put("label", liveData[i]);
            liveHash.put("value", "");
            liveDataArry.add(liveHash);
        }
    }

    public ArrayList getAlarmList(){
        return alarmArray;
    }

    public void setLiveDataArray(Integer index, String value){
        HashMap<String, String> liveHash = new HashMap<>();
        liveHash.put("label", liveData[index]);
        liveHash.put("value", value);
        liveDataArry.set(index, liveHash);
    }

    public String getLogAlarmItem(String bitArr){
        String alarm_name = "";
        for(int i = bitArr.length()-1; i >= 0; i--){
            HashMap<String, String> alarmItem = new HashMap<>();
            String alarmChar = String.valueOf(bitArr.charAt(i));
            if(alarmChar.equals("1")) {
                alarm_name = alarmName[i];
                break;
            }
        }
        return alarm_name;
    }

    public  ArrayList getliveDataArray(){
        return liveDataArry;
    }

    public void setAlarmLogArray(ArrayList<HashMap<String, String>> alarmLogList){
        alarmLogArray.clear();
        alarmLogArray.addAll(alarmLogList);
    }

    public ArrayList<HashMap<String, String>> getAlarmLogArray(){
        return alarmLogArray;
    }

    public void setSessionLogArray(ArrayList<HashMap<String, String>> sessionLogList){
        sessionLogArray.clear();
        sessionLogArray.addAll(sessionLogList);
    }

    public ArrayList<HashMap<String, String>> getSessionLogArray(){
        return sessionLogArray;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len/2];

        for(int i = 0; i < len; i+=2){
            data[i/2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String byteToHex(byte bytes){
        char[] hexChars = new char[2];
        int v = bytes & 0xFF;
        hexChars[0] = hexArray[v >>> 4];
        hexChars[1] = hexArray[v & 0x0F];
        return new String(hexChars);
    }

    public static String hexToAscii(String hex) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < hex.length(); i+=2) {
            String str = hex.substring(i, i+2);
            output.append((char)Integer.parseInt(str, 16));
        }
        return output.toString();
    }

    public static String asciiToHex(String ascii){
        char[] ch = ascii.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (char c : ch) {
            int i = (int) c;
            builder.append(Integer.toHexString(i).toUpperCase());
        }
        return builder.toString();
    }

    public static String hexToBinary(String Hex) {
        String bin =  new BigInteger(Hex, 16).toString(2);
        long inb = Long.parseLong(bin);
        bin = String.format(Locale.getDefault(),"%08d", inb);
        String finalBin = ("0000000000000000"+bin).substring(bin.length());
        return finalBin;
    }

    public static String binaryToHex(String binary){
        int i= Integer.parseInt(binary,2);
        return Integer.toHexString(i).toUpperCase();
    }

}
