package com.captech.retrorx;

import android.app.Application;

/**
 * Created by cteegarden on 1/26/16.
 * todo 1 - So how do we solve our lifecycle issue? Well first, let’s create a Singleton class which will be instantiated in our Application class.
 */
public class RxApplication extends Application {

    private NetworkService networkService;
    @Override
    public void onCreate() {
        super.onCreate();

        networkService = new NetworkService();

    }

    public NetworkService getNetworkService(){
        return networkService;
    }


}
