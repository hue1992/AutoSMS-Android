package autosms.born2go.com.autosms;

import android.app.Application;

/**
 * Created by Hue on 8/16/2016.
 */
public class AutoSMSApplication extends Application {
    public AutoSMSApplication() {
        super();
    }
    @Override
    public void onCreate() {
        super.onCreate();
        init();

    }
    protected void init() {
        // Initialize the instance of TextToSpeechSingleton
        AutoSMSSingleton.initInstance(getApplicationContext());
    }
}
