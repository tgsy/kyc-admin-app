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

import static android.support.v7.widget.RecyclerView.*;


public class VerifyFragment extends Fragment {

    Query query;
    FirebaseRecyclerOptions<User> options;
    FirebaseRecyclerAdapter adapter;
    RecyclerView mRecyclerView;

//    HashMap<String, String> status = new HashMap<>();
//        status.put("0", "Pending Verification");
//        status.put("1", "Pending Token Generation");
//        status.put("2", "Verified Customer");
//        status.put("3", "Lost Token");

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
        Log.i("DED","OnCreateView: ");
        View view = inflater.inflate(R.layout.fragment_verify, container, false);

        adapter = new FirebaseRecyclerAdapter<User, ViewHolder>(options) {

            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recyclerview, parent, false);
                return new ViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(ViewHolder holder, int position, User model) {
                Log.i("DED","test: "+ model.getFull_name());
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
        //if (isSignedIn()) { attachRecyclerViewAdapter(); }
        //FirebaseAuth.getInstance().addAuthStateListener(this);
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        //FirebaseAuth.getInstance().removeAuthStateListener(this);
        adapter.stopListening();
    }

    private boolean isSignedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final View mView;
        private final TextView mNameView;
        private final TextView mIdView;
        private final ImageView mIconView;
        private final TextView mUidView;
        private User mUser;

        public ViewHolder(View view) {
            super(view);
            this.mView = view;
            this.mNameView = (TextView) view.findViewById(R.id.verify_name);
            this.mIdView = (TextView) view.findViewById(R.id.verify_id);
            this.mIconView = (ImageView) view.findViewById(R.id.verify_icon);
            this.mUidView = (TextView) view.findViewById(R.id.verify_Uid);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), VerifyActivity.class);
            intent.putExtra("Uid", mUidView.getText());
            startActivity(intent);
        }
    }
}