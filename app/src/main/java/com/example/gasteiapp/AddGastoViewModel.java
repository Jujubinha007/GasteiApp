package com.example.gasteiapp;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

// ViewModel para a tela de adicionar/editar gastos. Gerencia a lógica de negócio e a comunicação com os repositórios.
public class AddGastoViewModel extends AndroidViewModel {
    private GastoRepository gastoRepository; // Repositório para operações de gastos
    private PlaceRepository placeRepository; // Repositório para operações de locais
    private MutableLiveData<Boolean> saveResult = new MutableLiveData<>(); // LiveData para o resultado da operação de salvar/atualizar

    // Construtor do ViewModel. Recebe a Application para inicializar os repositórios.
    public AddGastoViewModel(Application application) {
        super(application);
        gastoRepository = new GastoRepository(application);
        placeRepository = new PlaceRepository(application);
    }

    // Retorna o LiveData observável para o resultado da operação de salvar/atualizar.
    public LiveData<Boolean> getSaveResult() {
        return saveResult;
    }

    // Salva um novo gasto no banco de dados.
    public void saveGasto(Gasto gasto) {
        boolean success = gastoRepository.addGasto(gasto);
        saveResult.setValue(success);
    }

    // Atualiza um gasto existente no banco de dados.
    public void updateGasto(Gasto gasto) {
        boolean success = gastoRepository.updateGasto(gasto);
        saveResult.setValue(success);
    }

    // Exclui um gasto do banco de dados.
    public void deleteGasto(int gastoId) {
        gastoRepository.deleteGasto(gastoId);
    }

    // Encontra um local salvo próximo a uma dada coordenada.
    public String findNearbyPlace(double lat, double lon, int userId) {
        return placeRepository.findNearbyPlace(lat, lon, userId);
    }

    // Adiciona um novo local ao banco de dados.
    public void addPlace(Place place, int userId) {
        placeRepository.addPlace(place, userId);
    }
}
