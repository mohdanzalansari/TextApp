package com.example.textapp.recycler_views.USERS;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.textapp.R;
import com.example.textapp.recycler_views.Chat.messageActivity;
import com.example.textapp.settingUpGroupInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class users extends AppCompatActivity {

    private RecyclerView user_list;
    private userListAdapter user_list_adapter;
    private RecyclerView.LayoutManager user_list_layout_manager;

    ArrayList<userObject> userList, contactList;
    ArrayList<String> groupUserList;
    private Boolean isSelected = false;
    private ArrayList<Integer> selectedPositions;


    private boolean contactAlreadyExists = false;
    private TextView user_count, selected_size, start_chat, create_group;
    private RelativeLayout normal_user_rel_layout, selected_user_rel_layout;
    private ImageView cancel_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        Toolbar toolbar = findViewById(R.id.toolbar_user_activity);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        user_count = findViewById(R.id.contact_count_box);
        selected_size = findViewById(R.id.selected_size_box);
        start_chat = findViewById(R.id.start_chat_box);
        create_group = findViewById(R.id.create_group_box);
        cancel_btn = findViewById(R.id.cancel_select_box);
        normal_user_rel_layout = findViewById(R.id.normal_user_rel_lay);
        selected_user_rel_layout = findViewById(R.id.selected_rel_lay);


        contactList = new ArrayList<>();
        selectedPositions = new ArrayList<>();
        userList = new ArrayList<>();
        groupUserList = new ArrayList<>();

        initialize_recycler_view();
        get_contact_list();


        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                for (int i = 0; i < selectedPositions.size(); i++) {
                    View v = user_list.findViewHolderForAdapterPosition(selectedPositions.get(i)).itemView;
                    v.setSelected(false);
                }
                selectedPositions = new ArrayList<>();

                selected_user_rel_layout.setVisibility(View.GONE);
                normal_user_rel_layout.setVisibility(View.VISIBLE);

            }
        });

        start_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                normal_user_rel_layout.setVisibility(View.VISIBLE);
                selected_user_rel_layout.setVisibility(View.GONE);
                View v = user_list.findViewHolderForAdapterPosition(selectedPositions.get(0)).itemView;
                v.setSelected(false);
                checkUserandGo(selectedPositions.get(0));
            }
        });


        create_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                for (int i = 0; i < selectedPositions.size(); i++) {
                    groupUserList.add(userList.get(i).getUid());
                }

                Intent intent = new Intent(getApplicationContext(), settingUpGroupInfo.class);
                Bundle bundle = new Bundle();

                bundle.putStringArrayList("groupUserUIDs", groupUserList);
                intent.putExtras(bundle);
                startActivity(intent);


                for (int i = 0; i < selectedPositions.size(); i++) {
                    View v = user_list.findViewHolderForAdapterPosition(selectedPositions.get(i)).itemView;
                    v.setSelected(false);
                }

                selectedPositions = new ArrayList<>();

                selected_user_rel_layout.setVisibility(View.GONE);
                normal_user_rel_layout.setVisibility(View.VISIBLE);

            }
        });

    }


    private void get_contact_list() {
        Cursor ph = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        while (ph.moveToNext()) {
            final String name = ph.getString(ph.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String number = ph.getString(ph.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            number = number.replace(" ", "");
            number = number.replace("-", "");
            number = number.replace("(", "");
            number = number.replace(")", "");

            if (!String.valueOf(number.charAt(0)).equals("+")) {
                number = "+91" + number;
            }

            final String nnum=number;
            DatabaseReference userSelfConatct=FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("phone");
            userSelfConatct.keepSynced(true);
            userSelfConatct.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists())
                    {
                        if (!dataSnapshot.getValue().toString().equals(nnum))
                        {
                            userObject contact = new userObject("", name, nnum);
                            contactList.add(contact);
                            get_user_detail(contact);

                        }


                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });




        }

    }

    private void get_user_detail(userObject contact) {

        DatabaseReference userDB = FirebaseDatabase.getInstance().getReference().child("user");
        userDB.keepSynced(true);
        Query query = userDB.orderByChild("phone").equalTo(contact.getPhnum());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String phone = "", name = "";

                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        if (childSnapshot.child("phone").getValue() != null) {
                            phone = childSnapshot.child("phone").getValue().toString();
                        }
                        if (childSnapshot.child("name").getValue() != null) {
                            name = childSnapshot.child("name").getValue().toString();
                        }

                        userObject user = new userObject(childSnapshot.getKey(), name, phone);
                        if (name.equals(phone)) {
                            for (userObject contactIterator : contactList) {
                                if (contactIterator.getPhnum().equals(user.getPhnum())) {
                                    user.setName(contactIterator.getName());

                                }
                            }
                        }
                        if (userList.size() == 0) {
                            userList.add(user);
                            user_count.setText(String.valueOf(userList.size() + " contacts"));
                            user_list_adapter.notifyDataSetChanged();
                        } else {
                            for (userObject contactIterator : userList) {
                                if (contactIterator.getPhnum().equals(user.getPhnum())) {

                                    contactAlreadyExists = true;
                                    break;
                                }
                            }
                            if (contactAlreadyExists == false) {
                                userList.add(user);
                                user_count.setText(String.valueOf(userList.size() + " contacts"));
                                user_list_adapter.notifyDataSetChanged();
                            } else {
                                contactAlreadyExists = false;
                            }

                        }

                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void initialize_recycler_view() {

        user_list = findViewById(R.id.user_list_view);
        user_list.setNestedScrollingEnabled(false);
        user_list.setHasFixedSize(false);
        user_list_layout_manager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        user_list.setLayoutManager(user_list_layout_manager);
        user_list_adapter = new userListAdapter(userList);
        user_list.setAdapter(user_list_adapter);

        user_list_adapter.setSingleClickItemListener(new userListAdapter.OnUserListSingleItemClickListener() {
            @Override
            public void onUserListSingleItemClick(int position) {


                if (isSelected == false) {


                    checkUserandGo(position);



                } else {

                    if (selectedPositions.contains(position)) {
                        View view = user_list.findViewHolderForAdapterPosition(position).itemView;
                        view.setSelected(false);
                        selectedPositions.remove(new Integer(position));
                        selected_size.setText(String.valueOf(selectedPositions.size()));

                        if (selectedPositions.size() == 1) {
                            start_chat.setVisibility(View.VISIBLE);
                            create_group.setVisibility(View.GONE);

                        } else if (selectedPositions.size() == 0) {
                            selected_user_rel_layout.setVisibility(View.GONE);
                            normal_user_rel_layout.setVisibility(View.VISIBLE);
                        }

                    } else {
                        View view = user_list.findViewHolderForAdapterPosition(position).itemView;
                        view.setSelected(true);
                        selectedPositions.add(position);
                        selected_size.setText(String.valueOf(selectedPositions.size()));
                        start_chat.setVisibility(View.GONE);
                        create_group.setVisibility(View.VISIBLE);
                    }

                }
            }

            @Override
            public void onUserListLongSingleItemClick(int position) {

                isSelected = true;

                View view = user_list.findViewHolderForAdapterPosition(position).itemView;
                view.setSelected(true);
                selectedPositions.add(position);
                normal_user_rel_layout.setVisibility(View.GONE);
                selected_user_rel_layout.setVisibility(View.VISIBLE);
                start_chat.setVisibility(View.VISIBLE);
                create_group.setVisibility(View.GONE);
                selected_size.setText(String.valueOf(selectedPositions.size()));

            }
        });

    }

    private void checkUserandGo(final int position) {

        final DatabaseReference dbref = FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("createdFor");
        dbref.keepSynced(true);

        Query query = dbref.orderByChild("phno").equalTo(userList.get(position).getPhnum());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    String xchildValue = "";
                    for (DataSnapshot childsnapshot : dataSnapshot.getChildren()) {


                        if (childsnapshot.getValue().toString().equals("{phno=" + userList.get(position).getPhnum() + "}")) {
                            xchildValue = childsnapshot.getKey();
                            break;
                        }
                    }

                    Intent intent = new Intent(getApplicationContext(), messageActivity.class);
                    Bundle bundle = new Bundle();

                    bundle.putString("chatID", xchildValue);
                    intent.putExtras(bundle);
                    startActivity(intent);


                } else {
                    final String key = FirebaseDatabase.getInstance().getReference().child("chat").push().getKey();

                    FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("chat").child(key).setValue("true");
                    FirebaseDatabase.getInstance().getReference().child("user").child(userList.get(position).getUid()).child("chat").child(key).setValue("true");

                    FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("createdFor").child(key).child("phno").setValue(userList.get(position).getPhnum());

                    DatabaseReference mynum=FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("phone");
                    mynum.keepSynced(true);

                    mynum.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists())
                            {
                                FirebaseDatabase.getInstance().getReference().child("user").child(userList.get(position).getUid()).child("createdFor").child(key).child("phno").setValue(dataSnapshot.getValue());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });



                    FirebaseDatabase.getInstance().getReference().child("chatDetail").child(key).child("user").child(FirebaseAuth.getInstance().getUid()).setValue("true");
                    FirebaseDatabase.getInstance().getReference().child("chatDetail").child(key).child("user").child(userList.get(position).getUid()).setValue("true");

                    Intent intent = new Intent(getApplicationContext(), messageActivity.class);
                    Bundle bundle = new Bundle();

                    bundle.putString("chatID", key);
                    intent.putExtras(bundle);
                    startActivity(intent);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
