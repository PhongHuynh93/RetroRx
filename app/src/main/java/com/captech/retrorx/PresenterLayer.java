package com.captech.retrorx;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Observer;
import rx.Subscription;

/**
 * Created by cteegarden on 1/28/16.
 */
@SuppressWarnings("unchecked")
public class PresenterLayer implements PresenterInteractor {

    private ActivityView view;
    private NetworkService service;
    private Subscription subscription;

    public PresenterLayer(ActivityView view, NetworkService service){
        this.view = view;
        this.service = service;
    }

    /**
     * todo 5 - presenter call observable
     */
    public void loadRxData(){
        view.showRxInProcess();
        // use cache
        Observable<FriendResponse> friendResponseObservable = (Observable<FriendResponse>)
                service.getPreparedObservable(service.getAPI().getFriendsObservable(), FriendResponse.class, true, true);
        // each time we call subscribe() from any observer we will reuse any response/request that has already been made.
//        This means our onNext() and onComplete() will fire instantaneously if the response has already been returned and our request will only be made once.
        subscription = friendResponseObservable.subscribe(new Observer<FriendResponse>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                view.showRxFailure(e);
            }

            @Override
            public void onNext(FriendResponse response) {
                view.showRxResults(response);
            }
        });
    }

    public void loadRetroData(){
        view.showRetroInProcess();
        Call<FriendResponse> call = service.getAPI().getFriends();
        call.enqueue(new Callback<FriendResponse>() {
            @Override
            public void onResponse(Call<FriendResponse> call, Response<FriendResponse> response) {
                view.showRetroResults(response);
            }

            @Override
            public void onFailure(Call<FriendResponse> call, Throwable t) {
                view.showRetroFailure(t);
            }
        });
    }

    public void rxUnSubscribe(){
        if(subscription!=null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
    }


}
