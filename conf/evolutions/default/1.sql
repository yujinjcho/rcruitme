# users schema

# --- !Ups

create table users (
  id serial,
  first varchar(255) not null,
  last varchar(255) not null,
  email varchar(255) not null
);

# --- !Downs

drop table users;
