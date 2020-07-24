package com.example.friendschat;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GroupsFragment extends Fragment {

    private View view;
    private ListView listView;
    private List<String> list;
    private ArrayAdapter<String> arrayAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_groups, container, false);

        InitializeFields();

        GetGroupsList();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String groupName=parent.getItemAtPosition(position).toString();
                Intent intent=new Intent(getContext(),GroupChatActivity.class);
                intent.putExtra("groupName",groupName);
                startActivity(intent);
            }
        });

        return view;
    }

    private void GetGroupsList() {

        FirebaseDatabase.getInstance().getReference().child("Groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list.clear();
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    list.add(snapshot.getKey());
                }
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void InitializeFields() {
        listView=view.findViewById(R.id.list_view);
        list=new ArrayList<>();
        arrayAdapter=new ArrayAdapter(getContext(),android.R.layout.simple_list_item_1,list);
        listView.setAdapter(arrayAdapter);

    }
}