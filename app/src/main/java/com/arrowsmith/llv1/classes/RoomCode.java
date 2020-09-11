package com.arrowsmith.llv1.classes;

import java.io.Serializable;

public class RoomCode implements Serializable {

    private String code;

    public RoomCode(){};

    public RoomCode(String code){

        if(code.length() != 5) throw new IllegalArgumentException(
                "Error: game code invalid");

        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
