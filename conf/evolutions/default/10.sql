# add activated to user table

# --- !Ups

alter table users add column activated boolean default false;

# --- !Downs

alter table users drop column activated;
