package com.example.gasteiapp;

import android.content.Context;

// Repositório para gerenciar operações de dados relacionadas a usuários.
// Atua como uma camada de abstração entre o ViewModel e a fonte de dados (DatabaseHelper).
public class UserRepository {
    private DatabaseHelper dbHelper; // Instância do DatabaseHelper para acesso ao banco de dados

    // Construtor do repositório. Recebe o contexto para inicializar o DatabaseHelper.
    public UserRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Adiciona um novo usuário ao banco de dados, com a senha já "hashed".
    public boolean addUser(String username, String password) {
        String hashedPassword = PasswordHasher.hashPassword(password);
        return dbHelper.addUser(username, hashedPassword);
    }

    // Verifica se um usuário com o nome de usuário (email) fornecido já existe.
    public boolean checkUser(String username) {
        return dbHelper.checkUser(username);
    }

    // Verifica as credenciais de um usuário (nome de usuário e senha).
    public boolean checkUser(String username, String password) {
        // Obtém o hash da senha armazenado para o nome de usuário.
        String hashedPassword = dbHelper.getPasswordHash(username);
        if (hashedPassword == null) {
            return false; // Usuário não encontrado
        }
        // Verifica se a senha fornecida corresponde ao hash armazenado.
        return PasswordHasher.verifyPassword(password, hashedPassword);
    }

    // Obtém o ID de um usuário a partir do seu nome de usuário (email).
    public int getUserId(String username) {
        return dbHelper.getUserId(username);
    }
}
