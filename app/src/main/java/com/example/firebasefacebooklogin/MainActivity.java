package com.example.firebasefacebooklogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    CallbackManager mCallbackManager;
    LoginButton loginButton;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       /* FacebookSdk.sdkInitialize(getApplicationContext());*/
        AppEventsLogger.activateApp(this);

        firebaseAuth=FirebaseAuth.getInstance();

        mCallbackManager=CallbackManager.Factory.create();

        loginButton=findViewById(R.id.login_button);

        //setting the permission
        loginButton.setReadPermissions("public_profile","email","user_birthday","user_friends");

        //registering callback
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                //sign in completed

                //handle the token from firebase
handlerFacebookAccessToken(loginResult.getAccessToken());

//getting the user information
                GraphRequest request=GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try {
                            String email=object.getString("email");
                            String birthday=object.getString("birthday");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                Bundle parameters=new Bundle();
                parameters.putString("fields","id,name,email,gender,birthday");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser=firebaseAuth.getCurrentUser();

        if (currentUser!=null){
            Toast.makeText(this, "Logged in", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "No one Logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void handlerFacebookAccessToken(AccessToken token){
        AuthCredential credential= FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    FirebaseUser user=firebaseAuth.getCurrentUser();
                    Toast.makeText(MainActivity.this, "Log in completed with user: "+user, Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(MainActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
