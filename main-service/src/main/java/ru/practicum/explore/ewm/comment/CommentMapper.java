package ru.practicum.explore.ewm.comment;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.explore.ewm.comment.dto.CommentModerationDto;
import ru.practicum.explore.ewm.comment.model.Comment;
import ru.practicum.explore.ewm.comment.dto.CommentDto;
import ru.practicum.explore.ewm.comment.dto.NewCommentDto;
import ru.practicum.explore.ewm.comment.model.CommentModeration;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorName", source = "author.name")
    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "eventTitle", source = "event.title")
    CommentDto toDto(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "updatedOn", ignore = true)
    Comment toEntity(NewCommentDto newCommentDto);

    @Mapping(target = "commentId", source = "comment.id")
    @Mapping(target = "moderatorId", source = "moderator.id")
    @Mapping(target = "moderatorName", source = "moderator.name")
    CommentModerationDto toModerationDto(CommentModeration moderation);
}
