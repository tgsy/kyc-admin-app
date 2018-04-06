package com.example.tessa.kyc_admin;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    final View mView;
    final TextView mNameView;
    final TextView mIdView;
    final ImageView mIconView;
    final TextView mUidView;
    User mUser;

    public ViewHolder(View view) {
        super(view);
        this.mView = view;
        this.mNameView = (TextView) view.findViewById(R.id.recycler_name);
        this.mIdView = (TextView) view.findViewById(R.id.recycler_id);
        this.mIconView = (ImageView) view.findViewById(R.id.recycler_icon);
        this.mUidView = (TextView) view.findViewById(R.id.recycler_Uid);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

    }
}
