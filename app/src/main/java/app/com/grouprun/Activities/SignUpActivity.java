package app.com.grouprun.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import app.com.grouprun.R;


public class SignUpActivity extends AppCompatActivity {

    private EditText firstName;
    private EditText lastName;
    private EditText email;
    private EditText username;
    private EditText password;
    private Button signIN;
    private TextView login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        username = (EditText)findViewById(R.id.input_username);
        email = (EditText)findViewById(R.id.input_email);
        signIN = (Button)findViewById(R.id.button_signup);
        password = (EditText)findViewById(R.id.input_password);
        login = (TextView)findViewById(R.id.link_login);
        register();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),getParent().getClass());
                startActivity(intent);
            }
        });
    }


    public void register(){
        signIN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser user = new ParseUser();
                user.setEmail(email.getText().toString());
                user.setUsername(username.getText().toString());
                user.setPassword(password.getText().toString());
                user.signUpInBackground(new SignUpCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            signUpMsg("Account Created Successfully");

                            Intent sigin_intent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(sigin_intent);
                        } else {
                            signUpMsg("Account already taken.");
                        }
                    }
                });
            }

        });
    }

    protected void signUpMsg(String msg) {
        // TODO Auto-generated method stub
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }


}
