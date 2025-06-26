package com.example.gasteiapp;

import static android.text.TextUtils.indexOf;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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
        public Spinner spinnerCategoria, spinnerFmPagamentos;
        public TextView descriptionText, valueText, dateText;

        public GastoViewHolder(@NonNull View itemView) {
            super(itemView);
            spinnerCategoria = itemView.findViewById(R.id.spinnerCategoria);
            spinnerFmPagamentos = itemView.findViewById(R.id.spinnerFmPagamento);
            descriptionText = itemView.findViewById(R.id.gasto_description);
            valueText = itemView.findViewById(R.id.gasto_value);
            dateText = itemView.findViewById(R.id.gasto_date);

            ArrayAdapter<CharSequence> catAdapter = ArrayAdapter.createFromResource(
                    mContext, R.array.categorias_array, android.R.layout.simple_spinner_item);
            catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategoria.setAdapter(catAdapter);

            ArrayAdapter<CharSequence> pagAdapter = ArrayAdapter.createFromResource(
                    mContext, R.array.pagamento_array, android.R.layout.simple_spinner_item);
            pagAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerFmPagamentos.setAdapter(pagAdapter);

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
        String category = mCursor.getString(mCursor.getColumnIndexOrThrow("category"));
        String forma_pagamento  = mCursor.getString(mCursor.getColumnIndexOrThrow("forma_pagamento"));

        holder.descriptionText.setText(description);
        holder.valueText.setText(String.valueOf(value));
        holder.dateText.setText(date);holder.spinnerCategoria.setSelection(indexOf(R.array.categorias_array, category));
        holder.spinnerFmPagamentos.setSelection(indexOf(R.array.pagamento_array, forma_pagamento));
        }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (mCursor != null) mCursor.close();
        mCursor = newCursor;
        if (newCursor != null) notifyDataSetChanged();
    }

    // ---------- util ----------
    private int indexOf(int arrayResId, String value) {
        if (value == null) return 0;
        Resources res = mContext.getResources();
        String[] items = res.getStringArray(arrayResId);
        for (int i = 0; i < items.length; i++) {
            if (value.equalsIgnoreCase(items[i])) return i;
        }
        return 0; // default
    }
} 
