package com.imjustdoom.justpermissions.data;

import lombok.Getter;

import java.util.ArrayList;

@Getter
public class PlayerJP {

    //private ArrayList<String> prefixes;
    private String prefix;

    public PlayerJP() {

    }

    public void addPrefix(String prefix) {
        this.prefix = prefix;
        //prefixes.add(prefix);
    }
}
