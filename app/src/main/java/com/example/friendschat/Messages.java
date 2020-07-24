package com.example.friendschat;

public class Messages {

    private String from;
    private String message;
    private String type;
    private String to;
    private String messageid;
    private String date;
    private String time;

    public Messages() {
    }

    public Messages(String from, String message, String type, String to, String messageid, String date, String time) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.to = to;
        this.messageid = messageid;
        this.date = date;
        this.time = time;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessageid() {
        return messageid;
    }

    public void setMessageid(String messageid) {
        this.messageid = messageid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
