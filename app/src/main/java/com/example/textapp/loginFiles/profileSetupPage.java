package com.example.textapp.loginFiles;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.textapp.R;
import com.example.textapp.recycler_views.chatListView.MainActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class profileSetupPage extends AppCompatActivity {

    private ImageView profilepic;
    private EditText makeupName;
    private Button nextBtn;
    private int Pick_image_intent = 1;
    private ProgressDialog progressDialog;

    private String profileimageURI=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup_page);

        profilepic=findViewById(R.id.madeupProfile_image);
        makeupName=findViewById(R.id.madeupNameBox);

        nextBtn=findViewById(R.id.nextBtnBox);



        profilepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                changepic();
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!makeupName.getText().toString().isEmpty())
                {
                    FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("name").setValue(makeupName.getText().toString());
                }
                else
                {
                    FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("name").setValue("-1");
                }

                if (profileimageURI==null)
                {
                    FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("profilePicture").setValue("-1");
                }
                else
                {
                    FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("profilePicture").setValue(profileimageURI);
                }

                Intent intent =new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();

            }
        });

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
        progressDialog=new ProgressDialog(profileSetupPage.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Uploading Image...");
        progressDialog.show();
        Glide.with(getApplicationContext()).load(Uri.parse(profilePicURI)).into(profilepic);

        final StorageReference filepath = FirebaseStorage.getInstance().getReference().child("TextApp").child("UserProfilePicture").child(FirebaseAuth.getInstance().getUid()).child("image");
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

                        profileimageURI=uri.toString();
                        progressDialog.dismiss();

                    }
                });


            }
        });
    }

}
