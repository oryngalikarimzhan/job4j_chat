package ru.job4j.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.domain.Message;
import ru.job4j.domain.Person;
import ru.job4j.domain.Role;
import ru.job4j.domain.Room;
import ru.job4j.repository.RoomRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.HashMap;
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
    private final ObjectMapper objectMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(RoomsController.class.getSimpleName());

    public RoomsController(RoomRepository rooms, ObjectMapper objectMapper) {
        this.rooms = rooms;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/all")
    public List<Room> findAll() {
        return (List<Room>) rooms.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> findById(@PathVariable int id) {
        var room = this.rooms.findById(id);
        return new ResponseEntity<Room>(
                room.orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Room not found"
                )),
                HttpStatus.OK
        );
    }

    @GetMapping("/all/person/id/{id}")
    public List<Room> findByCreatorId(@PathVariable int id) {
        List<Room> rsl = rooms.findRoomsByCreatorId(id);
        if (rsl == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Username not found");
        }
        return rsl;
    }

    @GetMapping("/all/person/{username}")
    public List<Room> findByCreatorId(@PathVariable String username) {
        List<Room> rsl = rooms.findRoomsByCreatorUsername(username);
        if (rsl == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Username not found");
        }
        return rsl;
    }

    @PostMapping("/new")
    public ResponseEntity<Room> create(@RequestBody Room room,
                                       HttpServletRequest request) throws URISyntaxException {
        if (room.getName() == null) {
            throw new NullPointerException("Room name field mustn't be empty");
        }
        if (room.getName().length() < 3) {
            throw new IllegalArgumentException("Name can not be les than 3 character");
        }
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
        if (room.getName() == null) {
            throw new NullPointerException("Room name field mustn't be empty");
        }
        if (room.getName().length() < 3) {
            throw new IllegalArgumentException("Name can not be less than 3 character");
        }
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
    public ResponseEntity<Message> createRoomMessage(@RequestBody Message message,
                                                     @PathVariable int id,
                                                     HttpServletRequest request) throws URISyntaxException {
        if (message.getText() == null) {
            throw new NullPointerException("Message text field mustn't be empty");
        }
        if (message.getText().length() == 0) {
            throw new IllegalArgumentException("Message should have at least one character");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(request.getHeader("Authorization"));
        Message rsl = rest.exchange(
                RequestEntity.post(
                        new URI(MESSAGE_API)
                        ).headers(headers)
                        .body(message),
                        Message.class
                ).getBody();

        Person person = rest.exchange(
                RequestEntity.get(
                        new URI(PERSON_API + SecurityContextHolder.getContext()
                                .getAuthentication()
                                .getName())
                        ).headers(headers)
                        .build(),
                        Person.class)
                .getBody();
        rsl.setPerson(person);

        Room room = this.rooms.findById(id).orElseThrow();
        if (!room.getMembers().containsKey(person)) {
            Role role = rest.exchange(
                            RequestEntity.get(
                                            new URI(ROLE_API + 2)
                                    ).headers(headers)
                                    .build(),
                            Role.class)
                    .getBody();
            room.addMember(person, role);
        }
        room.addMessage(rsl);
        this.rooms.save(room);
        return new ResponseEntity(rsl, HttpStatus.CREATED);
    }

    @ExceptionHandler(value = { IllegalArgumentException.class})
    public void exceptionHandler(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(new HashMap<>() { {
            put("message", e.getMessage());
            put("type", e.getClass());
        }}));
        LOGGER.error(e.getLocalizedMessage());
    }
}

