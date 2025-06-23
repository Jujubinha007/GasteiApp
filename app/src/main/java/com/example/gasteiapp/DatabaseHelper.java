package com.example.gasteiapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    private Context context;
    private static final String DATABASE_NAME = "GasteiApp.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "gastos";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_VALUE = "value";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_FORMAPAGAMENTO = "forma_pagamento";
    private static final String COLUMN_CATEGORY = "category";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " (" + COLUMN_ID + "INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_DESCRIPTION + " TEXT, " +
                        COLUMN_VALUE + " DECIMAL, " +
                        COLUMN_DATE + " DATE, " +
                        COLUMN_FORMAPAGAMENTO + " TEXT, " +
                        COLUMN_CATEGORY + " TEXT);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(" DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
