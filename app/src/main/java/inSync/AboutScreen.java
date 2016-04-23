package inSync;

import android.app.Activity;
import android.os.Bundle;

import app.com.grouprun.R;

public class AboutScreen extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about_screen);
	}

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
