package com.zsecurity.demo.exceptions;

public class IncorrectOldPasswordException extends Exception{

    public IncorrectOldPasswordException(String msg){
        super(msg);
    }
}
