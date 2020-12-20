package com.dhiman.whataapp;


import android.content.Intent;
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
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {
    private View privateChatsView;
    private RecyclerView chatLists;
    private DatabaseReference ChatsRef,UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatsView=inflater.inflate(R.layout.fragment_chats, container, false);
        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();

        ChatsRef= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        chatLists=privateChatsView.findViewById(R.id.chats_list);
        chatLists.setLayoutManager(new LinearLayoutManager(getContext()));

        return privateChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options=new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatsRef,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ChatsViewHolder> adapter=new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder chatsViewHolder, int i, @NonNull Contacts contacts) {
                final String userIDs=getRef(i).getKey();
                final String[] retImage = {"default_image"};

                UsersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            if (dataSnapshot.hasChild("image")) {
                                retImage[0] = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(retImage[0]).placeholder(R.drawable.profile_image).into(chatsViewHolder.profileImage);
                            }
                            final String retName = dataSnapshot.child("name").getValue().toString();
                            final String retStatus = dataSnapshot.child("status").getValue().toString();
                            //Toast.makeText(getContext(), ""+retName, Toast.LENGTH_SHORT).show();
                            chatsViewHolder.userName.setText(retName);
                            chatsViewHolder.userStatus.setText("Last Seen...  Date :   Time:");


                            if (dataSnapshot.child("userState").hasChild("state"))
                            {
                                String state=dataSnapshot.child("userState").child("state").getValue().toString();
                                String date=dataSnapshot.child("userState").child("date").getValue().toString();
                                String time=dataSnapshot.child("userState").child("time").getValue().toString();

                                if (state.equals("online"))
                                {
                                    chatsViewHolder.userStatus.setText("Online");
                                    chatsViewHolder.onlineDot.setVisibility(View.VISIBLE);
                                }
                                else if (state.equals("offline"))
                                {
                                    chatsViewHolder.userStatus.setText("Last seen  "+date +" "+time);
                                    chatsViewHolder.onlineDot.setVisibility(View.INVISIBLE);


                                }

                            }
                            else
                            {
                                chatsViewHolder.userStatus.setText("Offline");
                                chatsViewHolder.onlineDot.setVisibility(View.INVISIBLE);


                            }



                            chatsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent ChatsIntent=new Intent(getContext(),ChatActivity.class);
                                    ChatsIntent.putExtra("visit_user_id",userIDs);
                                    ChatsIntent.putExtra("visit_user_name",retName);
                                    ChatsIntent.putExtra("visit_image", retImage[0]);

                                    startActivity(ChatsIntent);

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                return new ChatsViewHolder(view);
            }
        };

        chatLists.setAdapter(adapter);
        adapter.startListening();


    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder{

        CircleImageView profileImage;
        TextView userName,userStatus;
        ImageView onlineDot;


        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage=itemView.findViewById(R.id.users_profile_image);
            userName=itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_status);
            onlineDot=itemView.findViewById(R.id.user_online_status);
        }
    }
}