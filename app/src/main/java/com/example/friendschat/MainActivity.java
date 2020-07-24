package com.example.friendschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Toolbar mtoolbar;
    private ViewPager myViewPager;
    private TabLayout tabLayout;
    private TabsAccessorAdapter tabsAccessorAdapter;
    private FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
    FirebaseAuth auth;
    DatabaseReference rootRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        auth=FirebaseAuth.getInstance();
        rootRef= FirebaseDatabase.getInstance().getReference();
        mtoolbar=findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("FriendsChat");
        myViewPager=findViewById(R.id.main_tabs_pager);
        tabsAccessorAdapter=new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(tabsAccessorAdapter);
        tabLayout=findViewById(R.id.maintabs);
        tabLayout.setupWithViewPager(myViewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser firebaseUser=auth.getCurrentUser();

        if(firebaseUser==null){
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
            finish();
        }else{

            updateUserStatus("online");
            VerifyExistence();
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser firebaseUser=auth.getCurrentUser();
        if(firebaseUser!=null){
            updateUserStatus("offline");
        }

    }

    private void VerifyExistence() {

        String userId=auth.getCurrentUser().getUid();
        rootRef.child("Users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("name").exists()){
                    //Toast.makeText(MainActivity.this, "Welcome back!!", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MainActivity.this, "Please update your username", Toast.LENGTH_SHORT).show();
                    Intent intent =new Intent(getApplicationContext(),SettingsActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    //finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()){
            case R.id.settings_menu:
                startActivity(new Intent(getApplicationContext(),SettingsActivity.class));
                break;
            case R.id.find_friends_menu:

                startActivity(new Intent(getApplicationContext(),FindFriendsActivity.class));
                break;
            case R.id.logout_menu:
                updateUserStatus("offline");
                auth.signOut();
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                finish();
                break;
            case R.id.create_groups_menu:
                RequestNewGroup();
                break;

        }
        return true;
    }

    private void RequestNewGroup() {

        AlertDialog.Builder builder=new AlertDialog.Builder(this,R.style.AlertDialog);
        builder.setTitle("Enter the Group Name");
        final EditText groupName=new EditText(MainActivity.this);
        groupName.setHint("e.g Friends Forever");
        builder.setView(groupName);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String name=groupName.getText().toString();
                if(TextUtils.isEmpty(name)){
                    Toast.makeText(MainActivity.this, "Please enter a valid group name!", Toast.LENGTH_SHORT).show();
                }
                else{

                    createGroup(name);

                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void createGroup(final String name) {

        FirebaseDatabase.getInstance().getReference().child("Groups").child(name).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this, name+" group created sucessfully!!", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    public void updateUserStatus(String state){
        String currentSavedTime;
        String currentSavedDate;
        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd, yyyy");
        currentSavedDate=currentDate.format(calendar.getTime());
        SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");
        currentSavedTime=currentTime.format(calendar.getTime());

        HashMap<String,Object> map=new HashMap<>();
        map.put("time",currentSavedTime);
        map.put("date",currentSavedDate);
        map.put("state",state);

        FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid()).child("userState").updateChildren(map);


    }
}