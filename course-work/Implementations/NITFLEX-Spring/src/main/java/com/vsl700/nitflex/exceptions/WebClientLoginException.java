package com.vsl700.nitflex.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class WebClientLoginException extends RuntimeException {
    public WebClientLoginException(){
        super("Server couldn't login in the torrent website!");
    }

    public WebClientLoginException(String message){
        super(message);
    }
}
