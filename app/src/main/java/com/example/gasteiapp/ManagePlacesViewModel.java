package com.example.gasteiapp;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;

// ViewModel para a tela de gerenciamento de locais. Gerencia a lógica de negócio e a comunicação com o repositório de locais.
public class ManagePlacesViewModel extends AndroidViewModel {
    private PlaceRepository placeRepository; // Repositório para operações de locais
    private MutableLiveData<List<Place>> places = new MutableLiveData<>(); // LiveData para a lista de locais

    // Construtor do ViewModel. Recebe a Application para inicializar o PlaceRepository.
    public ManagePlacesViewModel(Application application) {
        super(application);
        placeRepository = new PlaceRepository(application);
    }

    // Retorna o LiveData observável para a lista de locais.
    public LiveData<List<Place>> getPlaces() {
        return places;
    }

    // Carrega a lista de locais do usuário e atualiza o LiveData.
    public void loadPlaces(int userId) {
        places.setValue(placeRepository.getUserPlaces(userId));
    }

    // Adiciona um novo local e recarrega a lista.
    public void addPlace(Place place, int userId) {
        placeRepository.addPlace(place, userId);
        loadPlaces(userId);
    }

    // Atualiza um local existente e recarrega a lista.
    public void updatePlace(Place place, int userId) {
        placeRepository.updatePlace(place);
        loadPlaces(userId);
    }

    // Exclui um local e recarrega a lista.
    public void deletePlace(int placeId, int userId) {
        placeRepository.deletePlace(placeId);
        loadPlaces(userId);
    }
}
