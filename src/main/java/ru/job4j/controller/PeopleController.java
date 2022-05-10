package ru.job4j.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
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
                person.orElse(new Person()),
                person.isPresent() ? HttpStatus.OK : HttpStatus.NOT_FOUND
        );
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Person> findById(@PathVariable int id) {
        var person = this.people.findById(id);
        return new ResponseEntity<Person>(
                person.orElse(new Person()),
                person.isPresent() ? HttpStatus.OK : HttpStatus.NOT_FOUND
        );
    }

    @PostMapping("/sign-up")
    public ResponseEntity<Person> create(@RequestBody Person person) {
        person.setPassword(encoder.encode(person.getPassword()));
        return new ResponseEntity<Person>(
                this.people.save(person),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/")
    public ResponseEntity<Void> update(@RequestBody Person person) {
        this.people.save(person);
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
