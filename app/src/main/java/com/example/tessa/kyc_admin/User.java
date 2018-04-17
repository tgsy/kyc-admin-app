package com.example.tessa.kyc_admin;

public class User {

    private String uid;
    private String email;
    private String full_name;
    private String id;
    private String date_of_birth;
    private String postal_code;
    private String image;
    private long status;

    public User() {}

    public User(String email, String uid, String full_name, String id, String date_of_birth, String postal_code, String image, long status, long token_access) {
        this.email = email;
        this.uid = uid;
        this.full_name = full_name;
        this.id = id;
        this.date_of_birth = date_of_birth;
        this.postal_code = postal_code;
        this.image = image;
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate_of_birth() {
        return date_of_birth;
    }

    public void setDate_of_birth(String date_of_birth) {
        this.date_of_birth = date_of_birth;
    }

    public String getPostal_code() {
        return postal_code;
    }

    public void setPostal_code(String postal_code) {
        this.postal_code = postal_code;
    }

    public long getStatus() {
        return status;
    }
    public void setStatus(long status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
