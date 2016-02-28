package app.com.grouprun;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseFacebookUtils;
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

        parseInit();

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
        ParseFacebookUtils.initialize(this);

    }
}