package ru.job4j.domain;

import ru.job4j.handlers.Operation;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.*;

@Entity
@Table(name = "people")
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull(message = "Id must be non null", groups = {
            Operation.OnUpdate.class, Operation.OnDelete.class
    })
    @Min(value = 1, message = "id value is bigger or equal to 1", groups = {
            Operation.OnUpdate.class, Operation.OnDelete.class
    })
    private int id;
    @NotBlank(message = "Username must be not empty", groups = {
            Operation.OnCreate.class
    })
    private String username;
    @NotBlank(message = "Password must be not empty", groups = {
            Operation.OnCreate.class
    })
    private String password;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Person person = (Person) o;
        return id == person.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}