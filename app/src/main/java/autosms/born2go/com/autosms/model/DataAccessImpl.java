package autosms.born2go.com.autosms.model;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;


import autosms.born2go.com.autosms.AutoSMSSingleton;

/**
 * Created by Tran An on 14/03/2016.
 */
public class DataAccessImpl {
    private static final String TAG = DataAccessImpl.class.getSimpleName();
    private static DataAccessImpl mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;

    public static String api_key = "TEST";

    //    //VDC
    final String LOGIN_HOST = "https://222.255.29.25:8443/laoschoolws/";
    final String HOST = "https://222.255.29.25:8443/laoschoolws/api/";


    private DataAccessImpl(Context context) {
        mCtx = context;
        mRequestQueue = AutoSMSSingleton.getInstance().getRequestQueue();
        HttpsTrustManager.allowAllSSL();
    }

    public static synchronized DataAccessImpl getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DataAccessImpl(context);
        }

        return mInstance;
    }




}