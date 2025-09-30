package ru.practicum.explore.server.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.explore.dto.EndpointHitDto;
import ru.practicum.explore.server.model.EndpointHitEntity;

@Mapper(componentModel = "spring")
public interface StatsMapper {

    @Mapping(target = "id", ignore = true)
    EndpointHitEntity toEntity(EndpointHitDto dto);
}
