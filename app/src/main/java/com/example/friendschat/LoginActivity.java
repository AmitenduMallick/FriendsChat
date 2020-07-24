package com.example.friendschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private EditText loginEmail,loginPassword;
    private TextView needAnotherAccount,forgotPassword;
    private Button loginUsingMail,loginUsingPhone;

    private FirebaseAuth mAuth;

    private FirebaseUser firebaseUser;
    private ProgressDialog pd;
    AwesomeValidation awesomeValidation;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        awesomeValidation=new AwesomeValidation(ValidationStyle.BASIC);

        pd=new ProgressDialog(this);
        mAuth=FirebaseAuth.getInstance();
        firebaseUser=mAuth.getCurrentUser();
        usersRef= FirebaseDatabase.getInstance().getReference().child("Users");

        InitializeFields();

        needAnotherAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),RegisterActivity.class));
            }
        });
        loginUsingMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String mail=loginEmail.getText().toString();
                String password=loginPassword.getText().toString();
                SignInUser(mail,password);
            }
        });

    }

    private void SignInUser(String mail, String password) {
        pd.setTitle("Login User");
        pd.setMessage("Please wait");

        pd.show();

        mAuth.signInWithEmailAndPassword(mail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    String currentUserId=mAuth.getCurrentUser().getUid();
                    String deviceToken= FirebaseInstanceId.getInstance().getToken();

                    usersRef.child(currentUserId).child("devicetoken").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){

                                Toast.makeText(LoginActivity.this, "Welcome to FriendsChat!!", Toast.LENGTH_SHORT).show();
                                Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                                pd.dismiss();

                            }

                        }
                    });

                }else{
                    String message=task.getException().getMessage();
                    Toast.makeText(LoginActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }

            }
        });
    }

    private void InitializeFields() {

        loginEmail=findViewById(R.id.login_email);
        loginPassword=findViewById(R.id.login_password);
        needAnotherAccount=findViewById(R.id.need_new_account_link);
        forgotPassword=findViewById(R.id.forgot_password_link);
        loginUsingMail=findViewById(R.id.login_button);
        loginUsingPhone=findViewById(R.id.login_using_phone);



    }
}