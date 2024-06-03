package com.vsl700.nitflex.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidTorrentException extends RuntimeException {
    public InvalidTorrentException(String message){
        super(message);
    }
}
