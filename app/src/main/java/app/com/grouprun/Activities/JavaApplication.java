package app.com.grouprun.Activities;

import android.app.Application;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

/**
 * Created by Neegbeah Reeves on 2/5/2016.
 */
public class JavaApplication extends Application {

    @Override
    public void onCreate(){
        super.onCreate();

        //PubNub SDK Credentials
        Pubnub pubnub = new Pubnub("pub-c-330ec2e2-f6e7-4558-9010-5247b1f0b098", "sub-c-bad78d26-6f9f-11e5-ac0d-02ee2ddab7fe");

        try {
            pubnub.subscribe("MainRunning", new Callback() {
                public void successCallback(String channel, Object message) {
                    System.out.println(message);
                }

                public void errorCallback(String channel, PubnubError error) {
                    System.out.println(error.getErrorString());
                }
            });
        } catch (PubnubException e) {
            e.printStackTrace();
        }

    }
}
