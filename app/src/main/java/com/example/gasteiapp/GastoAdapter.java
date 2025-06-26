package com.example.gasteiapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Adapter para exibir a lista de gastos em um RecyclerView.
public class GastoAdapter extends RecyclerView.Adapter<GastoAdapter.GastoViewHolder> {

    private Context mContext;
    private List<Gasto> mGastoList; // Lista de objetos Gasto a serem exibidos
    private OnItemClickListener listener; // Listener para cliques nos itens

    // Interface para o callback de clique no item
    public interface OnItemClickListener {
        void onItemClick(Gasto gasto);
    }

    // Define o listener para cliques nos itens
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // Construtor do adapter
    public GastoAdapter(Context context, List<Gasto> gastoList) {
        mContext = context;
        mGastoList = gastoList != null ? gastoList : new ArrayList<>();
    }

    // ViewHolder que representa cada item da lista de gastos
    public class GastoViewHolder extends RecyclerView.ViewHolder {
        public TextView categoryText, paymentMethodText;
        public TextView descriptionText, valueText, dateText;

        public GastoViewHolder(@NonNull View itemView) {
            super(itemView);
            // Referencia os componentes da UI dentro do item de layout
            categoryText = itemView.findViewById(R.id.gasto_category);
            paymentMethodText = itemView.findViewById(R.id.gasto_payment_method);
            descriptionText = itemView.findViewById(R.id.gasto_description);
            valueText = itemView.findViewById(R.id.gasto_value);
            dateText = itemView.findViewById(R.id.gasto_date);
            // Configura o listener de clique para o item completo
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(mGastoList.get(getAdapterPosition()));
                }
            });
        }
    }

    @NonNull
    @Override
    // Cria e retorna um novo ViewHolder
    public GastoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.gasto_item, parent, false);
        return new GastoViewHolder(view);
    }

    @Override
    // Preenche os dados de um item específico no ViewHolder
    public void onBindViewHolder(@NonNull GastoViewHolder holder, int position) {
        Gasto gasto = mGastoList.get(position);

        holder.descriptionText.setText(gasto.getDescription());
        // Formata o valor como moeda brasileira
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        holder.valueText.setText(currencyFormat.format(gasto.getValue()));
        holder.dateText.setText(formatDateForDisplay(gasto.getDate()));
        holder.categoryText.setText(gasto.getCategory());
        holder.paymentMethodText.setText(gasto.getFormaPagamento());
    }

    @Override
    // Retorna o número total de itens na lista
    public int getItemCount() {
        return mGastoList.size();
    }

    // Atualiza a lista de gastos e notifica o adapter sobre a mudança
    public void setGastos(List<Gasto> gastos) {
        mGastoList = gastos != null ? gastos : new ArrayList<>();
        notifyDataSetChanged();
    }

    // Formata a string de data para exibição
    private String formatDateForDisplay(String dateStr) {
        if (dateStr == null) return "";
        // Assume que a data do DB está no formato "yyyy-MM-dd"
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        // Define o formato de exibição desejado
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date date = dbFormat.parse(dateStr);
            return displayFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateStr; // Retorna a string original em caso de erro de parsing
        }
    }
} 
