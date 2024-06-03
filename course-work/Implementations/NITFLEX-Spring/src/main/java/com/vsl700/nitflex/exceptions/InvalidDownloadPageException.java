package com.vsl700.nitflex.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidDownloadPageException extends RuntimeException {
    public InvalidDownloadPageException(){
        super("The provided download page is not valid!");
    }

    public InvalidDownloadPageException(String message){
        super(message);
    }
}
