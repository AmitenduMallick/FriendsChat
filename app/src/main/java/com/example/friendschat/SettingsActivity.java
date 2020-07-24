package com.example.friendschat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private EditText username,status;
    private Button updateStatus;
    private CircleImageView profileImage;
    FirebaseAuth auth;
    public static final int galleryCode=1;
    private StorageReference UserProfileImagesRef;
    Uri resultUri;
    private StorageTask uploadTask;
    String url;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        auth=FirebaseAuth.getInstance();
        UserProfileImagesRef= FirebaseStorage.getInstance().getReference().child("Profile Images");

        InitializeFields();
        checkForName();


        updateStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name=username.getText().toString();
                String stat=status.getText().toString();
                String uid=auth.getCurrentUser().getUid();

                if(TextUtils.isEmpty(name)){
                    Toast.makeText(SettingsActivity.this, "Please provide a valid username", Toast.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(stat)){
                    Toast.makeText(SettingsActivity.this, "Please provide a status", Toast.LENGTH_SHORT).show();

                }
                else {

                    HashMap<String, Object> map = new HashMap<>();
                    map.put("name", name);
                    map.put("status", stat);
                    map.put("uid", uid);


                    FirebaseDatabase.getInstance().getReference().child("Users").child(uid).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                Toast.makeText(SettingsActivity.this, "Profile Updated!!", Toast.LENGTH_SHORT).show();
                                Intent intent=new Intent(SettingsActivity.this,MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }

                        }
                    });
                }
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setCropShape(CropImageView.CropShape.OVAL).start(SettingsActivity.this);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==galleryCode&&resultCode==RESULT_OK&&data!=null){
            Uri imageUri=data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode==RESULT_OK){
                resultUri=result.getUri();
                uploadImage();
                


            }

        }else{
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImage() {
        final ProgressDialog pd=new ProgressDialog(this);
        pd.setMessage("Uploading");
        pd.show();
        if(resultUri!=null){
            final StorageReference fileRef=UserProfileImagesRef.child(auth.getCurrentUser().getUid()+".jpeg");
            uploadTask=fileRef.putFile(resultUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUri=task.getResult();
                        url=downloadUri.toString();
                        FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getCurrentUser().getUid()).child("imageurl").setValue(url);
                        pd.dismiss();
                    }else{
                        Toast.makeText(SettingsActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }else{
            Toast.makeText(this, "No Image Selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkForName() {
        FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("name").exists()&&snapshot.child("status").exists()&&snapshot.child("uid").exists()){
                    username.setText(snapshot.child("name").getValue().toString());
                    status.setText(snapshot.child("status").getValue().toString());
                }
                if(snapshot.child("imageurl").exists()){

                    Picasso.get().load(snapshot.child("imageurl").getValue().toString()).into(profileImage);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void InitializeFields() {
        username=findViewById(R.id.set_user_name);
        status=findViewById(R.id.set_status);
        updateStatus=findViewById(R.id.update_button);
        profileImage=findViewById(R.id.set_profile_image);
        toolbar=findViewById(R.id.settings_app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");
    }
}