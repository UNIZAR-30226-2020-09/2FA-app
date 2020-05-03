package es.unizar.eina.pandora2FA.utiles;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

public class PrintOnThread {
    public static void show(final Context context, final String msg) {
        if (context != null && msg != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public static void changeValue(final Context context, final TextView view, final String texto) {
        if (context != null && texto != null && view != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    view.setText(texto);
                }
            });
        }
    }
}
