package com.example.gasteiapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// Adapter para exibir a lista de locais em um RecyclerView.
public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.PlaceViewHolder> {
    
    private List<Place> places; // Lista de objetos Place a serem exibidos
    private OnPlaceEditListener editListener; // Listener para edição de local
    private OnPlaceDeleteListener deleteListener; // Listener para exclusão de local
    
    // Interface para o callback de edição de local
    public interface OnPlaceEditListener {
        void onEdit(Place place);
    }
    
    // Interface para o callback de exclusão de local
    public interface OnPlaceDeleteListener {
        void onDelete(Place place);
    }
    
    // Construtor do adapter
    public PlacesAdapter(List<Place> places, OnPlaceEditListener editListener, OnPlaceDeleteListener deleteListener) {
        this.places = places;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }
    
    @NonNull
    @Override
    // Cria e retorna um novo ViewHolder
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place, parent, false);
        return new PlaceViewHolder(view);
    }
    
    @Override
    // Preenche os dados de um item específico no ViewHolder
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Place place = places.get(position);
        holder.bind(place);
    }
    
    @Override
    // Retorna o número total de itens na lista
    public int getItemCount() {
        return places.size();
    }

    // Atualiza a lista de locais e notifica o adapter sobre a mudança
    public void setPlaces(List<Place> newPlaces) {
        this.places = newPlaces;
        notifyDataSetChanged();
    }
    
    // ViewHolder que representa cada item da lista de locais
    class PlaceViewHolder extends RecyclerView.ViewHolder {
        private TextView tvPlaceName;
        private TextView tvCoordinates;
        private ImageButton btnEdit;
        private ImageButton btnDelete;
        
        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            // Referencia os componentes da UI dentro do item de layout
            tvPlaceName = itemView.findViewById(R.id.tvPlaceName);
            tvCoordinates = itemView.findViewById(R.id.tvCoordinates);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
        
        // Vincula os dados do objeto Place aos componentes da UI
        public void bind(Place place) {
            tvPlaceName.setText(place.getName());
            tvCoordinates.setText(String.format("%.6f, %.6f", place.getLatitude(), place.getLongitude()));
            
            // Configura o listener de clique para o botão de edição
            btnEdit.setOnClickListener(v -> {
                if (editListener != null) {
                    editListener.onEdit(place);
                }
            });
            
            // Configura o listener de clique para o botão de exclusão
            btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(place);
                }
            });
        }
    }
} 