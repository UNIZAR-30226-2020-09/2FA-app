package es.unizar.eina.pandora2FA.plataforma;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import es.unizar.eina.pandora2FA.R;
import es.unizar.eina.pandora2FA.utiles.PrintOnThread;
import es.unizar.eina.pandora2FA.utiles.SharedPreferencesHelper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SobrePandora extends AppCompatActivity {

    final String url = "https://pandorapp.herokuapp.com/api/estadisticas";
    private final OkHttpClient httpClient = new OkHttpClient();

    TextView nUsuarios;
    TextView nPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sobre_pandora);
        nUsuarios = findViewById(R.id.sobre_pandora_nUsers);
        nPass = findViewById(R.id.sobre_pandora_nPass);
        doPost();
    }

    public void contactar(View item){
        SharedPreferencesHelper.getInstance(getApplicationContext()).put("guest",false);
        startActivity(new Intent(SobrePandora.this, ContactarUno.class));
    }

    private void doPost() {
        // Formamos la petición
        final okhttp3.Request request = new Request.Builder().url(url).build();

        // Hacemos la petición
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject json = new JSONObject(response.body().string());
                    if (response.isSuccessful()) {
                        PrintOnThread.changeValue(getApplicationContext(),nUsuarios,Integer.toString(json.getInt("nUsuarios")));
                        PrintOnThread.changeValue(getApplicationContext(),nPass,Integer.toString(json.getInt("nContraseñas")));
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
