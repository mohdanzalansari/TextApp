package com.example.textapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.textapp.recycler_views.addmemberstogroup.addMembersToGroup;
import com.example.textapp.recycler_views.chatListView.chatListAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class profilePage extends AppCompatActivity {

    private String chatID, username, userprofileImage;
    private Boolean isGroup;

    private ProgressDialog progressDialog;
    private int Pick_image_intent=1;
    private TextView userprofilename, usermadeupname;
    private ImageView userprofileProfilePicture,editgroupname,editgroupimage,addMembers;

    private LinearLayout groupinfoLineaerLay;

    private RecyclerView group_member_list;
    private RecyclerView.Adapter group_member_list_adapter;
    private RecyclerView.LayoutManager group_member_list_layout_manager;

    private ArrayList<String> groupMemberUIDlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);



        chatID = getIntent().getExtras().getString("chatID");
        isGroup = getIntent().getExtras().getBoolean("isGroup");
        username = getIntent().getExtras().getString("username");
        userprofileImage = getIntent().getExtras().getString("userProfileImage");

        groupMemberUIDlist = new ArrayList<>();
        userprofilename = findViewById(R.id.userProfileNameBox);
        usermadeupname = findViewById(R.id.UserProfileMadeupName);
        userprofileProfilePicture = findViewById(R.id.userProfileImageBox);
        addMembers=findViewById(R.id.addmembersBox);


        groupinfoLineaerLay = findViewById(R.id.group_member_info);

        if (isGroup == true) {


            editgroupname=findViewById(R.id.editgropunamebox);
            editgroupimage=findViewById(R.id.editgroupimageBox);
            addMembers=findViewById(R.id.addmembersBox);


            editgroupimage.setVisibility(View.VISIBLE);
            editgroupname.setVisibility(View.VISIBLE);
            addMembers.setVisibility(View.VISIBLE);

            registerForContextMenu(editgroupimage);

            addMembers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent=new Intent(profilePage.this, addMembersToGroup.class);

                    Bundle bundle = new Bundle();
                    bundle.putString("chatID", chatID);
                    intent.putExtras(bundle);

                    startActivity(intent);
                }
            });

            editgroupname.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final AlertDialog.Builder alert=new AlertDialog.Builder(profilePage.this);
                    View mview=getLayoutInflater().inflate(R.layout.layout_for_name_change,null);

                    final EditText nameChange=mview.findViewById(R.id.personal_profile_name_change_box);
                    Button okBTN=mview.findViewById(R.id.personal_profile_change_ok_btn_box);
                    Button cancelBTN=mview.findViewById(R.id.personal_profile_cancel_btn_box);

                    alert.setView(mview);

                    final AlertDialog alertDialog=alert.create();
                    alertDialog.setCanceledOnTouchOutside(true);

                    alertDialog.show();


                    okBTN.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if(nameChange.getText().toString()==null )
                            {
                                nameChange.setError("Enter Name");
                                nameChange.requestFocus();
                            }
                            else
                            {
                                if (!nameChange.getText().toString().isEmpty())
                                {
                                    DatabaseReference usernamex= FirebaseDatabase.getInstance().getReference().child("chatDetail").child(chatID).child("groupName");
                                    usernamex.keepSynced(true);

                                    usernamex.setValue(nameChange.getText().toString());
                                    userprofilename.setText(nameChange.getText().toString());
                                    Toast.makeText(getApplicationContext(),"Name Changed Successfully",Toast.LENGTH_SHORT).show();
                                    alertDialog.dismiss();
                                }

                            }


                        }
                    });

                    cancelBTN.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            alertDialog.dismiss();
                        }
                    });

                }
            });

            editgroupimage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Toast.makeText(getApplicationContext(),"Long Press to change Group Image",Toast.LENGTH_SHORT).show();

                }
            });

            groupinfoLineaerLay.setVisibility(View.VISIBLE);
            DatabaseReference usergroupuid = FirebaseDatabase.getInstance().getReference().child("chatDetail").child(chatID).child("user");
            usergroupuid.keepSynced(true);

            usergroupuid.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot chidSnapshot : dataSnapshot.getChildren()) {
                            groupMemberUIDlist.add(chidSnapshot.getKey());

                        }
                        initializeRecyclerView();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            groupinfoLineaerLay.setVisibility(View.GONE);
        }


        if (!username.equals("-1")) {
            userprofilename.setText(username);
        } else {


            final DatabaseReference usernameMadeup = FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("name");
            usernameMadeup.keepSynced(true);

            usernameMadeup.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (!dataSnapshot.getValue().toString().equals("-1")) {
                            usermadeupname.setVisibility(View.VISIBLE);
                            usermadeupname.setText(dataSnapshot.getValue().toString());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            final DatabaseReference usernamerefe = FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("phone");
            usernamerefe.keepSynced(true);

            usernamerefe.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        userprofilename.setText(dataSnapshot.getValue().toString());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }


        if ((userprofileImage.equals("-1")) && (isGroup == true)) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_group_image);
            userprofileProfilePicture.setImageBitmap(bitmap);
        } else if (!userprofileImage.equals("-1")) {
            Glide.with(getApplicationContext()).load(Uri.parse(userprofileImage)).into(userprofileProfilePicture);
        }


    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.group_image_change_options, menu);

    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.change_group_image_box:

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), Pick_image_intent);
                return true;

            case R.id.remove_group_image_box:

                if(userprofileImage!=null) {
                    progressDialog=new ProgressDialog(profilePage.this);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("Removing Image...");
                    progressDialog.show();
                    DatabaseReference usergroupphoto = FirebaseDatabase.getInstance().getReference().child("chatDetail").child(chatID).child("groupProfilePicture");
                    usergroupphoto.keepSynced(true);
                    usergroupphoto.setValue("-1");

                    FirebaseStorage filepath=FirebaseStorage.getInstance();

                    StorageReference imgref=filepath.getReferenceFromUrl(userprofileImage);
                    imgref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_profile_image);
                            userprofileProfilePicture.setImageBitmap(bitmap);
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),"Group Image Removed",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"No Group image",Toast.LENGTH_SHORT).show();
                }

                return true;

            default:
                return super.onContextItemSelected(item);




        }



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String profilePicURI=null;
        if (resultCode == RESULT_OK) {
            if (requestCode == Pick_image_intent) {
                if (data.getClipData() == null) {
                    profilePicURI=data.getData().toString();
                }

            }
        }

        progressDialog=new ProgressDialog(profilePage.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Changing Image...");
        progressDialog.show();
        Glide.with(getApplicationContext()).load(Uri.parse(profilePicURI)).into(userprofileProfilePicture);

        final String profileURI=profilePicURI;


        DatabaseReference userphoto = FirebaseDatabase.getInstance().getReference().child("chatDetail").child(chatID).child("groupProfilePicture");
        userphoto.keepSynced(true);

        userphoto.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.getValue().toString().equals("-1"))
                    {
                        updateProfileimage(profileURI);
                    }
                    else
                    {
                        FirebaseStorage filepathx=FirebaseStorage.getInstance();

                        StorageReference imgref=filepathx.getReferenceFromUrl(dataSnapshot.getValue().toString());
                        imgref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {


                                updateProfileimage(profileURI);

                            }
                        });
                    }
                }
                else
                {
                    updateProfileimage(profileURI);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




    }

    public void updateProfileimage(String profileURI)
    {
        final StorageReference filepath = FirebaseStorage.getInstance().getReference().child("TextApp").child("GroupProfilePicture").child(chatID).child("image");
        Bitmap bitmap = null;

        try {
            bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), Uri.parse(profileURI));
        }
        catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Something went wrong, please try again later...", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            return;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 10, baos);
        byte[] image_data = baos.toByteArray();

        UploadTask uploadTask = filepath.putBytes(image_data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {


                        DatabaseReference userphoto = FirebaseDatabase.getInstance().getReference().child("chatDetail").child(chatID).child("groupProfilePicture");
                        userphoto.keepSynced(true);
                        userphoto.setValue(uri.toString());
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(),"Profile Image Changed",Toast.LENGTH_SHORT).show();

                        return;
                    }
                });


            }
        });
    }

    private void initializeRecyclerView() {

        group_member_list = findViewById(R.id.group_member_info_recycler);
        group_member_list.setNestedScrollingEnabled(false);
        group_member_list.setHasFixedSize(false);
        group_member_list_layout_manager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        group_member_list.setLayoutManager(group_member_list_layout_manager);
        group_member_list_adapter = new GroupUIDListAdapter(getApplicationContext(), groupMemberUIDlist);
        group_member_list.setAdapter(group_member_list_adapter);
    }
}
