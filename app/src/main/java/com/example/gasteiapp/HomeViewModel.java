package com.example.gasteiapp;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;

// ViewModel para a tela Home. Gerencia a lógica de negócio e a comunicação com o repositório de gastos.
public class HomeViewModel extends AndroidViewModel {
    private GastoRepository gastoRepository; // Repositório para operações de gastos
    private MutableLiveData<List<Gasto>> gastos = new MutableLiveData<>(); // LiveData para a lista de gastos

    // Construtor do ViewModel. Recebe a Application para inicializar o GastoRepository.
    public HomeViewModel(Application application) {
        super(application);
        gastoRepository = new GastoRepository(application);
    }

    // Retorna o LiveData observável para a lista de gastos.
    public LiveData<List<Gasto>> getGastos() {
        return gastos;
    }

    // Carrega a lista de gastos do usuário com base no filtro e atualiza o LiveData.
    public void loadGastos(int userId, String filter) {
        gastos.setValue(gastoRepository.getGastosByUser(userId, filter));
    }
}
