package com.example.textapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.example.textapp.recycler_views.Chat.messageActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class settingUpGroupInfo extends AppCompatActivity {


    private ArrayList<String> groupUserUIDList;
    private ImageView groupProfilePicture;
    private EditText groupName;
    private TextView createGroupBtn;
    private int Pick_image_intent = 1;
    private ProgressDialog progressDialog;
    private String groupProfilePictureURI=null;

    final String key = FirebaseDatabase.getInstance().getReference().child("chat").push().getKey();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_up_group_info);

        Toolbar toolbar=findViewById(R.id.setting_up_group_info_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        groupUserUIDList=new ArrayList<>();

        groupUserUIDList=getIntent().getExtras().getStringArrayList("groupUserUIDs");

        groupProfilePicture=findViewById(R.id.group_Profile_image);
        groupName=findViewById(R.id.group_name_box);
        createGroupBtn=findViewById(R.id.create_group_btn);



        createGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(groupName.getText().toString().isEmpty())
                {
                    groupName.setError("Enter Group Name");
                    groupName.requestFocus();
                }
                else
                {
                    createGroup(groupUserUIDList);
                    groupUserUIDList.clear();
                    Intent intent = new Intent(getApplicationContext(), messageActivity.class);
                    Bundle bundle = new Bundle();

                    bundle.putString("chatID", key);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                }

            }
        });


        groupProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                changepic();

            }
        });

    }

    private void createGroup(ArrayList<String> groupUserUIDList) {


        FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("chat").child(key).setValue("true");

        for (int i = 0; i < groupUserUIDList.size(); i++) {
            FirebaseDatabase.getInstance().getReference().child("user").child(groupUserUIDList.get(i)).child("chat").child(key).setValue("true");
        }

        FirebaseDatabase.getInstance().getReference().child("chatDetail").child(key).child("groupName").setValue(groupName.getText().toString());
        DatabaseReference userDB = FirebaseDatabase.getInstance().getReference().child("chatDetail").child(key).child("user");

        Map<String, Object> userMap = new HashMap<>();
        userMap.put(FirebaseAuth.getInstance().getUid(), "true");


        for (int i = 0; i < groupUserUIDList.size(); i++) {

            userMap.put(groupUserUIDList.get(i), "true");

        }

        userDB.updateChildren(userMap);

        if ( groupProfilePictureURI== null) {
            FirebaseDatabase.getInstance().getReference().child("chatDetail").child(key).child("groupProfilePicture").setValue("-1");
        } else

        {
            FirebaseDatabase.getInstance().getReference().child("chatDetail").child(key).child("groupProfilePicture").setValue(groupProfilePictureURI);
        }
    }

    private void changepic() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), Pick_image_intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String profilePicURI=null;
        if (resultCode == RESULT_OK) {
            if (requestCode == Pick_image_intent) {
                if (data.getClipData() == null) {
                    profilePicURI=data.getData().toString();
                }

            }
        }

        Glide.with(getApplicationContext()).load(Uri.parse(profilePicURI)).into(groupProfilePicture);
        progressDialog=new ProgressDialog(settingUpGroupInfo.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Changing Image...");
        progressDialog.show();
        final StorageReference filepath = FirebaseStorage.getInstance().getReference().child("TextApp").child("GroupProfilePicture").child(key).child("image");
        Bitmap bitmap = null;

        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(profilePicURI));
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

                        groupProfilePictureURI=uri.toString();
                        progressDialog.dismiss();
                    }
                });


            }
        });
    }


}
