package ru.job4j.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.job4j.domain.Person;
import ru.job4j.repository.PersonRepository;

import java.util.List;

@RestController
@RequestMapping("/person")
public class PeopleController {
    private PersonRepository people;

    public PeopleController(final PersonRepository people) {
        this.people = people;
    }

    @GetMapping("/")
    public List<Person> findAll() {
        return (List<Person>) this.people.findAll();
    }

    @GetMapping("/{login}")
    public ResponseEntity<Person> findByLogin(@PathVariable String login) {
        var person = this.people.findByLogin(login);
        return new ResponseEntity<Person>(
                person.orElse(new Person()),
                person.isPresent() ? HttpStatus.OK : HttpStatus.NOT_FOUND
        );
    }

    @GetMapping("/user_by_id/{id}")
    public ResponseEntity<Person> findById(@PathVariable int id) {
        var person = this.people.findById(id);
        return new ResponseEntity<Person>(
                person.orElse(new Person()),
                person.isPresent() ? HttpStatus.OK : HttpStatus.NOT_FOUND
        );
    }

    @PostMapping("/")
    public ResponseEntity<Person> create(@RequestBody Person person) {
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
