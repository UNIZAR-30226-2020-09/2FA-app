package es.unizar.eina.pandora.autenticacion;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import es.unizar.eina.pandora.Principal;
import es.unizar.eina.pandora.R;
import es.unizar.eina.pandora.utiles.PrintOnThread;
import es.unizar.eina.pandora.utiles.SharedPreferencesHelper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Login extends AppCompatActivity {

    final String url = "https://pandorapp.herokuapp.com/api/usuarios/login";
    private final OkHttpClient httpClient = new OkHttpClient();

    TextView email;
    TextView password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /*
        Por si vuelven a cambiar el token, necesitaremos iniciar sesión para que nos den uno nuevo
        //String _email = sharedPreferences.getString("email",null);
        //String _password = sharedPreferences.getString("password",null);
        // Si ya tenemos estos datos, iniciamos sesión automáticamente

        if (_email != null && _password != null) {
            doPost(sharedPreferences.getString("email",null),
                    sharedPreferences.getString("password",null));
            finish();
        }
        */



        email = findViewById(R.id.login_entrada_usuario);
        password = findViewById(R.id.login_entrada_clave);
    }

    public void entrar(View view) {
        SharedPreferencesHelper sharedPreferencesHelper = SharedPreferencesHelper.getInstance(getApplicationContext());
        sharedPreferencesHelper.put("email", email.getText().toString().trim());
        sharedPreferencesHelper.put("password", password.getText().toString().trim());

        doPost(sharedPreferencesHelper.getString("email"),
                sharedPreferencesHelper.getString("password"));
    }

    private void doPost(final String correo, final String contrasena) {
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

        // Hacemos la petición
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject json = new JSONObject(response.body().string());
                    if (response.isSuccessful()) {
                            String token = json.getString("token");
                            SharedPreferencesHelper sharedPreferencesHelper = SharedPreferencesHelper.getInstance(getApplicationContext());
                            sharedPreferencesHelper.put("token", token);
                            startActivity(new Intent(Login.this, Principal.class));
                            finishAffinity();
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
