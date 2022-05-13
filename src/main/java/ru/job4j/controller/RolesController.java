package ru.job4j.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.domain.Role;
import ru.job4j.repository.RoleRepository;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@RestController
@RequestMapping("/role")
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
    public ResponseEntity<Role> findById(@PathVariable int id) {
        var role = this.roles.findById(id);
        return new ResponseEntity<Role>(
                role.orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Role is not found"
                )),
                HttpStatus.OK
        );
    }

    @PatchMapping("/")
    public ResponseEntity<Void> update(@RequestBody Role role)
            throws InvocationTargetException, IllegalAccessException {
        var oldRole = roles.findById(role.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Role is not found"));
        oldRole = FieldDataSetter.setByReflection(oldRole, role);
        roles.save(oldRole);
        return ResponseEntity.ok().build();
    }
}
