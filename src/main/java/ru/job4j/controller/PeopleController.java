package ru.job4j.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.domain.Person;
import ru.job4j.repository.PersonRepository;

import java.util.List;

@RestController
@RequestMapping("/person")
public class PeopleController {
    private final PersonRepository people;
    private final BCryptPasswordEncoder encoder;

    public PeopleController(PersonRepository people, BCryptPasswordEncoder encoder) {
        this.people = people;
        this.encoder = encoder;
    }

    @GetMapping("/all")
    public List<Person> findAll() {
        return (List<Person>) this.people.findAll();
    }

    @GetMapping("/{username}")
    public ResponseEntity<Person> findByLogin(@PathVariable String username) {
        var person = this.people.findByUsername(username);
        return new ResponseEntity<Person>(
                person.orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "User is not found."
                )),
                HttpStatus.OK
        );
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Person> findById(@PathVariable int id) {
        var person = this.people.findById(id);
        return new ResponseEntity<Person>(
                person.orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User is not found."
                )),
                HttpStatus.OK
        );
    }

    @PostMapping("/sign-up")
    public ResponseEntity<Person> create(@RequestBody Person person) {
        if (person.getPassword() == null || person.getUsername() == null) {
            throw new NullPointerException("Username and password mustn't be empty");
        }
        person.setPassword(encoder.encode(person.getPassword()));
        return new ResponseEntity<Person>(
                this.people.save(person),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/")
    public ResponseEntity<Void> update(@RequestBody Person person) {
        Person rsl = this.people.findByUsername(person.getUsername()).orElse(person);
        this.people.save(rsl);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        Person person = new Person();
        person.setId(id);
        this.people.delete(person);
        return ResponseEntity.ok().build();
    }
}
