package com.example.friendschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {

    private EditText registerEmail,registerPassword;
    private Button register;
    private TextView alreadyHaveAnAccount;

    FirebaseAuth mAuth;
    private ProgressDialog pd;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        pd=new ProgressDialog(this);
        mAuth=FirebaseAuth.getInstance();
        rootRef= FirebaseDatabase.getInstance().getReference();
        InitializeFields();

        alreadyHaveAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail=registerEmail.getText().toString();
                String password=registerPassword.getText().toString();
                CreateNewUser(mail,password);
            }
        });


    }

    private void CreateNewUser(String mail, String password) {

        pd.setTitle("Creating User");
        pd.setMessage("Please wait");
        pd.show();
        mAuth.createUserWithEmailAndPassword(mail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){


                    String deviceToken= FirebaseInstanceId.getInstance().getToken();

                    rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).setValue("");

                    rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("devicetoken").setValue(deviceToken);
                    Toast.makeText(RegisterActivity.this, "Registered to FriendsChat!!", Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                    pd.dismiss();
                }
                else{
                    String message=task.getException().getMessage();
                    Toast.makeText(RegisterActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }

            }
        });

    }

    private void InitializeFields() {
        registerEmail=findViewById(R.id.register_email);
        registerPassword=findViewById(R.id.register_password);
        register=findViewById(R.id.register_button);
        alreadyHaveAnAccount=findViewById(R.id.already_have_account_login);
    }
}