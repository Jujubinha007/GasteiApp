package com.example.gasteiapp;

// Classe de modelo (Model) que representa um usuário na aplicação.
public class User {
    private int id;
    private String username;
    private String passwordHash; // Armazena o hash da senha do usuário

    // Construtor para criar um objeto User.
    public User(int id, String username, String passwordHash) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    // Getters para acessar os atributos do User.
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }

    // Setters para modificar os atributos do User.
    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}
