package com.example.gasteiapp;

// Classe que representa um local (ponto geográfico) salvo pelo usuário.
public class Place {
    private int id;
    private String name;
    private double latitude;
    private double longitude;
    
    // Construtor para inicializar um objeto Place.
    public Place(int id, String name, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    // Métodos Getters para acessar as propriedades do local.
    public int getId() { return id; }
    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    
    // Métodos Setters para modificar as propriedades do local.
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
} 