package ru.job4j.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import ru.job4j.domain.Message;
import ru.job4j.domain.Room;
import ru.job4j.repository.PersonRepository;
import ru.job4j.repository.RoleRepository;
import ru.job4j.repository.RoomRepository;

import java.sql.Timestamp;
import java.util.List;

@RestController
@RequestMapping("/room")
public class RoomsController {
    @Autowired
    private RestTemplate rest;
    private final RoomRepository rooms;
    private final RoleRepository roles;
    private final PersonRepository people;



    private static final String MESSAGE_API = "http://localhost:8080/message/";
    private static final String PERSON_API_LOGIN = "http://localhost:8080/person/";

    public RoomsController(RoomRepository rooms, PersonRepository people, RoleRepository roles) {
        this.rooms = rooms;
        this.people = people;
        this.roles = roles;
    }

    @GetMapping("/all")
    public List<Room> findAll() {
        return (List<Room>) rooms.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> findById(@PathVariable int id) {
        var room = this.rooms.findById(id);
        return new ResponseEntity<Room>(
                room.orElse(new Room()),
                room.isPresent() ? HttpStatus.OK : HttpStatus.NOT_FOUND
        );
    }

    @GetMapping("/all/person/{id}")
    public List<Room> findByCreator(@PathVariable int id) {
        return rooms.findRoomsByCreatorId(id);
    }

    @PostMapping("/new")
    public ResponseEntity<Room> create(@RequestBody Room room) {
        room.setCreated(new Timestamp(System.currentTimeMillis()));
        room.setUpdated(new Timestamp(System.currentTimeMillis()));
        room.setCreator(
                people.findByUsername(
                        SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName())
                        .orElse(null));
        return new ResponseEntity<Room>(
                this.rooms.save(room),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/")
    public ResponseEntity<Void> update(@RequestBody Room room,
                                       @RequestParam("role_id") int roleId,
                                       @RequestParam("user_id") int userId) {
        Room oldRoom = this.rooms.findById(room.getId()).orElse(room);
        oldRoom.setUpdated(new Timestamp(System.currentTimeMillis()));
        oldRoom.addMember(people.findById(userId).orElse(null), roles.findById(roleId).orElse(null));
        this.rooms.save(oldRoom);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        Room room = new Room();
        room.setId(id);
        this.rooms.delete(room);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/{id}/message/new")
    public ResponseEntity<Message> createMessage(@RequestBody Message message,
                                                 @PathVariable int id) {
        Message rsl = rest.postForObject(MESSAGE_API, message, Message.class);
        var optionalRoom = this.rooms.findById(id);
        Room room = null;
        if (optionalRoom.isPresent()) {
            room = optionalRoom.get();
            room.addMessage(rsl);
        }
        this.rooms.save(room);
        return new ResponseEntity(rsl, HttpStatus.CREATED);
    }
}

