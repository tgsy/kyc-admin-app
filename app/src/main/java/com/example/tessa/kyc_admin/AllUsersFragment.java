package com.example.tessa.kyc_admin;

import android.app.SearchManager;
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
import android.widget.SearchView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class AllUsersFragment extends Fragment {

    Query query;
    FirebaseRecyclerOptions<User> options;
    FirebaseRecyclerAdapter adapter;
    RecyclerView mRecyclerView;

    public AllUsersFragment() {    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b = getArguments();
        Log.i("TES", "bundlesize: "+b.size());

        if (b.size()!=0) {
            String search = b.getString("Query");
            Log.i("TES", "setting query at bsize>0 here");
            Log.i("TES", "b.getString(): " +search);

            query = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("users")
                    .orderByChild("id")
                    .startAt(search)
                    .endAt(search + "\uf8ff")
                    .limitToLast(50);
        } else {
            Log.i("TES", "setting query at bsize=0 here");
            query = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("users")
                    .limitToLast(50);
        }
        options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_all_users, container, false);

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
                if (model.getStatus()==0)
                    holder.mIconView.setImageResource(R.drawable.ic_navigate_next_black_24dp);
                else if (model.getStatus()==2)
                    holder.mIconView.setImageResource(R.drawable.ic_verified_user_black_24dp);
                else
                    holder.mIconView.setImageResource(R.drawable.ic_report_black_24dp);
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

        mRecyclerView = view.findViewById(R.id.allusers_recyclerview);

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
}
