package es.unizar.eina.pandora2FA.reestablecer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

    private SharedPreferencesHelper sharedPreferencesHelper;

    private Button confirmar;

    String savedEmail;

    private EditText codigo;
    private EditText password;          // Campo de contraseña.
    private EditText confirmPassword;   // Campo de confirmar contraseña.

    private Boolean mostrandoPass1 = false;
    private Boolean mostrandoPass2 = false;
    private ImageButton registro_mostrar1;
    private ImageButton registro_mostrar2;

    //Comprobación longitud contraseñas
    private Boolean passwordCheckLength = false;

    // Comprobación de que las dos contraseñas son iguales.
    private Boolean confirmCheck = false;

    // Comprobaciones de patrones y caracteres de los campos del registro.
    private Boolean passwordCheckValue = false;

    // Comprobación de que los campos de las contraseñas no están vacíos
    private Boolean passwordEmpty = true;

    private TextView limitPass;     // Límite de caracteres de la contraseña.
    private TextView limitPass2;    // Límite de caracteres de confirmar contraseña.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reestablecer_dos);
        sharedPreferencesHelper = SharedPreferencesHelper.getInstance(getApplicationContext());

        codigo = findViewById(R.id.codigo_verificacion);
        confirmar = findViewById(R.id.registro2_confirmar);
        password = findViewById(R.id.registro2_clave1);
        confirmPassword = findViewById(R.id.registro2_clave2);
        limitPass = findViewById(R.id.registro2_long_clave);
        limitPass2 = findViewById(R.id.registro2_long_clave2);
        registro_mostrar1 = findViewById(R.id.registro2_imagen_ver);
        registro_mostrar2 = findViewById(R.id.registro2_imagen_ver2);

        password.addTextChangedListener(registerTextWatcher);
        confirmPassword.addTextChangedListener(registerTextWatcher);

        savedEmail = SharedPreferencesHelper.getInstance(getApplicationContext()).getString("email");
    }

    private TextWatcher registerTextWatcher = new TextWatcher() {

        private String _password;
        private String _confirmpassword;

        private String newLimitpassword;
        private String newLimitpassword2;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            _password = password.getText().toString().trim();
            _confirmpassword = confirmPassword.getText().toString().trim();

            int passchars = 40 - _password.length();
            if (passchars < 0) {
                limitPass.setTextColor(Color.rgb(200,0,0));
            }
            else {
                limitPass.setTextColor(Color.rgb(128,128,128));
            }

            newLimitpassword = Integer.toString(passchars);

            Integer pass2chars = 40 - _confirmpassword.length();
            if (passchars < 0) {
                limitPass2.setTextColor(Color.rgb(200,0,0));
            }
            else {
                limitPass2.setTextColor(Color.rgb(128,128,128));
            }

            newLimitpassword2 = pass2chars.toString();

            limitPass.setText(newLimitpassword);
            limitPass2.setText(newLimitpassword2);

            passwordEmpty = _password.isEmpty();
        }

        @Override
        public void afterTextChanged(Editable s) {
            passwordCheckLength = _password.length() >= 8 &&  _password.length() <= 40;
            passwordCheckValue = checkValue(_password);

            //Comprueba que las dos claves son iguales
            confirmCheck = _password.equals(_confirmpassword);
            confirmar.setEnabled(!passwordEmpty);
        }

        private boolean checkValue(String _password){
            final String specialChars = "@#$%!";
            char currentCharacter;
            boolean numberPresent = false;
            boolean upperCasePresent = false;
            boolean lowerCasePresent = false;
            boolean specialCharacterPresent = false;
            for (int i = 0; i < _password.length(); i++) {
                currentCharacter = _password.charAt(i);
                if (Character.isDigit(currentCharacter)) {
                    numberPresent = true;
                } else if (Character.isUpperCase(currentCharacter)) {
                    upperCasePresent = true;
                } else if (Character.isLowerCase(currentCharacter)) {
                    lowerCasePresent = true;
                } else if (specialChars.contains(String.valueOf(currentCharacter))) {
                    specialCharacterPresent = true;
                }
            }
            return numberPresent && upperCasePresent && lowerCasePresent && specialCharacterPresent;
        }
    };

    public void password_visible(View view){
        if (mostrandoPass1){
            password.setTransformationMethod(PasswordTransformationMethod.getInstance());
            mostrandoPass1 = false;
            registro_mostrar1.setColorFilter(R.color.colorPrimaryDark);
        }
        else{
            password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            mostrandoPass1= true;
            registro_mostrar1.clearColorFilter();
        }
    }

    public void password2_visible(View view){
        if (mostrandoPass2){
            confirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            mostrandoPass2 = false;
            registro_mostrar2.setColorFilter(R.color.colorPrimaryDark);
        }
        else{
            confirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            mostrandoPass2= true;
            registro_mostrar2.clearColorFilter();
        }
    }

    // Se registra el usuario si se cumplen todos los requisitos, si no se muestra el error.
    public void verificar(View view) throws InterruptedException {
        if (!passwordCheckLength) {
            Toast.makeText(getApplicationContext(),"La contraseña tiene que tener entre 8 y 40 caracteres.", Toast.LENGTH_LONG).show();
        }
        else if(!passwordCheckValue) {
            Toast.makeText(getApplicationContext(),"La contraseña debe tener al menos una minúscula, una mayúscula, un número y un carácter especial @#$%!", Toast.LENGTH_LONG).show();
        }
        else if (!confirmCheck) {
            Toast.makeText(getApplicationContext(),"Las contraseñas no coinciden.", Toast.LENGTH_LONG).show();
        }
        else {
            String password_in = password.getText().toString().trim();
            confirmar.setEnabled(false);

            doPost(sharedPreferencesHelper.getString("email"), sharedPreferencesHelper.getString("password"), password.getText().toString(), codigo.getText().toString());
            confirmar.setEnabled(true);
        }
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
