create table roles (
    id serial primary key,
    name varchar(255)
);

create table people (
    id serial primary key,
    username varchar(255),
    password varchar(255)
);

create table rooms (
    id serial primary key,
    name varchar(200),
    creator_person_id int references people(id),
    created timestamp,
    updated timestamp
);

create table messages (
    id serial primary key,
    text text,
    person_id int references people(id),
    room_id int references rooms(id)
);

create table rooms_people_roles (
    room_id int references rooms(id),
    person_id int references people(id),
    role_id int references roles(id)
);
