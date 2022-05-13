package ru.job4j.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.job4j.domain.Room;

import java.util.List;

public interface RoomRepository extends CrudRepository<Room, Integer> {
    @Query("select distinct rm "
            + "from Room rm "
            + "left join fetch rm.members "
            + "left join fetch rm.messages "
            + "left join fetch rm.creator cr "
            + "where cr.id = :creatorId")
    List<Room> findRoomsByCreatorId(@Param("creatorId") int creatorId);

    @Query("select distinct rm "
            + "from Room rm "
            + "left join fetch rm.members "
            + "left join fetch rm.messages "
            + "left join fetch rm.creator cr "
            + "where cr.username = :username")
    List<Room> findRoomsByCreatorUsername(@Param("username") String username);
}
