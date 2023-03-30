package com.eneas.fernsteuerung;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
//import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


import java.io.PrintStream;
import java.io.OutputStream;




import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private WifiManager wifiManager;
    private String ssid;

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_WIFI_STATE = 1;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    private static final String TAG = "MainActivity";

    private ImageView CamImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        registerReceiver(networkStateReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_WIFI_STATE}, MY_PERMISSIONS_REQUEST_ACCESS_WIFI_STATE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        }

        CamImage = findViewById(R.id.CamImage);


        System.setOut(new FilteringPrintStream(System.out));


        Button buttondown = findViewById(R.id.buttondown);
        buttondown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "Rückwärts fahren geht leider nicht.";
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
        Button buttonup = findViewById(R.id.buttonup);
        buttonup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "Forwärts fahren geht leider nicht.";
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
        Button buttonleft = findViewById(R.id.buttonleft);
        buttonleft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "Nach links fahren geht leider nicht.";
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
        Button buttonright = findViewById(R.id.buttonright);
        buttonright.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "Nach rechts fahren geht leider nicht.";
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }


    public class FilteringPrintStream extends PrintStream {

        public FilteringPrintStream(OutputStream out)  {
            super(out);
        }

        @Override
        public void println(String x) {
            if (x==null || !x.contains("isSBSettingEnabled")) {
                super.println(x);
            }
        }
    }



    private BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (networkInfo != null && networkInfo.isConnected()) {
                    ssid = getSSID(getApplicationContext());
                    Log.d(TAG, "Connected to network: " + ssid);
                    capture();
                }
            }
        }
    };

/*    private Bitmap resizeBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        if (bitmap == null) {
            Log.d(TAG, "Bitmap is null");
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }*/



    private void capture() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Bitmap bitmap;
                    ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();

                    if (activeNetwork != null && activeNetwork.isConnected() && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        ssid = getSSID(getApplicationContext());
//                        Log.d(TAG, "Current SSID: " + ssid);
                        if (ssid != null && ssid.contains("Auto Eneas")) {
                            bitmap = getBitmapFromURL("http://192.168.4.1/capture");
//                            bitmap = resizeBitmap(bitmap, 800, 800);
                            Bitmap finalBitmap1 = bitmap;
                            CamImage.post(new Runnable() {
                                @Override
                                public void run() {
                                    CamImage.setImageBitmap(finalBitmap1);
                                }
                            });

                        } else {
                            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.carnc);
                        }
                    } else {
                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.carnc);
                    }

                    Bitmap finalBitmap = bitmap;
                    CamImage.post(new Runnable() {
                        @Override
                        public void run() {
                            CamImage.setImageBitmap(finalBitmap);
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error occurred while capturing image", e);
                }

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                }

                capture();
            }
        });
    }


    private Bitmap getBitmapFromURL(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getSSID(Context context) {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                String ssid = wifiInfo.getSSID();
                if (ssid != null && !ssid.isEmpty() && !ssid.equals("<unknown ssid>")) {
                    return ssid;
                }
            }
        }
        return "Unable to retrieve SSID";
    }
}