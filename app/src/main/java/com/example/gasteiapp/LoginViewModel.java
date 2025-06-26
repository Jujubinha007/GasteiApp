package com.example.gasteiapp;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

// ViewModel para a tela de login. Gerencia a lógica de negócio e a comunicação com o repositório de usuários.
public class LoginViewModel extends AndroidViewModel {
    private UserRepository userRepository; // Repositório para operações de usuário
    private MutableLiveData<Boolean> loginResult = new MutableLiveData<>(); // LiveData para o resultado do login
    private MutableLiveData<Integer> userId = new MutableLiveData<>(); // LiveData para o ID do usuário logado

    // Construtor do ViewModel. Recebe a Application para inicializar o UserRepository.
    public LoginViewModel(Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    // Retorna o LiveData observável para o resultado do login.
    public LiveData<Boolean> getLoginResult() {
        return loginResult;
    }

    // Retorna o LiveData observável para o ID do usuário.
    public LiveData<Integer> getUserId() {
        return userId;
    }

    // Método para realizar o login. Verifica as credenciais e atualiza os LiveData.
    public void login(String username, String password) {
        boolean isValid = userRepository.checkUser(username, password);
        loginResult.setValue(isValid);
        if (isValid) {
            userId.setValue(userRepository.getUserId(username));
        }
    }
}
