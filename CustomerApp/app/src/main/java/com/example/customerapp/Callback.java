package com.example.customerapp;

public interface Callback<T>
{
    void onComplete(T result);
}