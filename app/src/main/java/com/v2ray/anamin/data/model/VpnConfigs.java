package com.v2ray.anamin.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class VpnConfigs {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("sconfigs")
    private List<String> sconfigs;

    @SerializedName("xconfigs")
    private List<XConfig> xconfigs;

    // Getters and Setters

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

    public List<String> getSconfigs() {
        return sconfigs;
    }

    public void setSconfigs(List<String> sconfigs) {
        this.sconfigs = sconfigs;
    }

    public List<XConfig> getXconfigs() {
        return xconfigs;
    }

    public void setXconfigs(List<XConfig> xconfigs) {
        this.xconfigs = xconfigs;
    }

    public static class XConfig {

        @SerializedName("status")
        private String status;

        @SerializedName("client")
        private Client client;

        @SerializedName("link")
        private String link;

        // Getters and Setters

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Client getClient() {
            return client;
        }

        public void setClient(Client client) {
            this.client = client;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public static class Client {

            @SerializedName("id")
            private String id;

            @SerializedName("flow")
            private String flow;

            @SerializedName("email")
            private String email;

            @SerializedName("limitIp")
            private int limitIp;

            @SerializedName("totalGB")
            private String totalGB;

            @SerializedName("expiryTime")
            private String expiryTime;

            @SerializedName("enable")
            private int enable;

            @SerializedName("tgId")
            private String tgId;

            @SerializedName("subId")
            private String subId;

            @SerializedName("reset")
            private int reset;

            @SerializedName("up")
            private String up;

            @SerializedName("down")
            private String down;

            // Getters and Setters

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getFlow() {
                return flow;
            }

            public void setFlow(String flow) {
                this.flow = flow;
            }

            public String getEmail() {
                return email;
            }

            public void setEmail(String email) {
                this.email = email;
            }

            public int getLimitIp() {
                return limitIp;
            }

            public void setLimitIp(int limitIp) {
                this.limitIp = limitIp;
            }

            public String getTotalGB() {
                return totalGB;
            }

            public void setTotalGB(String totalGB) {
                this.totalGB = totalGB;
            }

            public String getExpiryTime() {
                return expiryTime;
            }

            public void setExpiryTime(String expiryTime) {
                this.expiryTime = expiryTime;
            }

            public int getEnable() {
                return enable;
            }

            public void setEnable(int enable) {
                this.enable = enable;
            }

            public String getTgId() {
                return tgId;
            }

            public void setTgId(String tgId) {
                this.tgId = tgId;
            }

            public String getSubId() {
                return subId;
            }

            public void setSubId(String subId) {
                this.subId = subId;
            }

            public int getReset() {
                return reset;
            }

            public void setReset(int reset) {
                this.reset = reset;
            }

            public String getUp() {
                return up;
            }

            public void setUp(String up) {
                this.up = up;
            }

            public String getDown() {
                return down;
            }

            public void setDown(String down) {
                this.down = down;
            }
        }
    }
}
