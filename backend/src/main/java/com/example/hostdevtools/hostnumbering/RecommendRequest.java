package com.example.hostdevtools.hostnumbering;

public class RecommendRequest {
    private boolean internetGiro;
    private String prefix;

    public boolean isInternetGiro() {
        return internetGiro;
    }

    public void setInternetGiro(boolean internetGiro) {
        this.internetGiro = internetGiro;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
