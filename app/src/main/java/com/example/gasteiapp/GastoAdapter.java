package com.example.gasteiapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class GastoAdapter extends RecyclerView.Adapter<GastoAdapter.GastoViewHolder> {

    private Context mContext;
    private Cursor mCursor;

    public GastoAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    public class GastoViewHolder extends RecyclerView.ViewHolder {
        public TextView descriptionText;
        public TextView valueText;
        public TextView dateText;

        public GastoViewHolder(@NonNull View itemView) {
            super(itemView);
            descriptionText = itemView.findViewById(R.id.gasto_description);
            valueText = itemView.findViewById(R.id.gasto_value);
            dateText = itemView.findViewById(R.id.gasto_date);
        }
    }

    @NonNull
    @Override
    public GastoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.gasto_item, parent, false);
        return new GastoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GastoViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position)) {
            return;
        }

        String description = mCursor.getString(mCursor.getColumnIndexOrThrow("description"));
        double value = mCursor.getDouble(mCursor.getColumnIndexOrThrow("value"));
        String date = mCursor.getString(mCursor.getColumnIndexOrThrow("date"));

        holder.descriptionText.setText(description);
        holder.valueText.setText(String.valueOf(value));
        holder.dateText.setText(date);
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = newCursor;

        if (newCursor != null) {
            notifyDataSetChanged();
        }
    }
} 
