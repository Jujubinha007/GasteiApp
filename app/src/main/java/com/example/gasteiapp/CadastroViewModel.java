package com.example.gasteiapp;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

// ViewModel para a tela de cadastro. Gerencia a lógica de negócio e a comunicação com o repositório de usuários.
public class CadastroViewModel extends AndroidViewModel {
    private UserRepository userRepository; // Repositório para operações de usuário
    private MutableLiveData<Boolean> registrationResult = new MutableLiveData<>(); // LiveData para o resultado do cadastro
    private MutableLiveData<String> error = new MutableLiveData<>(); // LiveData para mensagens de erro

    // Construtor do ViewModel. Recebe a Application para inicializar o UserRepository.
    public CadastroViewModel(Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    // Retorna o LiveData observável para o resultado do cadastro.
    public LiveData<Boolean> getRegistrationResult() {
        return registrationResult;
    }

    // Retorna o LiveData observável para mensagens de erro.
    public LiveData<String> getError() {
        return error;
    }

    // Método para registrar um novo usuário.
    public void register(String username, String password) {
        // Verifica se o usuário já existe
        if (userRepository.checkUser(username)) {
            error.setValue("Este e-mail já está cadastrado.");
            registrationResult.setValue(false);
        } else {
            // Tenta adicionar o usuário
            boolean success = userRepository.addUser(username, password);
            if (success) {
                registrationResult.setValue(true);
            } else {
                error.setValue("Erro ao realizar o cadastro.");
                registrationResult.setValue(false);
            }
        }
    }
}
