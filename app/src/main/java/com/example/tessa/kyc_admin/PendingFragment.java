package com.example.tessa.kyc_admin;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class PendingFragment extends Fragment {

    Query query;
    FirebaseRecyclerOptions<User> options;
    FirebaseRecyclerAdapter adapter;
    RecyclerView mRecyclerView;

    public PendingFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        query = FirebaseDatabase.getInstance()
                .getReference()
                .child("users")
                .orderByChild("status")
                .equalTo(3)
                .limitToLast(50);

        options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_pending, container, false);

        adapter = new FirebaseRecyclerAdapter<User, ViewHolder>(options) {

            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recyclerview, parent, false);
                return new ViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(ViewHolder holder, int position, User model) {
                holder.mNameView.setText(model.getFull_name());
                holder.mIdView.setText(model.getId());
                holder.mUidView.setText(model.getUid());
                holder.mIconView.setImageResource(R.drawable.pendingicon);
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
            }
        };

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mRecyclerView.smoothScrollToPosition(adapter.getItemCount());
            }
        });

        mRecyclerView = view.findViewById(R.id.token_recyclerview);
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
}
