package com.emotiv.util;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.emotiv.controllers.R;

public class Util {

    public static final String TAG = "DebugEmotiv";

    /* Config */
    public static final int TYPE_USER_ADD = 16;
    public static final int TYPE_USER_REMOVE = 32;
    public static final int TYPE_EMOSTATE_UPDATE = 64;
    public static final int TYPE_METACOMMAND_EVENT = 256;

    /* Train */
    public static final int HANDLER_TRAIN_STARTED = 1;
    public static final int HANDLER_TRAIN_SUCCEED = 2;
    public static final int HANDLER_TRAIN_FAILED = 3;
    public static final int HANDLER_TRAIN_COMPLETED = 4;
    public static final int HANDLER_TRAIN_ERASED = 5;
    public static final int HANDLER_TRAIN_REJECTED = 6;
    public static final int HANDLER_ACTION_CURRENT = 7;
    public static final int HANDLER_USER_ADD = 8;
    public static final int HANDLER_USER_REMOVE = 9;
    public static final int HANDLER_TRAINED_RESET = 10;

    /**
     * Toolbar com o titulo da aplicacao
     * @param app activity
     * @param isButtonHome se ha o botao de voltar para a anterior
     * @return retorna a Toolbar criada
     */
    public static Toolbar initToolbar(AppCompatActivity app, boolean isButtonHome){

        Toolbar toolbar = (Toolbar) app.findViewById(R.id.barraMain);
        toolbar.setTitle(R.string.app_name);
        app.setSupportActionBar(toolbar);

        if(isButtonHome)
            app.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        return toolbar;
    }

    /**
     * Toolbar com o titulo da aplicacao
     * @param app activity
     * @param isButtonHome se ha o botao de voltar para a anterior
     * @return retorna a Toolbar criada
     */
    public static Toolbar initToolbar(AppCompatActivity app, boolean isButtonHome, String titulo){

        Toolbar toolbar = (Toolbar) app.findViewById(R.id.barraMain);
        toolbar.setTitle(titulo);
        app.setSupportActionBar(toolbar);

        if(isButtonHome)
            app.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        return toolbar;
    }

    public static void mudaTela(Context contexto, Class classe){
        Intent nova = new Intent(contexto, classe);
        contexto.startActivity(nova);
    }
}
