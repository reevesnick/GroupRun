package app.com.grouprun.Activities;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.widget.LoginButton;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import app.com.grouprun.Fragments.FacebookButtonFragment;
import app.com.grouprun.R;

//import com.parse.LogInCallback;
//import com.parse.ParseException;
//import com.parse.ParseUser;
/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements FacebookCallback,
        FacebookButtonFragment.OnFragmentInteractionListener, LoaderCallbacks<Object> {
    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    final List<String> permissions = Arrays.asList("public_profile", "email");
    // UI references.
    private EditText userName;
    private EditText passwordText;
    private Button logInButton;
    private TextView signUpText;
    private LoginButton faceBookButton;
    String name;
    String email;
    ParseUser parseUser;
    ImageView mProfileImage;
    Bitmap image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        faceBookButton = (LoginButton) findViewById(R.id.facebook_login_button);


        // Set up the login form.
        userName = (EditText) findViewById(R.id.userName);
//        populateAutoComplete();
        passwordText = (EditText) findViewById(R.id.input_password);
        logInButton = (Button)findViewById(R.id.button_login);
        signUpText = (TextView)findViewById(R.id.link_signup);
        signin();
        register();
        parseSignin();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }
    private void register(){
        signUpText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(intent);
            }
        });
    }


    public void parseSignin(){

        faceBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseFacebookUtils.logInWithReadPermissionsInBackground(LoginActivity.this, permissions, new LogInCallback() {

                    @Override
                    public void done(final ParseUser user, ParseException err) {
                        if (user == null) {
                            Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
                        } else if (user.isNew()) {
                            Log.d("MyApp", "User signed up and logged in through Facebook!");
                            getUserDetailsFromFB();
                        } else if (!ParseFacebookUtils.isLinked(user)) {
                            ParseFacebookUtils.linkWithReadPermissionsInBackground(user, LoginActivity.this, permissions, new SaveCallback() {
                                @Override
                                public void done(ParseException ex) {
                                    if (ParseFacebookUtils.isLinked(user)) {
                                        getUserDetailsFromParse();
                                    }
                                }
                            });
                        } else {
                            getUserDetailsFromParse();
                        }
                    }
                });
            }
        });
    }

    public void onStart(){
        super.onStart();
        parseUser = ParseUser.getCurrentUser();

    }
    public void getUserDetailsFromParse() {
        parseUser = ParseUser.getCurrentUser();
        Toast.makeText(LoginActivity.this, "Welcome back " + parseUser.getUsername(), Toast.LENGTH_SHORT).show();
        Intent toMapActivity = new Intent(LoginActivity.this, MapActivity.class);
        startActivity(toMapActivity);
        finish();
    }
    private void getUserDetailsFromFB() {

        // Suggested by https://disqus.com/by/dominiquecanlas/
        Bundle parameters = new Bundle();
        parameters.putString("fields", "email,name,picture");
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me",
                parameters,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
         /* handle the result */
                        try {
                            email = response.getJSONObject().getString("email");
                            name = response.getJSONObject().getString("name");
                            JSONObject picture = response.getJSONObject().getJSONObject("picture");
                            JSONObject data = picture.getJSONObject("data");
                            //  Returns a 50x50 profile picture
                            String pictureUrl = data.getString("url");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();
    }
    private void saveNewUser(final Bitmap image) {
        parseUser = ParseUser.getCurrentUser();
        parseUser.setUsername(name);
        parseUser.setEmail(email);

        parseUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Intent toLogin = new Intent(LoginActivity.this, MapActivity.class);
                toLogin.putExtra("profilePic", image);
                Toast.makeText(LoginActivity.this, "Welcome " + name + ", thanks for signing up!", Toast.LENGTH_SHORT).show();
                startActivity(toLogin);
                finish();

            }
        });
    }

    private void signin(){
        logInButton.setOnClickListener(new OnClickListener() {


            @Override
            public void onClick(View v) {
                if ("".equals(userName.toString()) || "".equals(passwordText.toString())) {
//                    DISPLAY INPUT VALIDTION ERROR
                    return;
                } else {
                    ParseUser.logInInBackground(userName.getText().toString(), passwordText.getText().toString(), new LogInCallback() {
                        @Override
                        public void done(ParseUser parseUser, ParseException e) {
                            if (parseUser != null) {
                                Intent toLogin = new Intent(LoginActivity.this, MapActivity.class);
                                startActivity(toLogin);
                                userName.setText("");
                                passwordText.setText("");
                                signInMessage("You have successfully logged in!");

                            } else {
//                            DISPLAY ERRROR
                                signInMessage("Your username or password was incorrect.");
                                System.out.println(parseUser);
                            }
                        }
                    });

                }
            }
        });
    }

    protected void signInMessage(String s){
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

//    private void populateAutoComplete() {
//        if (!mayRequestContacts()) {
//            return;
//        }
//
//        getLoaderManager().initLoader(0, null, this);
//    }
//
//
//    private boolean mayRequestContacts() {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//            return true;
//        }
//        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
//            return true;
//        }
//        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
//            Snackbar.make(userName, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
//                    .setAction(android.R.string.ok, new OnClickListener() {
//                        @Override
//                        @TargetApi(Build.VERSION_CODES.M)
//                        public void onClick(View v) {
//                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
//                        }
//                    });
//        } else {
//            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
//        }
//        return false;
//    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                populateAutoComplete();
            }
        }
    }


    @Override
    public void onSuccess(Object o) {

    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onError(FacebookException error) {

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public Loader<Object> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Object> loader, Object data) {

    }

    @Override
    public void onLoaderReset(Loader<Object> loader) {

    }


}