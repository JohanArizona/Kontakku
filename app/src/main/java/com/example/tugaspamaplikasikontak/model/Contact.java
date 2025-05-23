package com.example.tugaspamaplikasikontak.model;

public class Contact {
    private String id;
    private String name;
    private String phone;
    private String instagram;

    public Contact() {
    }

    public Contact(String id, String name, String phone, String instagram) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.instagram = instagram;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getInstagram() {
        return instagram;
    }

    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }
}