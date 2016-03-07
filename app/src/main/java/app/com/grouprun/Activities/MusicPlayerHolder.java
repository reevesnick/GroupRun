package app.com.grouprun.Activities;

/**
 * Created by neegbeahreeves on 2/22/16.
 */

import android.app.Activity;
import android.os.Bundle;

import app.com.grouprun.R;

/**
 * This is only the UI of the music player holder. It can be deleted at any time.
 *
 */
public class MusicPlayerHolder extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
    }


}
