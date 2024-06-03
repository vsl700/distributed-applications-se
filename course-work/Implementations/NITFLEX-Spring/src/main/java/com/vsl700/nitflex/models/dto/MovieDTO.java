package com.vsl700.nitflex.models.dto;

import com.vsl700.nitflex.models.Movie;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MovieDTO {
    private String id;
    private String name;
    private Movie.MovieType type;
    private boolean hasTrailer;
    private String requester;
}