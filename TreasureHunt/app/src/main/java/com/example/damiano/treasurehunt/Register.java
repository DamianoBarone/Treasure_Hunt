package com.example.damiano.treasurehunt;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.Bind;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Register extends AppCompatActivity implements BackgroundActions {
    private static final String TAG = "SignupActivity";
    private BackgroundTask mAuthTask = null;
    public static final String URL  = "http://192.168.168.176:8084/Treasure_server/Servlet";
    OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    EditText _nameText;
    EditText _emailText;
    EditText _surname;
    EditText _passwordText;
    Button _signupButton;
TextView _loginLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        _emailText = (EditText) findViewById(R.id.input_email);
        _nameText= (EditText) findViewById(R.id.input_name);
        _surname=(EditText) findViewById(R.id.input_surname);
        _passwordText=(EditText) findViewById(R.id.input_password);
        _signupButton=(Button)  findViewById(R.id.btn_signup);
        _loginLink=(TextView) findViewById(R.id.link_login);
        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // go to register activity
                startActivity(new Intent(Register.this, LoginActivity.class));
                finish();
            } });
    }
    String send(String url, String json)
    {    String servletresponse;
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {

            servletresponse=response.body().string();
            return servletresponse;
        } catch (IOException e) {
            System.out.println("eccezionale ");
            e.printStackTrace();
        }
        return "vuoto";

    }
    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);



        final String name = _nameText.getText().toString();
        final String surname = _surname.getText().toString();
        final String email = _emailText.getText().toString();
        final String password = _passwordText.getText().toString();

        mAuthTask = new BackgroundTask("{'message_type':'2',\n" +
                "'email': '"+ email + "',\n" +
                "'password': '"+ password + "',\n" +
                "'name': '"+ name + "',\n" +
                "'surname': '"+ surname + "',\n" +
                "',\n }", this);
        mAuthTask.execute((Void) null); //run in background
    }


    public void onSignupSuccess() {

        //startActivity(new Intent(LoginActivity.this, Register.class)); LOLLO METTI LA PROSSIMA ACITIVITI
        startActivity(new Intent(Register.this, ListActivity.class));

        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;
        String surname = _surname.getText().toString();
        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }
        if (surname.isEmpty() || surname.length() < 3) {
            _surname.setError("at least 3 characters");
            valid = false;
        } else {
            _surname.setError(null);
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    @Override
    public boolean BackgroundActions(String response) {
        if (response.equals("ok register")) {
            System.out.println(response);//activiti successiva
            onSignupSuccess();
        }
        return true;
    }

    @Override
    public void PostExecuteActions(Boolean success) {
        ;
    }

    @Override
    public void CancelledActions() {
        ;
    }



}
