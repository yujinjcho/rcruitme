# auth token table

# --- !Ups

create table auth_tokens (
    id uuid default uuid_generate_v4(),
    user_id int,
    expiry timestamp
);

# --- !Downs

drop table auth_tokens;
