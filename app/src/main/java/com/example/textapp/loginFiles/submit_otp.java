package com.example.textapp.loginFiles;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.textapp.recycler_views.Chat.ImagesMessage.ImagesMessages;
import com.example.textapp.recycler_views.chatListView.MainActivity;
import com.example.textapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class submit_otp extends AppCompatActivity {

    private EditText in_otp;
    private TextView in_number;
    private Button signin_btn;
    private FirebaseAuth mAuth;
    private String mverificationid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_otp);

        Toolbar toolbar=findViewById(R.id.submit_otp_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        in_number = (TextView) findViewById(R.id.number_box);
        in_otp = (EditText) findViewById(R.id.otp_box);
        signin_btn = (Button) findViewById(R.id.btn_signin);
        String num = getIntent().getStringExtra("phnumber");
        mAuth = FirebaseAuth.getInstance();

        in_number.setText(num);
        send_verification_code(num);

        signin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String code = in_otp.getText().toString();

                if (code.isEmpty() || code.length() < 6) {
                    in_otp.setError("Enter valid code");
                    in_otp.requestFocus();
                    return;
                } else {
                    verifycode(code);
                }


            }
        });


    }

    private void send_verification_code(String num) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(num, 60, TimeUnit.SECONDS, this, mCallbacks);
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {


            Toast.makeText(getApplicationContext(), "Verifying code Automatically", Toast.LENGTH_SHORT).show();

            sign_in_with_credential(phoneAuthCredential);


        }

        @Override
        public void onVerificationFailed(FirebaseException e) {

            Toast.makeText(submit_otp.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            mverificationid = s;
            Toast.makeText(submit_otp.this, "Code Sent", Toast.LENGTH_SHORT).show();
        }
    };

    private void verifycode(String code) {

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mverificationid, code);
        sign_in_with_credential(credential);
    }

    private void sign_in_with_credential(PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    if (user != null) {
                        final DatabaseReference userDB = FirebaseDatabase.getInstance().getReference().child("user").child(user.getUid());

                        userDB.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (!dataSnapshot.exists()) {
                                    Map<String, Object> userMap = new HashMap<>();
                                    userMap.put("phone", user.getPhoneNumber());
                                    userMap.put("name", user.getPhoneNumber());
                                    userMap.put("profilePicture","-1");
                                    userDB.updateChildren(userMap);

                                    Intent intent = new Intent(submit_otp.this, profileSetupPage.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    Toast.makeText(submit_otp.this, "Log In Successful", Toast.LENGTH_SHORT).show();
                                    startActivity(intent);
                                    finish();

                                }
                                else {

                                    Intent intent = new Intent(submit_otp.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    Toast.makeText(submit_otp.this, "Log In Successful", Toast.LENGTH_SHORT).show();
                                    startActivity(intent);
                                    finish();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }


                } else {
                    Toast.makeText(submit_otp.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                }


            }
        });


    }


}



