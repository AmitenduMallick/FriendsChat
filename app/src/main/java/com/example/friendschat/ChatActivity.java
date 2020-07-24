package com.example.friendschat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverid;
    private String messageReceivername;
    private String messageRecieverImage;
    private TextView username,userLastseen;
    private CircleImageView userImage;
    private Toolbar toolbar;
    private ImageButton sendMessageButton,sendFilesButton;
    private EditText inputMessage;
    private FirebaseAuth mAuth;
    private String messageSenderId;
    private DatabaseReference RootRef;
    public List<Messages> messagesList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    MessagesAdapter messagesAdapter;
    private RecyclerView userMessagesList;
    String currentSavedTime;
    String currentSavedDate;
    private String checker="",myUrl="";
    private StorageTask uploadTask;
    private Uri fileuri;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        messageReceiverid=getIntent().getStringExtra("visit_user_id");
        messageReceivername=getIntent().getStringExtra("visit_username");
        messageRecieverImage=getIntent().getStringExtra("visit_user_image");
        mAuth=FirebaseAuth.getInstance();
        messageSenderId=mAuth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();
        pd=new ProgressDialog(this);


        InitializeControllers();

        username.setText(messageReceivername);
        Picasso.get().load(messageRecieverImage).placeholder(R.drawable.profile_image).into(userImage);


        RootRef.child("Messages").child(messageSenderId).child(messageReceiverid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {


                Messages messages=snapshot.getValue(Messages.class);
                messagesList.add(messages);
                messagesAdapter.notifyDataSetChanged();

                userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {



            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {





            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                SendMessage();

            }
        });
        displayLastSeen();

        sendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[]=new CharSequence[]
                        {
                          "Images","PDF Files","Word Document Files"
                        };
                AlertDialog.Builder builder=new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select File");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {

                        if(i==0){
                            checker="image";
                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent,"Select Image"),91);
                        }
                        if(i==1){
                            checker="pdf";

                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent,"Select PDF"),91);

                        }
                        if(i==2){

                            checker="docx";
                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent,"Select Document"),91);

                        }

                    }
                });
                builder.show();
            }
        });




    }



    private void SendMessage() {

        String message=inputMessage.getText().toString();
        if(TextUtils.isEmpty(message)){
            Toast.makeText(this, "First write a message", Toast.LENGTH_SHORT).show();
        }
        else{
            String messageSenderRef="Messages/"+ messageSenderId+"/"+messageReceiverid;
            String messageReceiverRef="Messages/"+ messageReceiverid+"/"+messageSenderId;

            DatabaseReference userMessageKey=RootRef.child("Messages").child(messageSenderId).child(messageReceiverid)
                    .push();
            String messagePushid=userMessageKey.getKey();

            Map messageTextBody=new HashMap();
            messageTextBody.put("message",message);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderId);
            messageTextBody.put("to",messageReceiverid);
            messageTextBody.put("messageid",messagePushid);
            messageTextBody.put("date",currentSavedDate);
            messageTextBody.put("time",currentSavedTime);
            Map messageBodyDetails=new HashMap();
            messageBodyDetails.put(messageSenderRef+"/"+messagePushid,messageTextBody);
            messageBodyDetails.put(messageReceiverRef+"/"+messagePushid,messageTextBody);
            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                    }else{
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    inputMessage.setText("");
                }
            });
        }

    }

    private void InitializeControllers() {

        toolbar=findViewById(R.id.chat_bar_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater=(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView=layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);
        userImage=findViewById(R.id.custom_profile_image);
        username=findViewById(R.id.custom_profile_name);
        userLastseen=findViewById(R.id.custom_lastseen);
        inputMessage=findViewById(R.id.input_message);
        sendMessageButton=findViewById(R.id.send_message_button);
        sendFilesButton=findViewById(R.id.send_files_button);
        userMessagesList=findViewById(R.id.private_messages_list_of_users);


        linearLayoutManager=new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        messagesAdapter=new MessagesAdapter(messagesList);
        userMessagesList.setAdapter(messagesAdapter);

        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd, yyyy");
        currentSavedDate=currentDate.format(calendar.getTime());
        SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");
        currentSavedTime=currentTime.format(calendar.getTime());

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==91&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null){


            pd.setMessage("Please Wait");
            pd.show();



            fileuri=data.getData();
            if(!checker.equals("image")){

                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Document Files");
                final String messageSenderRef="Messages/"+ messageSenderId+"/"+messageReceiverid;
                final String messageReceiverRef="Messages/"+ messageReceiverid+"/"+messageSenderId;

                DatabaseReference userMessageKey=RootRef.child("Messages").child(messageSenderId).child(messageReceiverid)
                        .push();
                final String messagePushid=userMessageKey.getKey();
                final StorageReference filePath=storageReference.child(messagePushid+"."+checker);
                filePath.putFile(fileuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String downloadUrl = uri.toString();

                                Map messageImageBody = new HashMap();
                                messageImageBody.put("message",downloadUrl);
                                messageImageBody.put("name",fileuri.getLastPathSegment());
                                messageImageBody.put("type",checker);
                                messageImageBody.put("from",messageSenderId);
                                messageImageBody.put("to", messageReceiverid);
                                messageImageBody.put("messageID", messagePushid);
                                messageImageBody.put("time", currentSavedTime);
                                messageImageBody.put("date", currentSavedDate);


                                Map messageBodyDetail = new HashMap();
                                messageBodyDetail.put(messageSenderRef+ "/" + messagePushid, messageImageBody);
                                messageBodyDetail.put(messageReceiverRef+ "/" + messagePushid, messageImageBody);

                                RootRef.updateChildren(messageBodyDetail);
                                pd.dismiss();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pd.dismiss();
                                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double p = (100.0* taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        pd.setMessage((int) p + " % Uploading...");
                    }
                });


            }else if(checker.equals("image")){

                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Image Files");
                final String messageSenderRef="Messages/"+ messageSenderId+"/"+messageReceiverid;
                final String messageReceiverRef="Messages/"+ messageReceiverid+"/"+messageSenderId;

                DatabaseReference userMessageKey=RootRef.child("Messages").child(messageSenderId).child(messageReceiverid)
                        .push();
                final String messagePushid=userMessageKey.getKey();
                 final StorageReference filePath=storageReference.child(messagePushid+"."+".jpg");
                uploadTask=filePath.putFile(fileuri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {

                        if(!task.isSuccessful()){
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            Uri downloadUri=task.getResult();
                            myUrl=downloadUri.toString();
                            Map messageTextBody=new HashMap();
                            messageTextBody.put("message",myUrl);
                            messageTextBody.put("name",fileuri.getLastPathSegment());
                            messageTextBody.put("type",checker);
                            messageTextBody.put("from",messageSenderId);
                            messageTextBody.put("to",messageReceiverid);
                            messageTextBody.put("messageid",messagePushid);
                            messageTextBody.put("date",currentSavedDate);
                            messageTextBody.put("time",currentSavedTime);
                            Map messageBodyDetails=new HashMap();
                            messageBodyDetails.put(messageSenderRef+"/"+messagePushid,messageTextBody);
                            messageBodyDetails.put(messageReceiverRef+"/"+messagePushid,messageTextBody);
                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful()){
                                        pd.dismiss();
                                    }else{
                                        pd.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    inputMessage.setText("");
                                }
                            });
                        }
                    }
                });

            }else{
                pd.dismiss();
                Toast.makeText(this, "Nothing Selected, Error", Toast.LENGTH_SHORT).show();
            }

        }


    }

    private void displayLastSeen(){
        RootRef.child("Users").child(messageReceiverid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.child("userState").hasChild("state")){
                    String date=snapshot.child("userState").child("date").getValue().toString();
                    String time =snapshot.child("userState").child("time").getValue().toString();
                    String state=snapshot.child("userState").child("state").getValue().toString();

                    if(state.equals("online")){
                        userLastseen.setText("online");
                    }else if(state.equals("offline")){
                        userLastseen.setText("Last seen: " + "\n" + date +" "+ time);
                    }
                }else{
                    userLastseen.setText("offline");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}