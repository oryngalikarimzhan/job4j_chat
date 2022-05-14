package ru.job4j.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.controller.tools.FieldDataSetter;
import ru.job4j.domain.Person;
import ru.job4j.handlers.Operation;
import ru.job4j.repository.PersonRepository;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

@RestController
@RequestMapping("/users")
@Validated
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
    public ResponseEntity<Person> findByLogin(@PathVariable @NotBlank(message = "Can not be empty") String username) {
        var person = this.people.findByUsername(username);
        return new ResponseEntity<Person>(
                person.orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "User is not found."
                )),
                HttpStatus.OK
        );
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Person> findById(@PathVariable @Min(value = 1, message = "id >= 1") int id) {
        var person = this.people.findById(id);
        return new ResponseEntity<Person>(
                person.orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User is not found."
                )),
                HttpStatus.OK
        );
    }

    @PostMapping("/sign-up")
    @Validated(Operation.OnCreate.class)
    public ResponseEntity<Person> create(@Valid @RequestBody Person person) {
        if (person.getPassword() == null || person.getUsername() == null) {
            throw new NullPointerException("Username and password mustn't be empty");
        }
        person.setPassword(encoder.encode(person.getPassword()));
        return new ResponseEntity<Person>(
                this.people.save(person),
                HttpStatus.CREATED
        );
    }

    @PatchMapping("/")
    @Validated(Operation.OnUpdate.class)
    public ResponseEntity<Void> update(@Valid @RequestBody Person person)
            throws InvocationTargetException, IllegalAccessException {
        var oldPerson = people.findById(person.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Person is not found"));
        oldPerson = FieldDataSetter.setByReflection(oldPerson, person);
        oldPerson.setPassword(encoder.encode(oldPerson.getPassword()));
        people.save(oldPerson);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Min(value = 1, message = "id >= 1") int id) {
        Person person = new Person();
        person.setId(id);
        this.people.delete(person);
        return ResponseEntity.ok().build();
    }
}
