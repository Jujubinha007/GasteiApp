package com.example.gasteiapp;

import android.os.Parcel;
import android.os.Parcelable;

// Classe de modelo (Model) que representa um gasto na aplicação.
// Implementa Parcelable para permitir a passagem de objetos Gasto entre Activities.
public class Gasto implements Parcelable {
    private int id;
    private String description;
    private double value;
    private String date;
    private String formaPagamento;
    private String category;
    private String imagePath;
    private Double latitude;
    private Double longitude;
    private String locationName;
    private int userId;

    // Construtor completo para criar um objeto Gasto.
    public Gasto(int id, String description, double value, String date, String formaPagamento, String category, String imagePath, Double latitude, Double longitude, String locationName, int userId) {
        this.id = id;
        this.description = description;
        this.value = value;
        this.date = date;
        this.formaPagamento = formaPagamento;
        this.category = category;
        this.imagePath = imagePath;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationName = locationName;
        this.userId = userId;
    }

    // Getters para acessar os atributos do Gasto.
    public int getId() { return id; }
    public String getDescription() { return description; }
    public double getValue() { return value; }
    public String getDate() { return date; }
    public String getFormaPagamento() { return formaPagamento; }
    public String getCategory() { return category; }
    public String getImagePath() { return imagePath; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getLocationName() { return locationName; }
    public int getUserId() { return userId; }

    // Setters para modificar os atributos do Gasto.
    public void setId(int id) { this.id = id; }
    public void setDescription(String description) { this.description = description; }
    public void setValue(double value) { this.value = value; }
    public void setDate(String date) { this.date = date; }
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }
    public void setCategory(String category) { this.category = category; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    public void setUserId(int userId) { this.userId = userId; }


    // Implementação da interface Parcelable
    // Construtor usado para recriar um objeto Gasto a partir de um Parcel.
    protected Gasto(Parcel in) {
        id = in.readInt();
        description = in.readString();
        value = in.readDouble();
        date = in.readString();
        formaPagamento = in.readString();
        category = in.readString();
        imagePath = in.readString();
        // Lógica para ler Double que pode ser null
        if (in.readByte() == 0) {
            latitude = null;
        } else {
            latitude = in.readDouble();
        }
        if (in.readByte() == 0) {
            longitude = null;
        } else {
            longitude = in.readDouble();
        }
        locationName = in.readString();
        userId = in.readInt();
    }

    // Objeto CREATOR necessário para a interface Parcelable.
    public static final Creator<Gasto> CREATOR = new Creator<Gasto>() {
        @Override
        public Gasto createFromParcel(Parcel in) {
            return new Gasto(in);
        }

        @Override
        public Gasto[] newArray(int size) {
            return new Gasto[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    // Escreve os dados do objeto Gasto em um Parcel.
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(description);
        dest.writeDouble(value);
        dest.writeString(date);
        dest.writeString(formaPagamento);
        dest.writeString(category);
        dest.writeString(imagePath);
        // Lógica para escrever Double que pode ser null
        if (latitude == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(latitude);
        }
        if (longitude == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(longitude);
        }
        dest.writeString(locationName);
        dest.writeInt(userId);
    }
}
