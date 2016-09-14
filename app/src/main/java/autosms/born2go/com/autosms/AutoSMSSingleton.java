package autosms.born2go.com.autosms;

import android.content.Context;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;

/**
 * Created by Hue on 8/16/2016.
 */
public class AutoSMSSingleton {
    private static AutoSMSSingleton instance;
    private final Context context;
    private static RequestQueue mRequestQueue;

    public AutoSMSSingleton(Context context) {
        this.context = context;
    }

    public static void initInstance(Context context) {
        if (instance == null) {
            instance = new AutoSMSSingleton(context);
        }


    }
    public static AutoSMSSingleton getInstance() {
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
//            mRequestQueue = Volley.newRequestQueue(context);
            Cache cache = new DiskBasedCache(context.getCacheDir(), 10 * 1024 * 1024);
            Network network = new BasicNetwork(new HurlStack());
            mRequestQueue = new RequestQueue(cache, network);
            // Don't forget to start the volley request queue
            mRequestQueue.start();
        }
        return mRequestQueue;
    }
}
