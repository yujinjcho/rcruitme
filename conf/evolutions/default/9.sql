# remove auth_token table

# --- !Ups

drop table auth_tokens;

# --- !Downs

create table auth_tokens (
    id uuid default uuid_generate_v4(),
    user_id int,
    expiry timestamp
);

