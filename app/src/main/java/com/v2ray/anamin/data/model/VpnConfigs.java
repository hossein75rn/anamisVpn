
package com.v2ray.anamin.data.model;

import com.airbnb.lottie.L;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class VpnConfigs {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("leftDays")
    @Expose
    private Integer leftDays;
    @SerializedName("configs")
    @Expose
    private String configs;
    @SerializedName("gconfigs")
    @Expose
    private List<List<String>> gconfigs;
    @SerializedName("endDate")
    @Expose
    private String endDate;


    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

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

    public Integer getLeftDays() {
        return leftDays;
    }

    public void setLeftDays(Integer leftDays) {
        this.leftDays = leftDays;
    }

    public String getConfigs() {
        return configs;
    }

    public void setConfigs(String configs) {
        this.configs = configs;
    }

    public List<List<String>> getGconfigs() {
        return gconfigs;
    }

    public void setGconfigs(List<List<String>> gconfigs) {
        this.gconfigs = gconfigs;
    }


    public String getAllconfigs(){
        StringBuilder resultConfigs = new StringBuilder();
        configs = configs== null ? "":configs;
        resultConfigs.append(configs);
        for (List<String> config : getGconfigs() ){
            resultConfigs.append("\n").append(config.get(0));
        }
        return resultConfigs.toString();
    }
}

