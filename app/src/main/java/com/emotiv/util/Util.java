package com.emotiv.util;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.emotiv.mentalcommand.R;

public class Util {

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
