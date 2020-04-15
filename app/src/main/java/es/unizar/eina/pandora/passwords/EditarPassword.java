package es.unizar.eina.pandora.passwords;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

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

public class EditarPassword extends AppCompatActivity {

    final String url = "https://pandorapp.herokuapp.com/api/categorias/listar";
    final String urlEditar = "https://pandorapp.herokuapp.com/api/contrasenya/modificar";
    private final OkHttpClient httpClient = new OkHttpClient();

    private EditText nombre;
    private EditText usuario;
    private EditText password;
    private EditText validez;
    private EditText nota;

    private Integer _id;
    private String _nombre;
    private String _usuario;
    private String _password;
    private String _validez;
    private String _nota;
    private String category_name;

    private String spassword;

    //Para el Spinner con las categorias
    private Spinner categorias;
    ArrayList<String> name_category = new ArrayList<>();
    ArrayList<Integer> id_category = new ArrayList<>();
    Integer id_cat;
    JSONArray cat = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_password);

        nombre = findViewById(R.id.editar_password_nombre);
        usuario = findViewById(R.id.editar_password_usuario);
        password = findViewById(R.id.editar_password_pass);
        validez = findViewById(R.id.editar_password_dias);
        nota = findViewById(R.id.editar_password_nota);
        categorias=findViewById(R.id.editar_password_cat);

        //Conseguimos las categorias para el spinner
        try {
            doPostCategory();
            getCategoryNameAndId();
        } catch (InterruptedException | JSONException e) {
            e.printStackTrace();
        }

        //Recuperamos la información de la contraseña
        SharedPreferencesHelper sharedPreferencesHelper = SharedPreferencesHelper.getInstance(getApplicationContext());
        JSONObject password = new JSONObject();
        spassword = sharedPreferencesHelper.getString("password",null);
        Log.d("EDITAR OK",spassword);
        password = sharedPreferencesHelper.getJSONObject("Password_info");
        Log.d("Prueba",password.toString());
        try {
            _id = password.getInt("passId");
            _nombre = password.getString("passwordName");
            _usuario = password.getString("userName");
            _password = password.getString("password");
            Integer dias = password.getInt("noDaysBeforeExpiration");
            _validez = Integer.toString(dias);
            _nota = password.getString("optionalText");
            category_name = password.getString("categoryName");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        nombre.setText(_nombre);
        usuario.setText(_usuario);
        (this.password).setText(_password);
        validez.setText(_validez);
        nota.setText(_nota);

        ArrayAdapter<String> categoriesAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, name_category);
        categorias.setAdapter(categoriesAdapter);
        if (category_name!= null) {
            int spinnerPosition = categoriesAdapter.getPosition(category_name);
            categorias.setSelection(spinnerPosition);
        }
    }

    public void goConfirmar(View view){
       getCategory();
       _nombre = nombre.getText().toString();
       _usuario = usuario.getText().toString();
       _password = password.getText().toString();
       _validez = validez.getText().toString();
       Integer dias = Integer.valueOf(_validez);
       _nota = nota.getText().toString();
        doPostEditar(_id, _nombre, _usuario, _password,_nota, dias, id_cat);
    }

    //Conseguir el id de la categoria seleccionada
    private void getCategory(){
        String cat_name = categorias.getSelectedItem().toString();
        //Buscamos su id:
        boolean encontrado=false;
        String aux;
        int i = 0;
        while (!id_category.isEmpty() && !encontrado ){
            aux = name_category.get(i);
            if (aux.equals(cat_name)){
                Log.d("Encontrado","OK");
                encontrado = true;
                id_cat = id_category.get(i);
            }else{
                i++;
            }
        }
    }

    protected void getCategoryNameAndId() throws JSONException {
        JSONObject aux;
        String name;
        Integer id;
        for (int i=0; i < cat.length();i++){
            aux = cat.getJSONObject(i);
            name = aux.getString("categoryName");
            id = aux.getInt("catId");
            Log.d("Category",name + "" + Integer.toString(id));
            name_category.add(name);
            id_category.add(id);
        }
        Log.d("OK",Integer.toString(cat.length()));
    }

    public void doPostCategory() throws InterruptedException {
        String token = SharedPreferencesHelper.getInstance(getApplicationContext()).getString("token");

        // Formamos la petición con el cuerpo creado
        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", token)
                .build();

        // Enviamos la petición SÍNCRONA
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        Log.d("ERROR ", response.body().string());
                    } else {
                        final JSONObject json = new JSONObject(response.body().string());
                        cat = json.getJSONArray("categories");
                    }
                }
                catch (IOException | JSONException e){
                    Log.d("EXCEPCION ", e.getMessage());
                }
            }
        });
        thread.start();
        thread.join();
    }

    private void doPostEditar(final Integer _id,final String name, final String user, final String pass, final String note,
                        final int dias, final int categoria) {
        // Recuperamos el token
        String token = SharedPreferencesHelper.getInstance(getApplicationContext()).getString("token");

        // Formamos un JSON con los parámetros
        JSONObject json = new JSONObject();
        try{
            json.accumulate("masterPassword",spassword);
            json.accumulate("id",_id);
            json.accumulate("passwordName",name);
            json.accumulate("password",pass);
            json.accumulate("expirationTime",dias);
            json.accumulate("passwordCategoryId",categoria);
            json.accumulate("optionalText",note);
            json.accumulate("userName",user);
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
                .url(urlEditar)
                .addHeader("Content-Type", formBody.contentType().toString())
                .addHeader("Authorization", token)
                .post(formBody)
                .build();

        // Hacemos la petición
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try{
                    JSONObject json = new JSONObject(response.body().string());
                    if (response.isSuccessful()) {
                        Log.d("EDITAR","OK");
                        startActivity(new Intent(EditarPassword.this, Principal.class));
                        finishAffinity();
                    }
                    else {
                        PrintOnThread.show(getApplicationContext(), json.getString("statusText"));
                        SharedPreferencesHelper.getInstance(getApplicationContext()).clear();
                    }
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call call, IOException e) { e.printStackTrace();}
        });
    }
}