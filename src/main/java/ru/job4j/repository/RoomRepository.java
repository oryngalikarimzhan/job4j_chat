package ru.job4j.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.job4j.domain.Room;

import java.util.List;

public interface RoomRepository extends CrudRepository<Room, Integer> {
    @Query("select distinct rm "
            + "from Room rm "
            + "join fetch rm.members "
            + "join fetch rm.messages "
            + "join fetch rm.creator cr "
            + "where cr.id = :creatorId")
    List<Room> findRoomsByCreatorId(@Param("creatorId") int creatorId);
}
