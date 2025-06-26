package com.example.gasteiapp;

// Classe de modelo (Model) que representa um local salvo na aplicação.
public class Place {
    private int id;
    private String name;
    private double latitude;
    private double longitude;
    
    // Construtor para criar um objeto Place.
    public Place(int id, String name, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    // Getters para acessar os atributos do Place.
    public int getId() { return id; }
    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    
    // Setters para modificar os atributos do Place.
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
} 