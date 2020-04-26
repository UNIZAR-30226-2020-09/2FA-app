package es.unizar.eina.pandora;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import es.unizar.eina.pandora.plataforma.ContactarUno;
import es.unizar.eina.pandora.plataforma.SobrePandora;
import es.unizar.eina.pandora.utiles.PrintOnThread;
import es.unizar.eina.pandora.utiles.SharedPreferencesHelper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Principal extends AppCompatActivity {

    private final String urlget2FAkey = "https://pandorapp.herokuapp.com/api/usuarios/get2FAkey";
    private final String urlEliminarCuenta = "https://pandorapp.herokuapp.com/api/usuarios/eliminar";

    private final OkHttpClient httpClient = new OkHttpClient();

    // Información del usuario.
    private String email;
    private String password;
    private ArrayList<JSONObject> lista_respuesta = new ArrayList<>();
    String key2FA;

    // Elementos de la interfaz.
    private Toolbar toolbar;
    private TextView drawerEmail;
    private DrawerLayout drawer;
    private NavigationView drawerView;
    private View headerDrawer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        // Recuperar información del usuario.
        SharedPreferencesHelper sharedPreferencesHelper = SharedPreferencesHelper.getInstance(getApplicationContext());
        email = sharedPreferencesHelper.getString("email",null);
        password = sharedPreferencesHelper.getString("password",null);



        // Menú desplegable.
        toolbar = findViewById(R.id.topbar_toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, 0, 0);
        drawer.addDrawerListener(toggle);
        drawerView = findViewById(R.id.principal_drawer);
        headerDrawer = drawerView.getHeaderView(0);
        drawerEmail = headerDrawer.findViewById(R.id.drawer_email);
        drawerEmail.setText(email);

    }








    public void cerrarSesion(MenuItem menuItem){
        SharedPreferencesHelper.getInstance(getApplicationContext()).clear();
        startActivity(new Intent(Principal.this, Inicio.class));
        finishAffinity();
    }

    public void contactar(MenuItem menuItem){
        SharedPreferencesHelper.getInstance(getApplicationContext()).put("guest",false);
        startActivity(new Intent(Principal.this, ContactarUno.class));
    }

    public void sobrePandora(MenuItem menuItem){
        startActivity(new Intent(Principal.this, SobrePandora.class));
    }

    public void eliminarCuenta(MenuItem menuItem){
        // Confirmar que queremos eliminar la cuenta
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        builder.setTitle("¿Está seguro de que quiere borrar su cuenta?");
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doPostEliminarCuenta();
            }
        }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }







    public void pulsarAdd() throws InterruptedException {
        // Recogemos el token
        String token = SharedPreferencesHelper.getInstance(getApplicationContext()).getString("token");

        JSONObject json = new JSONObject();
        try{
            //json.accumulate("masterPassword",password);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        // Formamos el cuerpo de la petición con el JSON creado
        RequestBody formBody = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                json.toString()
        );
        // Formamos la petición con el cuerpo creado
        final Request request = new Request.Builder()
                .url(urlget2FAkey)
                .addHeader("Content-Type", formBody.contentType().toString())
                .addHeader("Authorization", token)
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
                        key2FA = json.getString("key");
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

    public void doPostEliminarCuenta() {
        // Recogemos el token
        String token = SharedPreferencesHelper.getInstance(getApplicationContext()).getString("token");

        // Formamos la petición con el token (IMPORTANTE VER QUE ES DE TIPO DELETE)
        final Request request = new Request.Builder()
                .url(urlEliminarCuenta)
                .addHeader("Authorization", token)
                .delete()
                .build();

        // Hacemos la petición
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    SharedPreferencesHelper.getInstance(getApplicationContext()).clear();
                    startActivity(new Intent(Principal.this, Inicio.class));
                    finish();
                }
            }
            @Override
            public void onFailure(Call call, IOException e) { e.printStackTrace();}
        });
    }

}
