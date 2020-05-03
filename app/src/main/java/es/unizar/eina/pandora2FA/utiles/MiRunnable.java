package es.unizar.eina.pandora2FA.utiles;

public abstract class MiRunnable implements Runnable {
    private boolean killed = false;

    public boolean isKilled() {
        return killed;
    }

    public void killRunnable(){
        killed = true;
    }
}
