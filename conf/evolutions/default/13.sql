# add user type

# --- !Ups

alter table users add column password text default null;

# --- !Downs

alter table users drop column password;
