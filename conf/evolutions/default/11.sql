# make user id primary key

# --- !Ups

alter table users add primary key (id);

# --- !Downs

alter table users drop constraint users_pkey;
