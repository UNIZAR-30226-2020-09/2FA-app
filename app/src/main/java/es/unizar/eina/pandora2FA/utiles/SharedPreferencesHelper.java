package es.unizar.eina.pandora2FA.utiles;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class SharedPreferencesHelper {
    private static final String SETTINGS_NAME = "user";
    private static SharedPreferencesHelper instance;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;
    private boolean isBulkUpdate = false;

    private SharedPreferencesHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE);
    }


    public static SharedPreferencesHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferencesHelper(context.getApplicationContext());
        }
        return instance;
    }

    public void put(String key, String val) {
        doEdit();
        sharedPreferencesEditor.putString(key, val);
        doCommit();
    }

    public void put(String key, int val) {
        doEdit();
        sharedPreferencesEditor.putInt(key, val);
        doCommit();
    }

    public void put(String key, boolean val) {
        doEdit();
        sharedPreferencesEditor.putBoolean(key, val);
        doCommit();
    }

    public void put(String key, float val) {
        doEdit();
        sharedPreferencesEditor.putFloat(key, val);
        doCommit();
    }

    public void put(String key, double val) {
        doEdit();
        sharedPreferencesEditor.putString(key, String.valueOf(val));
        doCommit();
    }

    public void put(String key, long val) {
        doEdit();
        sharedPreferencesEditor.putLong(key, val);
        doCommit();
    }

    public void put(String key, JSONObject val) {
        doEdit();
        sharedPreferencesEditor.putString(key, val.toString());
        doCommit();
    }

    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    public String getString(String key) {
        return sharedPreferences.getString(key, null);
    }

    public int getInt(String key) {
        return sharedPreferences.getInt(key, 0);
    }

    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    public long getLong(String key) {
        return sharedPreferences.getLong(key, 0);
    }

    public long getLong(String key, long defaultValue) {
        return sharedPreferences.getLong(key, defaultValue);
    }

    public float getFloat(String key) {
        return sharedPreferences.getFloat(key, 0);
    }

    public float getFloat(String key, float defaultValue) {
        return sharedPreferences.getFloat(key, defaultValue);
    }

    public double getDouble(String key) {
        return getDouble(key, 0);
    }

    public double getDouble(String key, double defaultValue) {
        try {
            return Double.valueOf(sharedPreferences.getString(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    public JSONObject getJSONObject(String key) {
        String strJson = sharedPreferences.getString(key,"0");
        JSONObject response = new JSONObject();
        if (strJson != null) {
            try {
                response = new JSONObject(strJson);
            } catch (JSONException e) {
                Log.d("Excepcion","JSONOject");
            }
        }
        return response;
    }

    public void remove(String... keys) {
        doEdit();
        for (String key : keys) {
            sharedPreferencesEditor.remove(key);
        }
        doCommit();
    }

    public void clear() {
        doEdit();
        sharedPreferencesEditor.clear();
        doCommit();
    }

    public void edit() {
        isBulkUpdate = true;
        sharedPreferencesEditor = sharedPreferences.edit();
    }

    public void commit() {
        isBulkUpdate = false;
        sharedPreferencesEditor.commit();
        sharedPreferencesEditor = null;
    }

    private void doEdit() {
        if (!isBulkUpdate && sharedPreferencesEditor == null) {
            sharedPreferencesEditor = sharedPreferences.edit();
        }
    }

    private void doCommit() {
        if (!isBulkUpdate && sharedPreferencesEditor != null) {
            sharedPreferencesEditor.commit();
            sharedPreferencesEditor = null;
        }
    }
}