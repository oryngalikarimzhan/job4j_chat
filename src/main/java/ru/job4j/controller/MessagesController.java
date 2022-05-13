package ru.job4j.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.domain.Message;
import ru.job4j.repository.MessageRepository;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@RestController
@RequestMapping("/message")
public class MessagesController {
    private MessageRepository messages;

    public MessagesController(MessageRepository messages) {
        this.messages = messages;
    }

    @GetMapping("/")
    public List<Message> findAll() {
        return (List<Message>) this.messages.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Message> findById(@PathVariable int id) {
        var message = this.messages.findById(id);
        return new ResponseEntity<Message>(
                message.orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Message is not found."
                )),
                HttpStatus.OK
        );
    }

    @PostMapping("/")
    public ResponseEntity<Message> create(@RequestBody Message message) {
        if (message.getText() == null) {
            throw new NullPointerException("Message mustn't be empty");
        }
        return new ResponseEntity<Message>(
                this.messages.save(message),
                HttpStatus.CREATED
        );
    }

    @PatchMapping("/")
    public ResponseEntity<Void> update(@RequestBody Message message)
            throws InvocationTargetException, IllegalAccessException {
        var oldMessage = messages.findById(message.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Message is not found"));
        oldMessage = FieldDataSetter.setByReflection(oldMessage, message);
        messages.save(oldMessage);
        return ResponseEntity.ok().build();
    }

}
