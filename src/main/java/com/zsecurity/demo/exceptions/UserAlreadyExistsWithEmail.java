package com.zsecurity.demo.exceptions;

public class UserAlreadyExistsWithEmail extends Exception{
    public UserAlreadyExistsWithEmail(String msg){
        super(msg);
    }
}
