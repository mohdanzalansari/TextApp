package com.example.textapp.loginFiles;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.textapp.R;
import com.example.textapp.recycler_views.chatListView.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class login_phoneno extends AppCompatActivity {

    private EditText input_number;
    private Button sendotp_btn;


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        if (FirebaseAuth.getInstance().getCurrentUser()!=null)
        {
            Intent intent= new Intent(login_phoneno.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_phoneno);

        input_number=(EditText)findViewById(R.id.phone_box);
        sendotp_btn=(Button)findViewById(R.id.send_btn);

        sendotp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String number="+91"+input_number.getText().toString();
                if (number.isEmpty() || number.length()<10)
                {
                    input_number.setError("Enter valid number");
                    input_number.requestFocus();
                    return;
                }
                else {
                    sendOTP(number);
                }
            }
        });



    }

    private void sendOTP(String number) {


        Intent intent= new Intent(login_phoneno.this, submit_otp.class);
        intent.putExtra("phnumber",number);
        startActivity(intent);


    }


}
