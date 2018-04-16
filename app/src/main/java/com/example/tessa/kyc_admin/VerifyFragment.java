package com.example.tessa.kyc_admin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;

import static android.support.v7.widget.RecyclerView.*;


public class VerifyFragment extends Fragment {

    Query query;
    FirebaseRecyclerOptions<User> options;
    FirebaseRecyclerAdapter adapter;
    RecyclerView mRecyclerView;

    public VerifyFragment() {  }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            query = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("users")
                    .orderByChild("status")
                    .equalTo(0)
                    .limitToLast(50);

        options = new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(query, User.class)
                        .build();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_verify, container, false);

        adapter = new FirebaseRecyclerAdapter<User, VerifyViewHolder>(options) {

            @Override
            public VerifyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recyclerview, parent, false);
                return new VerifyViewHolder(view);
            }


            @Override
            protected void onBindViewHolder(@NonNull VerifyViewHolder holder, int position, @NonNull User model) {
                holder.mNameView.setText(model.getFull_name());
                holder.mIdView.setText(model.getId());
                holder.mUidView.setText(model.getUid());
                holder.mIconView.setImageResource(R.drawable.ic_navigate_next_black_24dp);
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
            }
        };

        adapter.registerAdapterDataObserver(new AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mRecyclerView.smoothScrollToPosition(adapter.getItemCount());
            }
        });



        mRecyclerView = view.findViewById(R.id.verify_recyclerview);

        //mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(adapter);

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private boolean isSignedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }


    public class VerifyViewHolder extends ViewHolder {

        public VerifyViewHolder(View view) {
            super(view);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), VerifyActivity.class);
            intent.putExtra("Uid", mUidView.getText());
            startActivity(intent);
        }
    }
}