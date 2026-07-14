package com.zsecurity.demo.exceptions;

public class UserNotYetLoggedInException extends Exception{
    public UserNotYetLoggedInException(String msg){
        super(msg);
    }
}
