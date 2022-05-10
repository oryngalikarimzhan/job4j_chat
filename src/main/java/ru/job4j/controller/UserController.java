package ru.job4j.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.job4j.domain.Person;
import ru.job4j.repository.PersonRepository;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final PersonRepository users;
    private final BCryptPasswordEncoder encoder;

    public UserController(PersonRepository users, BCryptPasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @GetMapping("/all")
    public List<Person> findAll() {
        return (List<Person>) this.users.findAll();
    }

    @GetMapping("/{login}")
    public ResponseEntity<Person> findByLogin(@PathVariable String login) {
        var person = this.users.findByUsername(login);
        return new ResponseEntity<Person>(
                person.orElse(new Person()),
                person.isPresent() ? HttpStatus.OK : HttpStatus.NOT_FOUND
        );
    }

    @GetMapping("/user_by_id/{id}")
    public ResponseEntity<Person> findById(@PathVariable int id) {
        var person = this.users.findById(id);
        return new ResponseEntity<Person>(
                person.orElse(new Person()),
                person.isPresent() ? HttpStatus.OK : HttpStatus.NOT_FOUND
        );
    }

    @PostMapping("/sign-up")
    public ResponseEntity<Person> create(@RequestBody Person person) {
        person.setPassword(encoder.encode(person.getPassword()));
        return new ResponseEntity<Person>(
                this.users.save(person),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/")
    public ResponseEntity<Void> update(@RequestBody Person person) {
        this.users.save(person);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        Person person = new Person();
        person.setId(id);
        this.users.delete(person);
        return ResponseEntity.ok().build();
    }
}
