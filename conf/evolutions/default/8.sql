# add google_key and remove credential_id to users

# --- !Ups

alter table users drop column credential_id;
alter table users add column google_key text default null;

# --- !Downs

alter table users add column credential_id text default 'credentials';
alter table users drop column google_key;
