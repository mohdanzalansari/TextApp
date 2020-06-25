package com.example.textapp.recycler_views.Chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;

import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.capybaralabs.swipetoreply.ISwipeControllerActions;
import com.capybaralabs.swipetoreply.SwipeController;
import com.example.textapp.R;
import com.example.textapp.imageViewer;
import com.example.textapp.personalProfilePage;
import com.example.textapp.profilePage;
import com.example.textapp.recycler_views.Chat.ImagesMessage.ImagesMessages;
import com.example.textapp.recycler_views.chatListView.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class messageActivity extends AppCompatActivity {

    private RecyclerView message_list;
    private messageListAdapter message_list_adapter;
    private RecyclerView.LayoutManager message_list_layout_manager;

    private boolean isfor = false;
    private int forwarded_posititon;

    private ArrayList<messageObject> messageList;
    private String messageActivityTitle = "-1", messageActivityprofileImage = "-1";
    private Button sendbtn, send_img_btn;
    private int Pick_image_intent = 1;
    private String chatID;
    private ArrayList<String> mediaURIlist;
    private EditText msg;
    private TextView forwarded_name, forwarded_msg, selected_count, userProfileName;
    private ImageView cancelBtn, userProfileImage, groupexitImage;
    private Boolean isGroup = false;
    private ArrayList<messageObject> SelectedObject;
    private ArrayList<Integer> selectedPositions;
    private RelativeLayout profile_relative_layout, option_relative_layout;

    ImageView deselect_selected, reply_selected, delete_selected, forward_selected;

    private Boolean isSelected = false;
    private Boolean nameSet = false;

    DatabaseReference messageReference;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_message_activity);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        profile_relative_layout = findViewById(R.id.profile_rel_lay);
        option_relative_layout = findViewById(R.id.option_rel_lay);

        userProfileImage = findViewById(R.id.profile_image);
        userProfileName = findViewById(R.id.profile_name);


        chatID = getIntent().getExtras().getString("chatID");
        setImgaeAndName();


        selected_count = findViewById(R.id.select_count_box);
        deselect_selected = findViewById(R.id.deselect_selected_btn_img);
        delete_selected = findViewById(R.id.del_selected_btn_img);
        reply_selected = findViewById(R.id.reply_button_img);
        forward_selected = findViewById(R.id.fowr_selected_btn_img);


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        messageList = new ArrayList<>();
        selectedPositions = new ArrayList<>();


        messageReference = FirebaseDatabase.getInstance().getReference().child("chat").child(chatID);
        forwarded_msg = findViewById(R.id.forwarded_msg_name_box);
        forwarded_name = findViewById(R.id.forwarded_name_box);
        msg = findViewById(R.id.msgBoxxx);
        sendbtn = findViewById(R.id.send);
        send_img_btn = findViewById(R.id.send_media);
        cancelBtn = findViewById(R.id.cancel_forwded);
        groupexitImage = findViewById(R.id.groupexitbuttonBox);


        userProfileName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), profilePage.class);

                Bundle bundle = new Bundle();

                bundle.putString("chatID", chatID);
                bundle.putBoolean("isGroup", isGroup);
                bundle.putString("username", messageActivityTitle);
                bundle.putString("userProfileImage", messageActivityprofileImage);
                intent.putExtras(bundle);
                startActivity(intent);


            }
        });

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!messageActivityprofileImage.equals("-1")) {
                    Intent intent = new Intent(messageActivity.this, imageViewer.class);
                    intent.putExtra("imageURI", messageActivityprofileImage);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "No profile Picture", Toast.LENGTH_SHORT).show();
                }

            }
        });

        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isfor == false) {
                    sendMessage(String.valueOf(-1));
                } else {
                    sendMessage(String.valueOf(forwarded_posititon));
                    isfor = false;
                    forwarded_posititon = -1;
                }

            }
        });

        send_img_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selected_messages_layout_setTO_Gone();


            }
        });

        deselect_selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                for (int i = 0; i < selectedPositions.size(); i++) {
                    View v = message_list.findViewHolderForAdapterPosition(selectedPositions.get(i)).itemView;
                    v.setSelected(false);
                }
                selectedPositions = new ArrayList<>();

                option_relative_layout.setVisibility(View.GONE);
                profile_relative_layout.setVisibility(View.VISIBLE);



            }
        });

        delete_selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (selectedPositions.size() > 1) {

                    Toast.makeText(getApplicationContext(), "Only one message can be deleted at a time", Toast.LENGTH_SHORT).show();
                } else {
                    messageReference.child(messageList.get(selectedPositions.get(0)).getMessageId()).child("isDeleted").setValue("true");

                    View v = message_list.findViewHolderForAdapterPosition(selectedPositions.get(0)).itemView;
                    v.setSelected(false);
                    selectedPositions = new ArrayList<>();
                    option_relative_layout.setVisibility(View.GONE);
                    profile_relative_layout.setVisibility(View.VISIBLE);
                }


            }
        });

        reply_selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                reply_to_msg(selectedPositions.get(0));

            }
        });

        forward_selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                String mesg=messageList.get(selectedPositions.get(0)).getMessage();

                ClipData clipData = ClipData.newPlainText("text", mesg);
                manager.setPrimaryClip(clipData);

                View viewx = message_list.findViewHolderForAdapterPosition(selectedPositions.get(0)).itemView;
                viewx.setSelected(false);
                Toast.makeText(messageActivity.this, "Text Copied", Toast.LENGTH_SHORT).show();





            }
        });

        groupexitImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final DatabaseReference usergroup = FirebaseDatabase.getInstance().getReference().child("chatDetail").child(chatID).child("user");
                usergroup.keepSynced(true);
                usergroup.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            usergroup.child(FirebaseAuth.getInstance().getUid()).removeValue();

                            final DatabaseReference usergroupinfo = FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("chat");
                            usergroupinfo.keepSynced(true);
                            usergroupinfo.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        usergroupinfo.child(chatID).removeValue();
                                        Intent intent = new Intent(messageActivity.this, MainActivity.class);
                                        Toast.makeText(getApplicationContext(), "Group Left", Toast.LENGTH_SHORT).show();
                                        startActivity(intent);
                                        finish();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }
        });


        getChatMessages();
        initialize_recycler_view();
    }

    private void setImgaeAndName() {


        DatabaseReference userChildCount = FirebaseDatabase.getInstance().getReference().child("chatDetail").child(chatID).child("groupName");
        userChildCount.keepSynced(true);

        userChildCount.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {


                    userProfileName.setText(dataSnapshot.getValue().toString());
                    messageActivityTitle = dataSnapshot.getValue().toString();
                    groupexitImage.setVisibility(View.VISIBLE);
                    isGroup = true;

                    DatabaseReference userGroupprofilepic = FirebaseDatabase.getInstance().getReference().child("chatDetail").child(chatID).child("groupProfilePicture");
                    userGroupprofilepic.keepSynced(true);

                    userGroupprofilepic.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                if (!dataSnapshot.getValue().toString().equals("-1")) {
                                    Glide.with(getApplicationContext()).load(Uri.parse(dataSnapshot.getValue().toString())).into(userProfileImage);
                                    messageActivityprofileImage = dataSnapshot.getValue().toString();
                                } else {
                                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_group_image);
                                    userProfileImage.setImageBitmap(bitmap);
                                    messageActivityprofileImage = "-1";
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                    isGroup = true;


                } else {
                    final DatabaseReference userNameValue = FirebaseDatabase.getInstance().getReference().child("chatDetail").child(chatID).child("user");
                    userNameValue.keepSynced(true);


                    userNameValue.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot childSnapShot : dataSnapshot.getChildren()) {
                                    if (!childSnapShot.getKey().equals(FirebaseAuth.getInstance().getUid())) {
                                        final String userUID = childSnapShot.getKey();


                                        DatabaseReference userNumber = FirebaseDatabase.getInstance().getReference().child("user").child(userUID).child("phone");

                                        userNumber.keepSynced(true);


                                        userNumber.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    String number = dataSnapshot.getValue().toString();
                                                    groupexitImage.setVisibility(View.GONE);
                                                    Cursor ph = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
                                                    while (ph.moveToNext()) {
                                                        String contactName = ph.getString(ph.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                                                        String contactNumber = ph.getString(ph.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                                                        contactNumber = contactNumber.replace(" ", "");
                                                        contactNumber = contactNumber.replace("-", "");
                                                        contactNumber = contactNumber.replace("(", "");
                                                        contactNumber = contactNumber.replace(")", "");

                                                        if (!String.valueOf(contactNumber.charAt(0)).equals("+")) {
                                                            contactNumber = "+91" + contactNumber;
                                                        }

                                                        if (contactNumber.equals(number)) {
                                                            messageActivityTitle = contactName;
                                                            userProfileName.setText(messageActivityTitle);
                                                            isGroup = false;
                                                            nameSet = true;
                                                        }
                                                    }
                                                    if (nameSet == false) {
                                                        messageActivityTitle = number;
                                                        userProfileName.setText(messageActivityTitle);
                                                        isGroup = false;

                                                    }


                                                    DatabaseReference userprofilepic = FirebaseDatabase.getInstance().getReference().child("user").child(userUID).child("profilePicture");
                                                    userprofilepic.keepSynced(true);

                                                    userprofilepic.addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            if (dataSnapshot.exists()) {
                                                                if (!dataSnapshot.getValue().toString().equals("-1")) {
                                                                    Glide.with(getApplicationContext()).load(Uri.parse(dataSnapshot.getValue().toString())).into(userProfileImage);
                                                                    messageActivityprofileImage = dataSnapshot.getValue().toString();
                                                                } else {
                                                                    messageActivityprofileImage = "-1";
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });


                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });


                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    private void getChatMessages() {

        messageReference.keepSynced(true);

        messageReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if (dataSnapshot.exists()) {
                    String textt = "", creatoridd = "", forwarded_position = "", isDeleted = "",time="";
                    ArrayList<String> mediaURLlist = new ArrayList<>();

                    if (dataSnapshot.child("text").getValue() != null) {
                        textt = dataSnapshot.child("text").getValue().toString();
                    }
                    if (dataSnapshot.child("creator").getValue() != null) {
                        creatoridd = dataSnapshot.child("creator").getValue().toString();
                    }
                    if (dataSnapshot.child("media").getChildrenCount() > 0) {
                        for (DataSnapshot mediasnapshot : dataSnapshot.child("media").getChildren()) {
                            mediaURLlist.add(mediasnapshot.getValue().toString());
                        }
                    }
                    if(dataSnapshot.child("time").getValue()!=null)
                    {
                        time=dataSnapshot.child("time").getValue().toString();
                    }

                    if (dataSnapshot.child("isDeleted").getValue() != null) {
                        if (dataSnapshot.child("isDeleted").getValue().equals("true")) {
                            textt = "This message has been deleted...";
                            isDeleted = "true";
                            forwarded_position = "-1";
                        } else {
                            isDeleted = "false";
                            if (dataSnapshot.child("isForwarded").getValue() != null) {
                                forwarded_position = dataSnapshot.child("isForwarded").getValue().toString();
                            }
                        }

                    }


                    messageObject newwMessage = new messageObject(textt, dataSnapshot.getKey(), creatoridd, forwarded_position, mediaURLlist, isDeleted,time);

                    messageList.add(newwMessage);
                    message_list_layout_manager.scrollToPosition(messageList.size() - 1);
                    message_list_adapter.notifyDataSetChanged();
                }


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


                for (int i = 0; i < messageList.size(); i++) {
                    if (messageList.get(i).getMessageId().equals(dataSnapshot.getKey())) {


                        String creatoridd = "", forwarded_position = "", isDeleted = "",time="";
                        ArrayList<String> mediaURLlist = new ArrayList<>();

                        if (dataSnapshot.child("creator").getValue() != null) {
                            creatoridd = dataSnapshot.child("creator").getValue().toString();
                        }
                        if (dataSnapshot.child("media").getChildrenCount() > 0) {
                            for (DataSnapshot mediasnapshot : dataSnapshot.child("media").getChildren()) {
                                mediaURLlist.add(mediasnapshot.getValue().toString());
                            }
                        }


                        if(dataSnapshot.child("time").getValue().toString()!=null)
                        {
                            time=dataSnapshot.child("time").getValue().toString();
                        }


                        isDeleted = "true";
                        if (dataSnapshot.child("isForwarded").getValue() != null) {
                            forwarded_position = "-1";

                        }


                        messageList.set(i, new messageObject("This message has been deleted...", dataSnapshot.getKey(), creatoridd, forwarded_position, mediaURLlist, isDeleted,time));

                        message_list_adapter.notifyDataSetChanged();

                        break;
                    }


                }


            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void sendMessage(String forwarded_position) {


        if (!msg.getText().toString().isEmpty()) {
            DatabaseReference newMesg = messageReference.push();

            Map newMsgMap = new HashMap();

            newMsgMap.put("text", msg.getText().toString());
            newMsgMap.put("isForwarded", forwarded_position);
            newMsgMap.put("creator", FirebaseAuth.getInstance().getUid());
            newMsgMap.put("isDeleted", "false");
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
            String datetime = sdf.format(cal.getTime());
            newMsgMap.put("time", datetime);

            newMesg.updateChildren(newMsgMap);

            DatabaseReference lastmessage = FirebaseDatabase.getInstance().getReference().child("chatDetail").child(chatID).child("lastMessage");
            lastmessage.keepSynced(true);
            lastmessage.setValue(msg.getText().toString());
        }
        selected_messages_layout_setTO_Gone();
        msg.setText(null);


    }


    private void openGallery() {


        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture(s)"), Pick_image_intent);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mediaURIlist = new ArrayList<>();
        if (resultCode == RESULT_OK) {
            if (requestCode == Pick_image_intent) {
                if (data.getClipData() == null) {
                    mediaURIlist.add(data.getData().toString());
                } else {
                    for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                        mediaURIlist.add(data.getClipData().getItemAt(i).getUri().toString());
                    }
                }

            }
        }

        if (mediaURIlist.size() > 0) {
            Intent intent = new Intent(messageActivity.this, ImagesMessages.class);
            intent.putExtra("ImageURIs", mediaURIlist);
            intent.putExtra("chatId", chatID);
            startActivity(intent);
        }
    }

    private void initialize_recycler_view() {


        message_list = findViewById(R.id.message_recycler_view);
        message_list.setNestedScrollingEnabled(false);
        message_list.setHasFixedSize(false);
        message_list_layout_manager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        message_list.setLayoutManager(message_list_layout_manager);
        message_list_adapter = new messageListAdapter(messageList, chatID, getApplicationContext());
        message_list.setAdapter(message_list_adapter);


        message_list_adapter.setSingletOnItemClickListener(new messageListAdapter.OnSingleItemClickListener() {


            @Override

            public void onSingleItemClick(final int position, String user) {


                if (isSelected == false) {

                    if (!messageList.get(position).isForwarded.equals("-1")) {
                        message_list_layout_manager.scrollToPosition(Integer.parseInt(messageList.get(position).isForwarded));

                        message_list.postDelayed(new Runnable() {
                            public void run() {
                                View view = message_list.findViewHolderForAdapterPosition(Integer.parseInt(messageList.get(position).isForwarded)).itemView;
                                view.setSelected(true);
                            }
                        }, 50);

                        message_list.postDelayed(new Runnable() {
                            public void run() {
                                View view = message_list.findViewHolderForAdapterPosition(Integer.parseInt(messageList.get(position).isForwarded)).itemView;
                                view.setSelected(false);
                            }
                        }, 700);


                    }
                } else {

                    if (!messageList.get(position).getIsDeleted().equals("true")) {

                        if (selectedPositions.contains(position)) {
                            View view = message_list.findViewHolderForAdapterPosition(position).itemView;
                            view.setSelected(false);


                            selectedPositions.remove(new Integer(position));
                            selected_count.setText(String.valueOf(selectedPositions.size()));
                            if (selectedPositions.size() == 1) {
                                reply_selected.setVisibility(View.VISIBLE);
                                forward_selected.setVisibility(View.VISIBLE);
                            } else if (selectedPositions.size() == 0) {
                                profile_relative_layout.setVisibility(View.VISIBLE);
                                option_relative_layout.setVisibility(View.GONE);
                            }


                        } else {

                            View view = message_list.findViewHolderForAdapterPosition(position).itemView;
                            view.setSelected(true);
                            selectedPositions.add(position);
                            selected_count.setText(String.valueOf(selectedPositions.size()));

                            if (selectedPositions.size() > 1) {
                                reply_selected.setVisibility(View.GONE);
                                forward_selected.setVisibility(View.GONE);
                            }
                        }
                    }
                }

            }


            @Override
            public void onSingleLongItemClick(int position) {


                if (!messageList.get(position).getIsDeleted().equals("true")) {
                    isSelected = true;

                    View view = message_list.findViewHolderForAdapterPosition(position).itemView;
                    view.setSelected(true);

                    selectedPositions.add(position);
                    forward_selected.setVisibility(View.VISIBLE);

                    profile_relative_layout.setVisibility(View.GONE);
                    option_relative_layout.setVisibility(View.VISIBLE);
                    selected_count.setText(String.valueOf(selectedPositions.size()));

                }
            }

        });


        SwipeController controller = new SwipeController(getApplicationContext(), new ISwipeControllerActions() {
            @Override
            public void onSwipePerformed(int position) {

                if (!messageList.get(position).getIsDeleted().equals("true")) {
                    reply_to_msg(position);
                }
            }
        });
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(controller);
        itemTouchHelper.attachToRecyclerView(message_list);


    }

    private void reply_to_msg(int position) {
        isfor = true;
        forwarded_posititon = position;
        forwarded_msg.setVisibility(View.VISIBLE);
        forwarded_name.setVisibility(View.VISIBLE);
        cancelBtn.setVisibility(View.VISIBLE);
        forwarded_name.setText(messageList.get(position).getSenderId());
        forwarded_msg.setText(messageList.get(position).getMessage());
        msg.requestFocus();
        return;
    }

    private void selected_messages_layout_setTO_Gone() {

        isfor = false;
        forwarded_msg.setVisibility(View.GONE);
        forwarded_name.setVisibility(View.GONE);
        cancelBtn.setVisibility(View.GONE);
        return;


    }


}


