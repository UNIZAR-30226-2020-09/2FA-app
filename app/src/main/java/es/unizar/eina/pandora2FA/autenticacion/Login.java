package es.unizar.eina.pandora2FA.autenticacion;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

public class Login extends AppCompatActivity {

    final String url = "https://pandorapp.herokuapp.com/api/2FA/login";
    private final OkHttpClient httpClient = new OkHttpClient();

    private TextView email;
    private TextView password;
    private Button entrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        email = findViewById(R.id.login_entrada_usuario);
        password = findViewById(R.id.login_entrada_clave);
        entrar = findViewById(R.id.login_entrar);

    }

    public void entrar(View view) throws InterruptedException {
        entrar.setEnabled(false);
        SharedPreferencesHelper sharedPreferencesHelper = SharedPreferencesHelper.getInstance(getApplicationContext());
        sharedPreferencesHelper.put("email", email.getText().toString().trim());
        sharedPreferencesHelper.put("password", password.getText().toString().trim());

        doPost(sharedPreferencesHelper.getString("email"),
                sharedPreferencesHelper.getString("password"));
        entrar.setEnabled(true);
    }

    private void doPost(final String correo, final String contrasena) throws InterruptedException {
        Log.d("correo", correo);
        Log.d("contrasena", contrasena);

        // Formamos un JSON con los parámetros
        JSONObject json = new JSONObject();
        try{
            json.accumulate("mail",correo);
            json.accumulate("masterPassword",contrasena);
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
                        sharedPreferencesHelper.put("token", token);
                        startActivity(new Intent(Login.this, Principal.class));
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
