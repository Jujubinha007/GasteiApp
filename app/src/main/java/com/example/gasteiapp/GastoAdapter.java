package com.example.gasteiapp;

import static android.text.TextUtils.indexOf;

import android.content.Context;
import android.content.Intent;
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

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// Adaptador para exibir a lista de gastos em um RecyclerView.
public class GastoAdapter extends RecyclerView.Adapter<GastoAdapter.GastoViewHolder> {

    private Context mContext;
    private Cursor mCursor;
    private OnItemClickListener listener;

    // Interface para lidar com cliques nos itens da lista.
    public interface OnItemClickListener {
        void onItemClick(Cursor cursor);
    }

    // Define o listener de clique para os itens.
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // Construtor do GastoAdapter.
    public GastoAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    // ViewHolder para os itens da lista de gastos.
    public class GastoViewHolder extends RecyclerView.ViewHolder {
        public TextView categoryText, paymentMethodText;
        public TextView descriptionText, valueText, dateText;

        // Construtor do ViewHolder, inicializa as views e configura o listener de clique.
        public GastoViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryText = itemView.findViewById(R.id.gasto_category);
            paymentMethodText = itemView.findViewById(R.id.gasto_payment_method);
            descriptionText = itemView.findViewById(R.id.gasto_description);
            valueText = itemView.findViewById(R.id.gasto_value);
            dateText = itemView.findViewById(R.id.gasto_date);
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    mCursor.moveToPosition(getAdapterPosition());
                    listener.onItemClick(mCursor);
                }
            });
        }
    }

    @NonNull
    @Override
    // Cria e retorna um novo ViewHolder quando necessário.
    public GastoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.gasto_item, parent, false);
        return new GastoViewHolder(view);
    }

    @Override
    // Vincula os dados de um gasto a um ViewHolder.
    public void onBindViewHolder(@NonNull GastoViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position)) {
            return;
        }

        // Obtém os dados do gasto do Cursor.
        String description = mCursor.getString(mCursor.getColumnIndexOrThrow("description"));
        double value = mCursor.getDouble(mCursor.getColumnIndexOrThrow("value"));
        String date = mCursor.getString(mCursor.getColumnIndexOrThrow("date"));
        String category = mCursor.getString(mCursor.getColumnIndexOrThrow("category"));
        String forma_pagamento  = mCursor.getString(mCursor.getColumnIndexOrThrow("forma_pagamento"));

        // Define os textos nas Views do ViewHolder.
        holder.descriptionText.setText(description);
        // Formata o valor como moeda brasileira.
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        holder.valueText.setText(currencyFormat.format(value));
        // Formata a data para exibição.
        holder.dateText.setText(formatDateForDisplay(date));
        holder.categoryText.setText(category);
        holder.paymentMethodText.setText(forma_pagamento);
    }

    @Override
    // Retorna o número total de itens na lista.
    public int getItemCount() {
        return mCursor.getCount();
    }

    // Troca o Cursor de dados e notifica o adaptador sobre a mudança.
    public void swapCursor(Cursor newCursor) {
        if (mCursor != null) mCursor.close();
        mCursor = newCursor;
        if (newCursor != null) notifyDataSetChanged();
    }

    // ---------- util ----------
    // Encontra o índice de um valor em um array de recursos.
    private int indexOf(int arrayResId, String value) {
        if (value == null) return 0;
        Resources res = mContext.getResources();
        String[] items = res.getStringArray(arrayResId);
        for (int i = 0; i < items.length; i++) {
            if (value.equalsIgnoreCase(items[i])) return i;
        }
        return 0; // Padrão se não encontrado.
    }

    // Formata uma string de data do formato "yyyy-MM-dd" para "dd/MM/yyyy".
    private String formatDateForDisplay(String dateStr) {
        if (dateStr == null) return "";
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date date = dbFormat.parse(dateStr);
            return displayFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateStr; // Retorna a string original se a formatação falhar.
        }
    }
} 
