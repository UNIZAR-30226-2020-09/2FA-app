package es.unizar.eina.pandora2FA.reestablecer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import es.unizar.eina.pandora2FA.Principal;
import es.unizar.eina.pandora2FA.R;
import es.unizar.eina.pandora2FA.utiles.PrintOnThread;
import es.unizar.eina.pandora2FA.utiles.SharedPreferencesHelper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ReestablecerDos extends AppCompatActivity {

    final String url = "https://pandorapp.herokuapp.com/api/2FA/verificarReset";
    private final OkHttpClient httpClient = new OkHttpClient();

    private TextView codigo;
    private TextView nuevaContra;
    private Button verificar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reestablecer_dos);

        codigo = findViewById(R.id.codigo_verificacion);
        nuevaContra = findViewById(R.id.ueva_clave_reestablecer);
        verificar = findViewById(R.id.verificar_codigo);

    }

    public void verificar(View view) throws InterruptedException {

        verificar.setEnabled(false);
        SharedPreferencesHelper sharedPreferencesHelper = SharedPreferencesHelper.getInstance(getApplicationContext());
        doPost(sharedPreferencesHelper.getString("email"), sharedPreferencesHelper.getString("password"), nuevaContra.getText().toString(), codigo.getText().toString());
        verificar.setEnabled(true);
    }

    private void doPost(final String correo, final String oldContrasena, final String newContrasena, final String codigo) throws InterruptedException {
        Log.d("correo", correo);
        Log.d("oldContrasena", oldContrasena);
        Log.d("newContrasena", newContrasena);
        Log.d("codigo", codigo);

        // Formamos un JSON con los parámetros
        JSONObject json = new JSONObject();
        try{
            json.accumulate("mail",correo);
            json.accumulate("oldMasterPassword",oldContrasena);
            json.accumulate("newMasterPassword",newContrasena);
            json.accumulate("resetCode",codigo);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        // Formamos el cuerpo de la petición con el JSON creado
        RequestBody formBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString());

        // Formamos la petición con el cuerpo creado
        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", formBody.contentType().toString())
                .post(formBody)
                .build();


        // Hacemos la petición SÍNCRONA
        // Enviamos la petición en un thread nuevo y actuamos en función de la respuesta
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try (Response response = httpClient.newCall(request).execute()) {
                    JSONObject json = new JSONObject(response.body().string());
                    if (response.isSuccessful()) {
                        String token = json.getString("token");
                        SharedPreferencesHelper sharedPreferencesHelper = SharedPreferencesHelper.getInstance(getApplicationContext());
                        sharedPreferencesHelper.put("lastCodeDate", (long) 0);
                        sharedPreferencesHelper.put("token", token);
                        startActivity(new Intent(ReestablecerDos.this, Principal.class));
                        finishAffinity();
                    }else{
                        PrintOnThread.show(getApplicationContext(), json.getString("statusText"));
                    }
                }
                catch (IOException | JSONException e){
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        thread.join();
    }
}
