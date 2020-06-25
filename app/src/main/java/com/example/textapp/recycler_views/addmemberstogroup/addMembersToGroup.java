package com.example.textapp.recycler_views.addmemberstogroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.media.Image;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.textapp.R;
import com.example.textapp.recycler_views.Chat.messageActivity;
import com.example.textapp.recycler_views.USERS.userListAdapter;
import com.example.textapp.recycler_views.USERS.userObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class addMembersToGroup extends AppCompatActivity {

    private RecyclerView userSelected_list;
    private userListAdapter userSelected_list_adapter;
    private RecyclerView.LayoutManager userSelected_list_layout_manager;

    ArrayList<userObject> userList, contactList;
    ArrayList<String> selectedPositions;
    private RelativeLayout ll;
    private TextView user_count,a,b;
    private ImageView i;
    private boolean contactAlreadyExists = false;
    private TextView addMembers;
    private String chatID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_members_to_group);

        Toolbar toolbar = findViewById(R.id.toolbar_addtogroup_activity);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        chatID = getIntent().getExtras().getString("chatID");
        user_count = findViewById(R.id.contact_count_box);
        contactList = new ArrayList<>();
        userList = new ArrayList<>();
        selectedPositions=new ArrayList<>();
        addMembers=findViewById(R.id.create_group_box);
        ll=findViewById(R.id.selected_rel_lay);
        i=findViewById(R.id.cancel_select_box);
        a=findViewById(R.id.selected_size_box);
        b=findViewById(R.id.start_chat_box);
        ll.setVisibility(View.VISIBLE);
        addMembers.setVisibility(View.VISIBLE);
        i.setVisibility(View.GONE);
        a.setVisibility(View.GONE);
        b.setVisibility(View.GONE);
        addMembers.setText("Add Selected Contacts");

        initialize_recycler_view();
        get_contact_list();

        addMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(selectedPositions.size()==0)
                {
                    Toast.makeText(addMembersToGroup.this, "No contact selected", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    for (int i=0;i<selectedPositions.size();i++)
                    {
                        FirebaseDatabase.getInstance().getReference().child("user").child(selectedPositions.get(i)).child("chat").child(chatID).setValue("true");
                        FirebaseDatabase.getInstance().getReference().child("chatDetail").child(chatID).child("user").child(selectedPositions.get(i)).setValue("true");
                    }
                    Toast.makeText(addMembersToGroup.this, String.valueOf(selectedPositions.size())+" new members added", Toast.LENGTH_SHORT).show();
                    selectedPositions.clear();
                    Intent intent =new Intent(addMembersToGroup.this, messageActivity.class);
                    Bundle bundle = new Bundle();

                    bundle.putString("chatID", chatID);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                }

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
            DatabaseReference userSelfConatct= FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("phone");
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
                            userSelected_list_adapter.notifyDataSetChanged();
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
                                userSelected_list_adapter.notifyDataSetChanged();
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

        userSelected_list = findViewById(R.id.addusertogroup_list_view);
        userSelected_list.setNestedScrollingEnabled(false);
        userSelected_list.setHasFixedSize(false);
        userSelected_list_layout_manager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        userSelected_list.setLayoutManager(userSelected_list_layout_manager);
        userSelected_list_adapter = new userListAdapter(userList);
        userSelected_list.setAdapter(userSelected_list_adapter);

        userSelected_list_adapter.setSingleClickItemListener(new userListAdapter.OnUserListSingleItemClickListener() {
            @Override
            public void onUserListSingleItemClick(int position) {

                if (selectedPositions.contains(userList.get(position).getUid())) {
                    View view = userSelected_list.findViewHolderForAdapterPosition(position).itemView;
                    view.setSelected(false);
                    selectedPositions.remove(new String(userList.get(position).getUid()));
                }
                else {

                    View view = userSelected_list.findViewHolderForAdapterPosition(position).itemView;
                    view.setSelected(true);
                    selectedPositions.add(userList.get(position).getUid());
                }
            }

            @Override
            public void onUserListLongSingleItemClick(int position) {

                Toast.makeText(addMembersToGroup.this, "Click to add new members to group", Toast.LENGTH_SHORT).show();
            }
        });



    }


}
