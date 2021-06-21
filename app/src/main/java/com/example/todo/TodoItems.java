package com.example.todo;

import com.google.firebase.Timestamp;

import java.util.Date;

public class TodoItems {
    public TodoItems(String id, String description, Boolean check, Timestamp date) {
        this.description = description;
        this.check = check;
        this.id = id;
        this.date = date;
    }

    private String description;
    private Boolean check;
    private String id;
    private Timestamp date;


    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getCheck() {
        return check;
    }

    public void setCheck(Boolean check) {
        this.check = check;
    }



}
