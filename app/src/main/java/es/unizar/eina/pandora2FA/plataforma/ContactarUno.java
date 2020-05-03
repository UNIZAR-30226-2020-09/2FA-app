package es.unizar.eina.pandora2FA.plataforma;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import es.unizar.eina.pandora2FA.R;
import es.unizar.eina.pandora2FA.utiles.SharedPreferencesHelper;

public class ContactarUno extends AppCompatActivity {

    TextView mensaje;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactar_uno);
        mensaje = findViewById(R.id.contactar1_entrada_comunicado);
    }

    public void goSiguiente(View view){
        if(!mensaje.getText().toString().equals("")){
            SharedPreferencesHelper.getInstance(getApplicationContext()).put("mensaje",mensaje.getText().toString());
            startActivity(new Intent(this,ContactarDos.class));
        }
    }
}
