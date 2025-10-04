package ru.practicum.explore.event;

import org.mapstruct.*;
import ru.practicum.explore.event.model.Event;
import ru.practicum.explore.event.dto.request.NewEventDto;
import ru.practicum.explore.event.dto.request.UpdateEventAdminRequest;
import ru.practicum.explore.event.dto.request.UpdateEventUserRequest;
import ru.practicum.explore.event.dto.response.EventFullDto;
import ru.practicum.explore.event.dto.response.EventShortDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EventMapper {

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "views", ignore = true)
    Event toEntity(NewEventDto newEventDto);

    EventFullDto toFullDto(Event event);

    EventShortDto toShortDto(Event event);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "views", ignore = true)
    void updateEventFromUserRequest(UpdateEventUserRequest updateEventUserRequest,
                                    @MappingTarget Event event);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "views", ignore = true)
    void updateEventFromAdminRequest(UpdateEventAdminRequest updateEventAdminRequest,
                                     @MappingTarget Event event);
}
