package com.nexustech.comicfire.domains;

public class Info {

    String Title,Info,Type,InfoId;

    public Info() {
    }

    public Info(String title, String info, String type, String infoId) {
        Title = title;
        Info = info;
        Type = type;
        InfoId = infoId;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getInfo() {
        return Info;
    }

    public void setInfo(String info) {
        Info = info;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getInfoId() {
        return InfoId;
    }

    public void setInfoId(String infoId) {
        InfoId = infoId;
    }
}
