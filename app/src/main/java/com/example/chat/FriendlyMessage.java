package com.example.chat;

public class FriendlyMessage {
    private String text;
    private String name;
    private String photourl;
    public FriendlyMessage(){
    }

    public FriendlyMessage(String text,String name,String photourl){
        this.text=text;
        this.name=name;
        this.photourl=photourl;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhotourl(String photourl) {
        this.photourl = photourl;
    }

    public String getText() {
        return text;
    }

    public String getName() {
        return name;
    }

    public String getPhotourl() {
        return photourl;
    }
}
