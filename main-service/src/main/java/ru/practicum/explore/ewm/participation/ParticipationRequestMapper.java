package ru.practicum.explore.ewm.participation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.explore.ewm.participation.model.ParticipationRequest;
import ru.practicum.explore.ewm.participation.dto.ParticipationRequestDto;

@Mapper(componentModel = "spring")
public interface ParticipationRequestMapper {

    @Mapping(target = "event", source = "event.id")
    @Mapping(target = "requester", source = "requester.id")
    ParticipationRequestDto toDto(ParticipationRequest participationRequest);
}
