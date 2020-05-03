package es.unizar.eina.pandora2FA.plataforma;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import es.unizar.eina.pandora2FA.Inicio;
import es.unizar.eina.pandora2FA.Principal;
import es.unizar.eina.pandora2FA.R;
import es.unizar.eina.pandora2FA.utiles.SharedPreferencesHelper;

public class ContactarTres extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactar_tres);
    }

    public void terminar(View view){
        boolean guest = SharedPreferencesHelper.getInstance(getApplicationContext()).getBoolean("guest",true);
        // Si no habíamos iniciado sesión, vamos a la pantalla de inicio
        if(guest){
            startActivity(new Intent(ContactarTres.this, Inicio.class));
            finishAffinity();
        }
        // En caso contrario, volvemos a la pantalla principal
        else{
            startActivity(new Intent(ContactarTres.this, Principal.class));
            finishAffinity();
        }
    }
}
