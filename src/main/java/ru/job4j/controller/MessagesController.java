package ru.job4j.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.controller.tools.FieldDataSetter;
import ru.job4j.domain.Message;
import ru.job4j.handlers.Operation;
import ru.job4j.repository.MessageRepository;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

@RestController
@RequestMapping("/message")
@Validated
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
    public ResponseEntity<Message> findById(@PathVariable @Min(value = 1, message = "id >= 1") int id) {
        var message = this.messages.findById(id);
        return new ResponseEntity<Message>(
                message.orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Message is not found."
                )),
                HttpStatus.OK
        );
    }

    @PostMapping("/")
    @Validated(Operation.OnCreate.class)
    public ResponseEntity<Message> create(@Valid @RequestBody Message message) {
        if (message.getText() == null) {
            throw new NullPointerException("Message mustn't be empty");
        }
        return new ResponseEntity<Message>(
                this.messages.save(message),
                HttpStatus.CREATED
        );
    }

    @PatchMapping("/")
    @Validated(Operation.OnUpdate.class)
    public ResponseEntity<Void> update(@Valid @RequestBody Message message)
            throws InvocationTargetException, IllegalAccessException {
        var oldMessage = messages.findById(message.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Message is not found"));
        oldMessage = FieldDataSetter.setByReflection(oldMessage, message);
        messages.save(oldMessage);
        return ResponseEntity.ok().build();
    }

}
