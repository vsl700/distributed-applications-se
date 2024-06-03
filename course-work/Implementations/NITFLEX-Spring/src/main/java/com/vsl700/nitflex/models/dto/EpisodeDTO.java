package com.vsl700.nitflex.models.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EpisodeDTO {
    private String id;
    private int seasonNumber;
    private int episodeNumber;
}
