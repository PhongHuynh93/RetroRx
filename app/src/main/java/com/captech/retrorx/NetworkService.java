package com.captech.retrorx;


import android.support.v4.util.LruCache;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by cteegarden on 1/25/16.
 * todo - model layer
 */

public class NetworkService {

    private static String baseUrl = "https://dl.dropboxusercontent.com/u/57707756/";
    private NetworkAPI networkAPI;
    private OkHttpClient okHttpClient;
    // TODO: 8/21/16 3 -  let’s create a place to store any observable that we might reuse later.  I accomplish this by creating a LruCache inside of my singleton class.
    private LruCache<Class<?>, Observable<?>> apiObservables;

    public NetworkService() {
        this(baseUrl);
    }

    /**
     * todo 2 - Next we need to place the Retrofit API interface and Retrofit builder inside of our newly created Singleton.
     *
     * @param baseUrl
     */
    public NetworkService(String baseUrl) {
        okHttpClient = buildClient();
        apiObservables = new LruCache<>(10);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient)
                .build();

        networkAPI = retrofit.create(NetworkAPI.class);
    }

    /**
     * Method to return the API interface.
     *
     * @return
     */
    public NetworkAPI getAPI() {
        return networkAPI;
    }


    /**
     * Method to build and return an OkHttpClient so we can set/get
     * headers quickly and efficiently.
     *
     * @return
     */
    public OkHttpClient buildClient() {

        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response response = chain.proceed(chain.request());
                // Do anything with response here
                //if we ant to grab a specific cookie or something..
                return response;
            }
        });

        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                //this is where we will add whatever we want to our request headers.
                Request request = chain.request().newBuilder().addHeader("Accept", "application/json").build();
                return chain.proceed(request);
            }
        });

        return builder.build();
    }

    /**
     * Method to clear the entire cache of observables
     */
    public void clearCache() {
        apiObservables.evictAll();
    }


    /**
     * todo 3 - create a method that our presenter will call to retrieve an observable.
     * Method to either return a cached observable or prepare a new one.
     *
     * The method above accepts any typed observable as well as the corresponding class parameter, which we will use to store our observable in the LruCache.
     * The boolean parameters are used to determine if we want to cache the observable or reuse a cached observable. If you always want to cache and reuse your observable, these are not necessary.
     *  @param unPreparedObservable
     * @param clazz
     * @param cacheObservable
     * @param useCache
     * @return Observable ready to be subscribed to
     */
    public Observable<?> getPreparedObservable(Observable<?> unPreparedObservable, Class<?> clazz, boolean cacheObservable, boolean useCache) {

        Observable<?> preparedObservable = null;

        if (useCache)//this way we don't reset anything in the cache if this is the only instance of us not wanting to use it.
            preparedObservable = apiObservables.get(clazz);

        if (preparedObservable != null)
            return preparedObservable;


        //we are here because we have never created this observable before or we didn't want to use the cache...

        preparedObservable = unPreparedObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());

        /**
         * todo 4 -  In order to appropriately cache the observable, we need to call the cache() method on it and to add the it to the LruCache.
         */
        if (cacheObservable) {
            // .  If we don't call the cache() method on the observable before we add it to the LruCache the instance of the observable will be saved but the response/request will not.
            preparedObservable = preparedObservable.cache();
            apiObservables.put(clazz, preparedObservable);
        }


        return preparedObservable;
    }


    /**
     * all the Service alls to use for the retrofit requests.
     */
    public interface NetworkAPI {


        @GET("FriendLocations.json")
//real endpoint
        Call<FriendResponse> getFriends();


        @GET("FriendLocations.json")
            //real endpoint
        Observable<FriendResponse> getFriendsObservable();

    }

}
