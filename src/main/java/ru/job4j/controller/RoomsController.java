package ru.job4j.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import ru.job4j.domain.Message;
import ru.job4j.domain.Room;
import ru.job4j.repository.RoomRepository;

import java.sql.Timestamp;
import java.util.List;

@RestController
@RequestMapping("/room")
public class RoomsController {
    @Autowired
    private RestTemplate rest;
    private RoomRepository rooms;

    private static final String MESSAGE_API = "http://localhost:8080/message/";
    private static final String PERSON_API_LOGIN = "http://localhost:8080/person/{login}";

    public RoomsController(RoomRepository rooms) {
        this.rooms = rooms;
    }

    @GetMapping("/")
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

    @PostMapping("/")
    public ResponseEntity<Room> create(@RequestBody Room room) {
        room.setCreated(new Timestamp(System.currentTimeMillis()));
        room.setUpdated(new Timestamp(System.currentTimeMillis()));
        return new ResponseEntity<Room>(
                this.rooms.save(room),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/")
    public ResponseEntity<Void> update(@RequestBody Room room) {
        room.setUpdated(new Timestamp(System.currentTimeMillis()));
        this.rooms.save(room);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        Room room = new Room();
        room.setId(id);
        this.rooms.delete(room);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/{id}")
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

