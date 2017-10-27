package com.projeto.util;

public class Emotiv {

    private static boolean connected = false;
    private static int userID = -1;

    public static boolean isConnected() {
        return connected;
    }

    public static void setConnected(boolean connected) {
        Emotiv.connected = connected;
    }

    public static int getUserID() {
        return userID;
    }

    public static void setUserID(int userID) {
        Emotiv.userID = userID;
    }

    public static void clearUserID() {
        Emotiv.userID = -1;
    }
}