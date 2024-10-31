package com.v2ray.anamin.data.model;

import java.util.List;

public class ConfigSuccess {
    public String status;
    public User user;
    public Configs configs;

    public static class User {
        public String uuid;
        public String username;
        public String full_name;
        public int left_payment;
        public int status;
    }

    public static class Configs {
        public List<List<Xui>> xui;
        public List<List<Costume>> costume;
        public List<List<General>> general;
    }

    public static class Xui {
        public Client client;
        public String link;
    }

    public static class Costume {
        public String uuid;
        public String config;
        public int id;
    }

    public static class General {
        public String uuid;
        public String config;
        public int id;
    }

    public static class Client {
        public long expiryTime;
        public long totalGB;
        public int enable;
        public long up;
        public long down;
        public String email;
        public String flow;
        public String id;
        public int limitIp;
        public int reset;
        public String subId;
        public String tgId;
    }
}
