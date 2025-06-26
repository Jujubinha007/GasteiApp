package com.example.gasteiapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// Adaptador para exibir a lista de locais salvos em um RecyclerView.
public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.PlaceViewHolder> {
    
    private List<Place> places;
    private OnPlaceEditListener editListener;
    private OnPlaceDeleteListener deleteListener;
    
    // Interface para lidar com a edição de um local.
    public interface OnPlaceEditListener {
        void onEdit(Place place);
    }
    
    // Interface para lidar com a exclusão de um local.
    public interface OnPlaceDeleteListener {
        void onDelete(Place place);
    }
    
    // Construtor do PlacesAdapter.
    public PlacesAdapter(List<Place> places, OnPlaceEditListener editListener, OnPlaceDeleteListener deleteListener) {
        this.places = places;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }
    
    @NonNull
    @Override
    // Cria e retorna um novo ViewHolder quando necessário.
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place, parent, false);
        return new PlaceViewHolder(view);
    }
    
    @Override
    // Vincula os dados de um local a um ViewHolder.
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Place place = places.get(position);
        holder.bind(place);
    }
    
    @Override
    // Retorna o número total de itens na lista.
    public int getItemCount() {
        return places.size();
    }
    
    // ViewHolder para os itens da lista de locais.
    class PlaceViewHolder extends RecyclerView.ViewHolder {
        private TextView tvPlaceName;
        private TextView tvCoordinates;
        private ImageButton btnEdit;
        private ImageButton btnDelete;
        
        // Construtor do ViewHolder, inicializa as views.
        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlaceName = itemView.findViewById(R.id.tvPlaceName);
            tvCoordinates = itemView.findViewById(R.id.tvCoordinates);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
        
        // Vincula os dados de um 'Place' às Views do ViewHolder.
        public void bind(Place place) {
            tvPlaceName.setText(place.getName());
            tvCoordinates.setText(String.format("%.6f, %.6f", place.getLatitude(), place.getLongitude()));
            
            // Configura o listener para o botão de edição.
            btnEdit.setOnClickListener(v -> {
                if (editListener != null) {
                    editListener.onEdit(place);
                }
            });
            
            // Configura o listener para o botão de exclusão.
            btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(place);
                }
            });
        }
    }
} 