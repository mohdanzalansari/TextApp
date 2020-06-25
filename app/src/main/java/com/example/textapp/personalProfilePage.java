package com.example.textapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class personalProfilePage extends AppCompatActivity {


    private ImageView personalProfileImage;
    private TextView personalName,personalNumber;
    private ProgressDialog progressDialog;
    String personalImageURI=null;
    private int Pick_image_intent=1;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.personal_image_options_menu, menu);

    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.view_image_box:

                if ((personalImageURI != null)&&(!personalImageURI.equals("-1"))) {
                    Intent intent=new Intent(personalProfilePage.this,imageViewer.class);
                    intent.putExtra("imageURI",personalImageURI);
                    startActivity(intent);

                } else
                {
                    Toast.makeText(getApplicationContext(),"No profile image",Toast.LENGTH_SHORT).show();
                }

                return true;

            case R.id.change_image_box:




                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), Pick_image_intent);




                return true;

            case R.id.remove_image_box:

                if(personalImageURI!=null) {
                    progressDialog=new ProgressDialog(personalProfilePage.this);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("Removing Image...");
                    progressDialog.show();
                    DatabaseReference userphoto = FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("profilePicture");
                    userphoto.keepSynced(true);
                    userphoto.setValue("-1");

                    FirebaseStorage filepath=FirebaseStorage.getInstance();

                    StorageReference imgref=filepath.getReferenceFromUrl(personalImageURI);
                    imgref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_profile_image);
                            personalProfileImage.setImageBitmap(bitmap);
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),"Profile Image Removed",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"No profile image",Toast.LENGTH_SHORT).show();
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

        progressDialog=new ProgressDialog(personalProfilePage.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Changing Image...");
        progressDialog.show();
        Glide.with(getApplicationContext()).load(Uri.parse(profilePicURI)).into(personalProfileImage);

        final String profileURI=profilePicURI;


        DatabaseReference userphoto = FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("profilePicture");
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
        final StorageReference filepath = FirebaseStorage.getInstance().getReference().child("TextApp").child("UserProfilePicture").child(FirebaseAuth.getInstance().getUid()).child("image");
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


                        DatabaseReference userphoto = FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("profilePicture");
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_profile_page);

        Toolbar toolbar=findViewById(R.id.personal_profile_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        personalProfileImage=findViewById(R.id.personalProfile_imageBOX);
        personalName=findViewById(R.id.personalNameBox);
        personalNumber=findViewById(R.id.personalNumberBox);

        DatabaseReference userphoto= FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("profilePicture");
        userphoto.keepSynced(true);
        userphoto.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    if(!dataSnapshot.getValue().toString().equals("-1")) {

                        personalImageURI=dataSnapshot.getValue().toString();
                        Glide.with(getApplicationContext()).load(Uri.parse(dataSnapshot.getValue().toString())).into(personalProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        registerForContextMenu(personalProfileImage);

        DatabaseReference username= FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("name");
        username.keepSynced(true);
        username.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.getValue().toString().equals("-1"))
                    {
                        personalName.setText("The name has not been set yet, Click to set a username");
                    }
                    else
                    {
                        personalName.setText(dataSnapshot.getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        personalName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final AlertDialog.Builder alert=new AlertDialog.Builder(personalProfilePage.this);
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
                                DatabaseReference username= FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("name");
                                username.keepSynced(true);

                                username.setValue(nameChange.getText().toString());
                                personalName.setText(nameChange.getText().toString());
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

        DatabaseReference usernumber= FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("phone");
        usernumber.keepSynced(true);
        usernumber.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    personalNumber.setText(dataSnapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
