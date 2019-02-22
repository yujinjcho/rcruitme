# add user google token

# --- !Ups

alter table users add column google_token text default null;
alter table users add column token_expiry int default null;

# --- !Downs

alter table users drop column google_token;
alter table users drop column token_expiry;
