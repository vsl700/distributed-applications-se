package com.vsl700.nitflex.services;

import com.vsl700.nitflex.models.Movie;

public interface MovieTranscoderService {
    void transcode(Movie movie);
}
