package com.vsl700.nitflex.configs;

import com.vsl700.nitflex.models.Movie;
import com.vsl700.nitflex.models.User;
import com.vsl700.nitflex.models.dto.MovieDTO;
import com.vsl700.nitflex.models.dto.UserDTO;
import com.vsl700.nitflex.models.dto.UserSettingsDTO;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper(){
        var modelMapper = new ModelMapper();

        Converter<String, Boolean> STRING_TO_BOOLEAN_CONVERTER =
                mappingContext -> mappingContext.getSource() != null;

        modelMapper.createTypeMap(Movie.class, MovieDTO.class)
                .addMappings(mapping -> mapping.using(STRING_TO_BOOLEAN_CONVERTER).map(
                        Movie::getTrailerPath,
                        MovieDTO::setHasTrailer))
                .addMappings(mapping -> mapping.map(
                        src -> src.getRequester().getUsername(),
                        MovieDTO::setRequester
                ));

        modelMapper.createTypeMap(User.class, UserDTO.class)
                .addMappings(mapping -> mapping.map(
                        src -> src.getRole().getName(),
                        UserDTO::setRole
                ));

        modelMapper.createTypeMap(User.class, UserSettingsDTO.class)
                .addMappings(mapping -> mapping.map(
                        src -> src.getRole().getName(),
                        UserSettingsDTO::setRole
                ));

        return modelMapper;
    }
}
