package es.unizar.eina.pandora2FA.plataforma;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import es.unizar.eina.pandora2FA.R;
import es.unizar.eina.pandora2FA.utiles.PrintOnThread;
import es.unizar.eina.pandora2FA.utiles.SharedPreferencesHelper;
import okhttp3.*;

public class ContactarDos extends AppCompatActivity {

    final String url = "https://pandorapp.herokuapp.com/api/mensaje";
    private final OkHttpClient httpClient = new OkHttpClient();

    TextView remitente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactar_dos);

        remitente = findViewById(R.id.contactar2_entrada_comunicado);

        // Si viene de la pantalla principal (ha iniciado sesión), rellenamos el campo con su email
        // puesto que ya lo conocemos
        boolean guest = SharedPreferencesHelper.getInstance(getApplicationContext()).getBoolean("guest");
        if(!guest){
            remitente.setText(SharedPreferencesHelper.getInstance(getApplicationContext()).getString("email"));
        }
    }

    public void contactar(View view){
        if(!remitente.getText().toString().equals("")){
            String remitente_insertado = remitente.getText().toString().trim();
            String mensaje_insertado = SharedPreferencesHelper.getInstance(getApplicationContext()).getString("mensaje");
            doPost(remitente_insertado, mensaje_insertado);
        }
    }

    private void doPost(final String remitente, final String mensaje) {
        // Formamos un JSON con los parámetros
        JSONObject json = new JSONObject();
        try{
            json.accumulate("mail",remitente);
            json.accumulate("body",mensaje);
        }
        catch (Exception e){
            Log.d("EXCEPCION", e.getMessage());
        }

        // Formamos el cuerpo de la petición con el JSON creado
        RequestBody formBody = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                json.toString()
        );

        // Formamos la petición con el cuerpo creado
        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", formBody.contentType().toString())
                .post(formBody)
                .build();

        // Hacemos la petición
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject json = new JSONObject(response.body().string());
                    if (response.isSuccessful()) {
                        startActivity(new Intent(ContactarDos.this, ContactarTres.class));
                        finish();
                    }
                    else {
                        PrintOnThread.show(getApplicationContext(), json.getString("statusText"));
                        SharedPreferencesHelper.getInstance(getApplicationContext()).clear();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call call, IOException e) { e.printStackTrace();}
        });
    }
}
