package com.projeto.util;

import com.emotiv.insight.IEdk;
import com.emotiv.insight.IEdkErrorCode;
import com.emotiv.insight.IEmoStateDLL;
import com.emotiv.insight.MentalCommandDetection;

public class Emotiv {

    /* Config */
    public static final int TYPE_USER_ADD = IEdk.IEE_Event_t.IEE_UserAdded.ToInt();
    public static final int TYPE_USER_REMOVE = IEdk.IEE_Event_t.IEE_UserRemoved.ToInt();
    public static final int TYPE_EMOSTATE_UPDATE = IEdk.IEE_Event_t.IEE_EmoStateUpdated.ToInt();
    public static final int TYPE_MENTALCOMMAND = IEdk.IEE_Event_t.IEE_MentalCommandEvent.ToInt();

    public static final int TYPE_TRAIN_STARTED = MentalCommandDetection.IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingStarted.getType();
    public static final int TYPE_TRAIN_SUCCEED = MentalCommandDetection.IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingSucceeded.getType();
    public static final int TYPE_TRAIN_FAILED = MentalCommandDetection.IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingFailed.getType();
    public static final int TYPE_TRAIN_COMPLETED = MentalCommandDetection.IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingCompleted.getType();
    public static final int TYPE_TRAIN_ERASED = MentalCommandDetection.IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingDataErased.getType();
    public static final int TYPE_TRAIN_REJECTED = MentalCommandDetection.IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingRejected.getType();
    public static final int TYPE_TRAIN_RESET = MentalCommandDetection.IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingReset.getType();

    public static final int COMMAND_RESET = MentalCommandDetection.IEE_MentalCommandTrainingControl_t.MC_RESET.getType();
    public static final int COMMAND_REJECT = MentalCommandDetection.IEE_MentalCommandTrainingControl_t.MC_REJECT.getType();
    public static final int COMMAND_ACCEPT = MentalCommandDetection.IEE_MentalCommandTrainingControl_t.MC_ACCEPT.getType();
    public static final int COMMAND_ERASE = MentalCommandDetection.IEE_MentalCommandTrainingControl_t.MC_ERASE.getType();
    public static final int COMMAND_START = MentalCommandDetection.IEE_MentalCommandTrainingControl_t.MC_START.getType();

    public static final int OK = IEdkErrorCode.EDK_OK.ToInt();

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