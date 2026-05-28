package com.github.seecret1.userservice.dto;

public class AddressWriteDto {

    private String address;

    private String zipCode;

    private String city;

    private String countryCode;

    public AddressWriteDto address(String address) {
        this.address = address;
        return this;
    }
}
