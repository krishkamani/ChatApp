package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {
    private String reciever_id,sender_user_id,current_state;
    private CircularImageView visit_profile;
    private TextView visit_name,visit_status;
    private Button request_button,decline_button;
    private FirebaseAuth mauth;
    DatabaseReference ref,chatrequestref,contactsRef,NotificationRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mauth=FirebaseAuth.getInstance();
        sender_user_id=mauth.getCurrentUser().getUid();

        reciever_id=getIntent().getExtras().get("visit_user_id").toString();

        visit_profile=findViewById(R.id.visit_profile_image);
        visit_name=findViewById(R.id.visit_user_name);
        visit_status=findViewById(R.id.visit_status);
        request_button=findViewById(R.id.send_message_request_button);
        decline_button=findViewById(R.id.decline_message_request_button);
        current_state="new";

        if(sender_user_id.equals(reciever_id))request_button.setVisibility(View.INVISIBLE);
        else request_button.setVisibility(View.VISIBLE);

        ref= FirebaseDatabase.getInstance().getReference();
        chatrequestref=FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");
        NotificationRef=FirebaseDatabase.getInstance().getReference().child("Notifications");

        ref.child("Users").child(reciever_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.hasChild("name") && dataSnapshot.hasChild("image"))
                {
                    String retrieveusername=dataSnapshot.child("name").getValue().toString();
                    String retrieveuserstatus=dataSnapshot.child("status").getValue().toString();
                    String retrieveuserimage=dataSnapshot.child("image").getValue().toString();

                    visit_name.setText(retrieveusername);
                    visit_status.setText(retrieveuserstatus);
                    Picasso.get().load(retrieveuserimage).into(visit_profile);

                    ManageChatRequest();
                }
                else if(dataSnapshot.exists() && dataSnapshot.hasChild("name"))
                {
                    String retrieveusername=dataSnapshot.child("name").getValue().toString();
                    String retrieveuserstatus=dataSnapshot.child("status").getValue().toString();

                    visit_name.setText(retrieveusername);
                    visit_status.setText(retrieveuserstatus);

                    ManageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void ManageChatRequest() {
        chatrequestref.child(sender_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(reciever_id))
                {
                    String reuest_type=dataSnapshot.child(reciever_id).child("request_type").getValue().toString();
                    if(reuest_type.equals("sent"))
                    {
                        current_state="request_sent";
                        request_button.setText("  Cancel Chat Request  ");
                    }
                    else if(reuest_type.equals("received"))
                    {
                        current_state="request_received";
                        request_button.setText("  Accept Chat Request  ");

                        decline_button.setVisibility(View.VISIBLE);
                        decline_button.setEnabled(true);
                        decline_button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelChatRequest();
                            }
                        });
                    }
                }
                else
                {
                    contactsRef.child(sender_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(reciever_id))
                            {
                                current_state="friends";
                                request_button.setText(" Remove this contact ");
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

        request_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                request_button.setEnabled(false);

                if(current_state.equals("new"))
                {
                    SendChatRequest();
                }
                if(current_state.equals("request_sent"))
                {
                    CancelChatRequest();
                }
                if(current_state.equals("request_received"))
                {
                    AcceptChatRequest();
                }
                if(current_state.equals("friends"))
                {
                    RemoveSpecificChatRequest();
                }
            }
        });
    }

    private void RemoveSpecificChatRequest() {
        contactsRef.child(sender_user_id).child(reciever_id)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            contactsRef.child(reciever_id).child(sender_user_id)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                request_button.setEnabled(true);
                                                request_button.setText("  Send Request  ");
                                                current_state="new";

                                                decline_button.setVisibility(View.INVISIBLE);
                                                decline_button.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptChatRequest() {
        contactsRef.child(sender_user_id).child(reciever_id)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            contactsRef.child(reciever_id).child(sender_user_id)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                chatrequestref.child(sender_user_id).child(reciever_id)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful())
                                                                {
                                                                    chatrequestref.child(reciever_id).child(sender_user_id)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    request_button.setEnabled(true);
                                                                                    current_state="friends";
                                                                                    request_button.setText(" Remove this contact ");

                                                                                    decline_button.setVisibility(View.INVISIBLE);
                                                                                    decline_button.setEnabled(false);
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CancelChatRequest() {

        chatrequestref.child(sender_user_id).child(reciever_id)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            chatrequestref.child(reciever_id).child(sender_user_id)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                request_button.setEnabled(true);
                                                request_button.setText("  Send Request  ");
                                                current_state="new";

                                                decline_button.setVisibility(View.INVISIBLE);
                                                decline_button.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });


    }

    private void SendChatRequest() {

        chatrequestref.child(sender_user_id).child(reciever_id).child("request_type")
                .setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            chatrequestref.child(reciever_id).child(sender_user_id)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                HashMap<String,String> chatnotificationMap=new HashMap<>();
                                                chatnotificationMap.put("from",sender_user_id);
                                                chatnotificationMap.put("type","request");
                                                NotificationRef.child(reciever_id).push().setValue(chatnotificationMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful())
                                                                {
                                                                    request_button.setEnabled(true);
                                                                    current_state="request_sent";
                                                                    request_button.setText("  Cancel Chat Request  ");
                                                                }
                                                            }
                                                        });

                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
