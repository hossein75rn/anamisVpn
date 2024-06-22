package com.v2ray.anamin.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Login {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("fullName")
    @Expose
    private String fullName;
    @SerializedName("leftPayments")
    @Expose
    private Integer leftPayments;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Integer getLeftPayments() {
        return leftPayments;
    }

    public void setLeftPayments(Integer leftPayments) {
        this.leftPayments = leftPayments;
    }

}