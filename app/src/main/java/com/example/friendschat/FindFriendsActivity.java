package com.example.friendschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView findFriendsRecyclerList;
    private EditText search;
    FirebaseRecyclerOptions<Contacts> options;
    //FirebaseRecyclerAdapter<Contacts,FindFriendsViewHolder> adapter;
    List<Contacts> mContacts;
    ContactsAdapter contactsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        findFriendsRecyclerList=findViewById(R.id.find_friends_recycler_view);
        findFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));
        toolbar=findViewById(R.id.find_friends_bar);
        search=findViewById(R.id.find_friends_search);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Find Friends");
        mContacts=new ArrayList<>();
        contactsAdapter=new ContactsAdapter(mContacts,getApplicationContext());
        findFriendsRecyclerList.setAdapter(contactsAdapter);
        readUsers();
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                getSearch(s.toString());




            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void readUsers() {

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(TextUtils.isEmpty(search.getText().toString())){
                    mContacts.clear();
                    for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                         Contacts contacts=snapshot.getValue(Contacts.class);
                         mContacts.add(contacts);
                    }
                    contactsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void getSearch(String s) {

        Query query=FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("name").startAt(s).endAt(s+"\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mContacts.clear();
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    Contacts contacts=snapshot.getValue(Contacts.class);
                    mContacts.add(contacts);


                }
                contactsAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




    }


   /* @Override
    protected void onStart() {
        super.onStart();
        options=new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(FirebaseDatabase.getInstance().getReference().child("Users"),Contacts.class)
                .build();

        adapter=new FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, final int position, @NonNull Contacts model) {

                holder.userProfileName.setText(model.getName());
                holder.userProfileStatus.setText(model.getStatus());
                Picasso.get().load(model.getImageurl()).placeholder(R.drawable.profile_image).into(holder.userProfileImage);


                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visited_ID=getRef(position).getKey();
                        Intent intent=new Intent(getApplicationContext(),ViewProfileActivity.class);
                        intent.putExtra("visited_ID",visited_ID);
                        startActivity(intent);
                    }
                });


            }

            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                FindFriendsViewHolder findFriendsViewHolder=new FindFriendsViewHolder(view);
                return findFriendsViewHolder;
            }
        };
        findFriendsRecyclerList.setAdapter(adapter);
        adapter.startListening();
    }*/

    /*public static class FindFriendsViewHolder extends RecyclerView.ViewHolder{

        TextView userProfileName;
        TextView userProfileStatus;
        CircleImageView userProfileImage;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            userProfileName=itemView.findViewById(R.id.user_profile_name);
            userProfileStatus=itemView.findViewById(R.id.user_profile_status);
            userProfileImage=itemView.findViewById(R.id.users_profile_image);
        }
    }*/
}