# make user type not nullable

# --- !Ups

alter table users alter column type set not null;

# --- !Downs

alter table users alter column type drop not null;
