package ru.job4j.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.controller.tools.FieldDataSetter;
import ru.job4j.domain.Role;
import ru.job4j.handlers.Operation;
import ru.job4j.repository.RoleRepository;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

@RestController
@RequestMapping("/role")
@Validated
public class RolesController {

    private RoleRepository roles;

    public RolesController(final RoleRepository roles) {
        this.roles = roles;
    }

    @GetMapping("/")
    public List<Role> findAll() {
        return (List<Role>) this.roles.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> findById(@PathVariable @Min(value = 1, message = "id >= 1") int id) {
        var role = this.roles.findById(id);
        return new ResponseEntity<Role>(
                role.orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Role is not found"
                )),
                HttpStatus.OK
        );
    }

    @PatchMapping("/")
    @Validated(Operation.OnUpdate.class)
    public ResponseEntity<Void> update(@Valid @RequestBody Role role)
            throws InvocationTargetException, IllegalAccessException {
        var oldRole = roles.findById(role.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Role is not found"));
        oldRole = FieldDataSetter.setByReflection(oldRole, role);
        roles.save(oldRole);
        return ResponseEntity.ok().build();
    }
}
