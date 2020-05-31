package com.example.chatapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {

    private View view;
    private RecyclerView myrecyclerview;
    private DatabaseReference contactsRef,userRef;
    private FirebaseAuth mauth;
    private String currentUserId;
    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view=inflater.inflate(R.layout.fragment_contacts, container, false);
        myrecyclerview=view.findViewById(R.id.contacts_recyclerview_list);
        myrecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));

        mauth=FirebaseAuth.getInstance();
        currentUserId=mauth.getCurrentUser().getUid();

        contactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        userRef=FirebaseDatabase.getInstance().getReference().child("Users");
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options=new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ContactsViewHolder> adapter=new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Contacts model) {

                String userId=getRef(position).getKey();
                userRef.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       if(dataSnapshot.exists())
                       {
                           if(dataSnapshot.child("userState").hasChild("state"))
                           {
                               String state=dataSnapshot.child("userState").child("state").getValue().toString();
                               String date=dataSnapshot.child("userState").child("date").getValue().toString();
                               String time=dataSnapshot.child("userState").child("time").getValue().toString();

                               if(state.equals("online"))
                               {
                                   holder.onlineIcon.setVisibility(View.VISIBLE);
                               }
                               else if(state.equals("offline"))
                               {
                                   holder.onlineIcon.setVisibility(View.INVISIBLE);
                               }
                           }
                           else
                           {
                               holder.onlineIcon.setVisibility(View.INVISIBLE);
                           }

                           if(dataSnapshot.hasChild("image"))
                           {
                               String image=dataSnapshot.child("image").getValue().toString();
                               String name=dataSnapshot.child("name").getValue().toString();
                               String status=dataSnapshot.child("status").getValue().toString();

                               holder.username.setText(name);
                               holder.userstatus.setText(status);
                               Picasso.get().load(image).placeholder(R.drawable.profile_image).into(holder.profilepicture);
                           }
                           else
                           {

                               String name=dataSnapshot.child("name").getValue().toString();
                               String status=dataSnapshot.child("status").getValue().toString();

                               holder.username.setText(name);
                               holder.userstatus.setText(status);
                           }
                       }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                ContactsViewHolder viewHolder=new ContactsViewHolder(view);
                return viewHolder;
            }
        };

        myrecyclerview.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder {
        TextView username,userstatus;
        CircularImageView profilepicture;
        ImageView onlineIcon;
        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            username=itemView.findViewById(R.id.users_profile_name);
            userstatus=itemView.findViewById(R.id.users_status);
            profilepicture=itemView.findViewById(R.id.users_profile_image);
            onlineIcon=itemView.findViewById(R.id.users_online_status);
        }
    }
}
