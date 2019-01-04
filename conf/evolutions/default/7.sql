# add credential_id to user table

# --- !Ups

alter table users add column credential_id text;

# --- !Downs

alter table users drop column credential_id;
