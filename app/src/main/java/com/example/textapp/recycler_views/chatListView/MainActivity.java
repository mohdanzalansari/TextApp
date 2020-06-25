package com.example.textapp.recycler_views.chatListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import com.example.textapp.R;
import com.example.textapp.loginFiles.login_phoneno;
import com.example.textapp.personalProfilePage;
import com.example.textapp.recycler_views.USERS.users;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Button logout_btn, search_user;

    private RecyclerView chat_list;
    private RecyclerView.Adapter chat_list_adapter;
    private RecyclerView.LayoutManager chat_list_layout_manager;
    private ImageView infobtn;

    ArrayList<chatObject> chatList;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main_activity);
        setSupportActionBar(toolbar);

        Fresco.initialize(this);

        infobtn = findViewById(R.id.info_btn_box);

        logout_btn = (Button) findViewById(R.id.btn_logout);
        search_user = (Button) findViewById(R.id.user_search_btn);

        infobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, personalProfilePage.class);
                startActivity(intent);
            }
        });

        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, login_phoneno.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        search_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, users.class);

                startActivity(intent);

            }
        });


        chatList = new ArrayList<>();
        getPermission();
        initialize_recycler_view();
        get_user_chatList();
    }


    private void getPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS}, 1);
        }

    }


    private void initialize_recycler_view() {


        chat_list = findViewById(R.id.chatList_recycler_view);
        chat_list.setNestedScrollingEnabled(false);
        chat_list.setHasFixedSize(false);
        chat_list_layout_manager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        chat_list.setLayoutManager(chat_list_layout_manager);
        chat_list_adapter = new chatListAdapter(getApplicationContext(), chatList);
        chat_list.setAdapter(chat_list_adapter);
    }

    private void get_user_chatList() {
        DatabaseReference userchatDB = FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("chat");
        userchatDB.keepSynced(true);

        userchatDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    for (DataSnapshot childsnapshot : dataSnapshot.getChildren()) {
                        chatObject chat = new chatObject(childsnapshot.getKey());
                        boolean exists = false;
                        for (chatObject chatIterator : chatList) {
                            if (chatIterator.getChatid().equals((chat.getChatid()))) {
                                exists = true;
                            }
                        }
                        if (exists) {
                            continue;
                        }
                        chatList.add(chat);
                        chat_list_adapter.notifyDataSetChanged();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
