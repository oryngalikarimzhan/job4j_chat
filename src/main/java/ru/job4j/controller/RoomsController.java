package ru.job4j.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import ru.job4j.domain.Message;
import ru.job4j.domain.Person;
import ru.job4j.domain.Role;
import ru.job4j.domain.Room;
import ru.job4j.repository.RoomRepository;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.List;

@RestController
@RequestMapping("/room")
public class RoomsController {
    @Autowired
    private RestTemplate rest;
    private final RoomRepository rooms;
    private static final String MESSAGE_API = "http://localhost:8080/message/";
    private static final String PERSON_API = "http://localhost:8080/person/";
    private static final String ROLE_API = "http://localhost:8080/role/";

    public RoomsController(RoomRepository rooms) {
        this.rooms = rooms;
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
    public ResponseEntity<Room> create(@RequestBody Room room,
                                       HttpServletRequest request) throws URISyntaxException {
        room.setCreated(new Timestamp(System.currentTimeMillis()));
        room.setUpdated(new Timestamp(System.currentTimeMillis()));
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(request.getHeader("Authorization"));
        Person person = rest.exchange(
                RequestEntity.get(
                        new URI(PERSON_API + SecurityContextHolder.getContext()
                                .getAuthentication()
                                .getName())
                        ).headers(headers)
                        .build(),
                        Person.class)
                .getBody();
        room.setCreator(person);
        return new ResponseEntity<Room>(
                this.rooms.save(room),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/")
    public ResponseEntity<Void> update(@RequestBody Room room,
                                       HttpServletRequest request) throws URISyntaxException {
        Room oldRoom = this.rooms.findById(room.getId()).orElse(room);
        oldRoom.setUpdated(new Timestamp(System.currentTimeMillis()));
        oldRoom.setName(room.getName());
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(request.getHeader("Authorization"));
        Person person = rest.exchange(
                        RequestEntity.get(
                                        new URI(PERSON_API + request.getParameter("username"))
                                ).headers(headers)
                                .build(),
                        Person.class)
                .getBody();
        Role role = rest.exchange(
                        RequestEntity.get(
                                        new URI(ROLE_API + request.getParameter("role-id"))
                                ).headers(headers)
                                .build(),
                        Role.class)
                .getBody();
        oldRoom.addMember(person, role);
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
                                                 @PathVariable int id,
                                                 HttpServletRequest request) throws URISyntaxException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(request.getHeader("Authorization"));
        Message rsl1 = rest.exchange(RequestEntity.post(new URI(MESSAGE_API) + "new").headers(headers).body(message), Message.class).getBody();

        Person person = rest.exchange(
                        RequestEntity.get(
                                        new URI(PERSON_API + SecurityContextHolder.getContext()
                                                .getAuthentication()
                                                .getName())
                                ).headers(headers)
                                .build(),
                        Person.class)
                .getBody();
        rsl1.setPerson(person);
        var optionalRoom = this.rooms.findById(id);
        Room room = null;
        if (optionalRoom.isPresent()) {
            room = optionalRoom.get();
            room.addMessage(rsl1);
        }
        this.rooms.save(room);
        return new ResponseEntity(rsl1, HttpStatus.CREATED);
    }
}

