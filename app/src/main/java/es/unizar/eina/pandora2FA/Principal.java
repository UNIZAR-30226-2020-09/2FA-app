package es.unizar.eina.pandora2FA;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import es.unizar.eina.pandora2FA.plataforma.ContactarUno;
import es.unizar.eina.pandora2FA.plataforma.SobrePandora;
import es.unizar.eina.pandora2FA.utiles.PrintOnThread;
import es.unizar.eina.pandora2FA.utiles.SharedPreferencesHelper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Principal extends AppCompatActivity {

    private final String urlget2FAkey = "https://pandorapp.herokuapp.com/api/2FA/get2FAkey";
    private final String urlEliminarCuenta = "https://pandorapp.herokuapp.com/api/usuarios/eliminar";
    private final String urlCerrarSesion = "https://pandorapp.herokuapp.com/api/2FA/logout";

    private final OkHttpClient httpClient = new OkHttpClient();

    // Información del usuario.
    private String email;
    private String password;
    private ArrayList<JSONObject> lista_respuesta = new ArrayList<>();


    // Elementos de la interfaz.
    private Toolbar toolbar;
    private TextView drawerEmail;
    private DrawerLayout drawer;
    private NavigationView drawerView;
    private View headerDrawer;
    private TextView key2FA;
    private TextView segundosCuenta;

    //key2FA
    private String stringkey2FA;
    private int intsegudnosCuenta = 0;

    private int segundosMin = 0;
    private int segundosMax = 30;

    private Timer timer = new Timer();
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            try {
                actualizarCodigo();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        // Recuperar información del usuario.
        final SharedPreferencesHelper sharedPreferencesHelper = SharedPreferencesHelper.getInstance(getApplicationContext());
        email = sharedPreferencesHelper.getString("email",null);
        password = sharedPreferencesHelper.getString("password",null);

        key2FA = findViewById(R.id.id_key2FA);
        segundosCuenta = findViewById(R.id.id_segudnos);

        long lastCodeDate = sharedPreferencesHelper.getLong("lastCodeDate");
        long tActual = SystemClock.uptimeMillis();

        if((tActual - lastCodeDate) > 30000){
            intsegudnosCuenta = segundosMin;
        }
        else{
            intsegudnosCuenta = (int)(tActual/1000 - lastCodeDate/1000);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    key2FA.setText(sharedPreferencesHelper.getString("codeTFA"));
                }
            });
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                segundosCuenta.setText(String.valueOf(intsegudnosCuenta));
            }
        });


        timer.schedule(timerTask, 0, 1000);


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
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                drawerEmail.setText(email);
            }
        });
    }


    public void cerrarSesion(MenuItem menuItem){
        doPostCerrarSesion();
        timerTask.cancel();
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


    public void actualizarCodigo() throws InterruptedException {

        if(intsegudnosCuenta > segundosMin){
            intsegudnosCuenta --;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    segundosCuenta.setText(String.valueOf(intsegudnosCuenta));
                }
            });
            return;
        }
        SharedPreferencesHelper sharedPreferencesHelper = SharedPreferencesHelper.getInstance(getApplicationContext());
        sharedPreferencesHelper.put("lastCodeDate", SystemClock.uptimeMillis());
        intsegudnosCuenta = segundosMax;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                segundosCuenta.setText(String.valueOf(intsegudnosCuenta));
            }
        });

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
                .get()
                .addHeader("Authorization", token)
                .build();

        // Hacemos la petición SÍNCRONA
        // Enviamos la petición en un thread nuevo y actuamos en función de la respuesta
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try (Response response = httpClient.newCall(request).execute()) {
                    JSONObject json = new JSONObject(response.body().string());
                    if (response.isSuccessful()) {
                        stringkey2FA = json.getString("key");
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                key2FA.setText(stringkey2FA);
                            }
                        });
                        SharedPreferencesHelper.getInstance(getApplicationContext()).put("codeTFA", stringkey2FA);
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

    public void doPostCerrarSesion() {
        // Recogemos el token
        String token = SharedPreferencesHelper.getInstance(getApplicationContext()).getString("token");

        // Formamos la petición con el token
        JSONObject json = new JSONObject();
        RequestBody formBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString());
        final Request request = new Request.Builder()
                .url(urlCerrarSesion)
                .addHeader("Authorization", token)
                .post(formBody)
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
