package com.argctech.core.users.utils.exceptions;

public class PasswordDontMatchException extends Exception{
    public PasswordDontMatchException(String message) {
        super(message);
    }
}
