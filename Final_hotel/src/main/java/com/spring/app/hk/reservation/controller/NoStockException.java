package com.spring.app.hk.reservation.controller;

public class NoStockException extends RuntimeException {
    public NoStockException(String message) {
        super(message);
    }
}