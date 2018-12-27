# add user type

# --- !Ups

alter table users add column type varchar(255);

# --- !Downs

alter table users drop column type;
