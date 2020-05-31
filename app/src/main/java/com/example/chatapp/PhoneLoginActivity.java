package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    Button send_verification_number,verification_button;
    EditText phone_number,verification_code;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private FirebaseAuth mAuth;
    private String verificationID;
    private ProgressDialog progressDialog;
    private PhoneAuthProvider.ForceResendingToken token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);
        mAuth=FirebaseAuth.getInstance();

        send_verification_number=findViewById(R.id.send_ver_code_button);
        verification_button=findViewById(R.id.verify_button);
        phone_number=findViewById(R.id.phone_number_input);
        verification_code=findViewById(R.id.verification_code_input);
        progressDialog=new ProgressDialog(this);

        send_verification_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String number=phone_number.getText().toString();
                if(TextUtils.isEmpty(number))
                {
                    Toast.makeText(PhoneLoginActivity.this,"Please Enter mobile number",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    progressDialog.setTitle("Phone verification");
                    progressDialog.setMessage("please wait, we are authenticate your phone...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            number,60, TimeUnit.SECONDS,PhoneLoginActivity.this,callbacks);
                }
            }
        });

        verification_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_verification_number.setVisibility(View.INVISIBLE);
                phone_number.setVisibility(View.INVISIBLE);

                String verificationcode=verification_code.getText().toString();
                if(TextUtils.isEmpty(verificationcode))
                {
                    Toast.makeText(PhoneLoginActivity.this,"Please enter verification code first...",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    progressDialog.setTitle("Code verification");
                    progressDialog.setMessage("please wait, we are verifying your code...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationID,verificationcode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        callbacks= new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                progressDialog.dismiss();
                Toast.makeText(PhoneLoginActivity.this,"Error: "+e,Toast.LENGTH_SHORT).show();

                send_verification_number.setVisibility(View.VISIBLE);
                phone_number.setVisibility(View.VISIBLE);

                verification_button.setVisibility(View.INVISIBLE);
                verification_code.setVisibility(View.INVISIBLE);
            }
            @Override
                public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                verificationID=s;
                token=forceResendingToken;
                progressDialog.dismiss();
                Toast.makeText(PhoneLoginActivity.this,"Code has been sent,please check in messagebox...",Toast.LENGTH_SHORT).show();

                send_verification_number.setVisibility(View.INVISIBLE);
                phone_number.setVisibility(View.INVISIBLE);

                verification_button.setVisibility(View.VISIBLE);
                verification_code.setVisibility(View.VISIBLE);
            }
        };
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            progressDialog.dismiss();
                            Toast.makeText(PhoneLoginActivity.this,"Congratulations you are login successfully...",Toast.LENGTH_SHORT).show();
                            Intent loginintent=new Intent(PhoneLoginActivity.this,MainActivity.class);
                            startActivity(loginintent);
                            finish();

                        } else {
                            String message=task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this,"Error :"+message,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
