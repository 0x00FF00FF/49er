package org.rares.miner49er;

import android.app.Application;
import net.danlew.android.joda.JodaTimeAndroid;
import org.rares.miner49er.persistence.StorioFactory;

//import com.squareup.leakcanary.LeakCanary;
//import com.squareup.leakcanary.RefWatcher;

/**
 * @author rares
 * @since 04.03.2018
 */

public class Miner49erApplication extends Application {
/*    private RefWatcher refWatcher;

    public static RefWatcher getRefWatcher(Context context) {
        Miner49erApplication application = (Miner49erApplication) context.getApplicationContext();
        return application.refWatcher;
    }*/

    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);

        StorioFactory.INSTANCE.setup(this);

//        NetworkingService.INSTANCE.start();

/*        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        refWatcher = LeakCanary.refWatcher(this)
                .watchDelay(15, TimeUnit.SECONDS)
                .buildAndInstall();*/
    }
}
