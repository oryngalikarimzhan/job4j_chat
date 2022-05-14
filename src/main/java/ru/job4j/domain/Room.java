package ru.job4j.domain;

import ru.job4j.handlers.Operation;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.sql.Timestamp;
import java.util.*;

@Entity
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull(message = "Id must be non null", groups = {
            Operation.OnUpdate.class, Operation.OnDelete.class
    })
    @Min(value = 1, message = "id value is bigger or equal to 1")
    @Positive
    private int id;
    @NotBlank(message = "Username must be not empty")
    private String name;
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "room_id")
    @Null(message = "Not allowed to set messages list by yourself")
    private List<Message> messages = new ArrayList<>();
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_person_id")
    @Null(message = "Not allowed to set creator by yourself")
    private Person creator;
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "rooms_members_roles", joinColumns = {
            @JoinColumn(name = "room_id", nullable = false, updatable = false)},
            inverseJoinColumns = {
                    @JoinColumn(name = "role_id", nullable = false, updatable = false)})
    @MapKeyJoinColumn(name = "person_id")
    @Null(message = "Not allowed to set members by yourself")
    private Map<Person, Role> members = new HashMap<>();
    @Null(message = "Not allowed to set created date-time by yourself")
    private Timestamp created;
    @Null(message = "Not allowed to set updated date-time by yourself")
    private Timestamp updated;

    public void addMessage(Message message) {
        this.messages.add(message);
    }

    public void addMember(Person person, Role role) {
        this.members.put(person, role);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public Person getCreator() {
        return creator;
    }

    public void setCreator(Person creator) {
        this.creator = creator;
    }

    public Map<Person, Role> getMembers() {
        return members;
    }

    public void setMembers(Map<Person, Role> members) {
        this.members = members;
    }

    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    public Timestamp getUpdated() {
        return updated;
    }

    public void setUpdated(Timestamp updated) {
        this.updated = updated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Room room = (Room) o;
        return id == room.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
