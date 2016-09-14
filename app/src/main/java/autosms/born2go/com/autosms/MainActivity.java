package autosms.born2go.com.autosms;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import autosms.born2go.com.autosms.model.HttpsTrustManager;
import autosms.born2go.com.autosms.services.SMSCheckSent;
import autosms.born2go.com.autosms.services.SMSServices;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String USERNAME = "sms";
    private static final String PASSWORD = "10136";
    private String auth_key;
    private Context thiz;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        thiz = this;
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS},
                    101);
            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant

            return;
        }



        if (getIntent().getAction() != null) {
            if (getIntent().getAction().equals("CANCEL")) {
                cancelSmsSevices();
                finish();
            }
        } else {

        }
        //registerReceiverCheckSent();
    }

    private void registerReceiverCheckSent() {
        try {
            //regester Recive
            registerReceiver(new SMSCheckSent(), new IntentFilter("SMS_SENT"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void login() {
        HttpsTrustManager.allowAllSSL();
        String url = "https://222.255.29.25:8443/laoschoolws/login";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Service/login()", response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Service/login()", error.toString());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("api_key", "SMS");
                params.put("sso_id", USERNAME);
                params.put("password", PASSWORD);
                return params;
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                auth_key = response.headers.get("auth_key");
                Log.d(TAG, "auth_key:" + auth_key);
                startAutoSMS();
                // saveAuthKey(key);
                return super.parseNetworkResponse(response);
            }
        };

        AutoSMSSingleton.getInstance().getRequestQueue().add(stringRequest);
    }

    private void startAutoSMS() {
        //start sevices
        Intent intent = new Intent(thiz, SMSServices.class);
        intent.putExtra("auth_key", auth_key);
        pendingIntent = PendingIntent.getBroadcast(thiz, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 61000,
                pendingIntent);

        showNotification();

        finish();
    }

    private void showNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setContentTitle("LaoSchool-Send SMS").setSmallIcon(R.drawable.ic_stop_black_24dp);
        //.setDeleteIntent(pendingIntent);;
        mBuilder.setOngoing(true);


        Intent intent1 = new Intent(thiz, MainActivity.class);
        intent1.setAction("CANCEL");
        PendingIntent stopMainIntent = PendingIntent.getActivity(this, 61000, intent1, PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.addAction(R.drawable.ic_stop_black_24dp, "STOP", stopMainIntent);
        mBuilder.setContentIntent(stopMainIntent);


        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mBuilder.setContentIntent(stopMainIntent);
        mNotifyMgr.notify(61000, mBuilder.build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public void start(View view) {
        // sendSMS("", "test");
        login();
    }

    public void stop(View view) {
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    void cancelSmsSevices() {
        Log.d(TAG, "STOP Auto SMS");

        Intent intent = new Intent(thiz, SMSServices.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(thiz, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);

        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(61000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 101: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! do the
                    // calendar task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

        }
    }

    public void sendSMS(String phoneNumber, String message) {
        try {
            String SENT = "SMS_SENT";
            PendingIntent sentPI = PendingIntent.getBroadcast(thiz, 0, new Intent(SENT), PendingIntent.FLAG_UPDATE_CURRENT);
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, sentPI, null);
            Log.d(TAG, "Send sms to:" + phoneNumber + ",message:" + message);
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "sendSMS() -Error message:" + ex.getMessage());
        }
    }


}
