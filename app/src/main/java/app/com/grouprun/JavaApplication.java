package app.com.grouprun;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;
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

        pubnubInit();
        parseInit();

    }
    public void pubnubInit(){
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
    public void parseInit(){
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        // Add your initialization code here
        Parse.initialize(this, "bod2m2xVQt15kFgCHoRSXNDJ1e8XDdYQHEBH1sss", "DcLTk5ExvQFaFt8dG5yb5VAG8VigvNJpkQTunFhK");
        ParseUser.enableAutomaticUser();
        ParseACL defaultACL = new ParseACL();
        // Optionally enable public read access.
        // defaultACL.setPublicReadAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);
    }
}