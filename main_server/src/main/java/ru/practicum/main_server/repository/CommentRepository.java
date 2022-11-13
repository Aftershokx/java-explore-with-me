package ru.practicum.main_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.main_server.model.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<List<Comment>> findAllByEvent_Id(long itemId);
}
