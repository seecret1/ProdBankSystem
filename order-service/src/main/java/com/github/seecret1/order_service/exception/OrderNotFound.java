package com.github.seecret1.order_service.exception;

public class OrderNotFound extends RuntimeException {

    public OrderNotFound(String message) {
      super(message);
    }

    public OrderNotFound() { }
}
