package com.example.demo.dto;

public class ShortenRequest {
    private String url;
    private String customCode; // add this

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getCustomCode() { return customCode; }
    public void setCustomCode(String customCode) { this.customCode = customCode; }

    private String expiry; // ISO-8601 datetime string

public String getExpiry() { return expiry; }
public void setExpiry(String expiry) { this.expiry = expiry; }

}
