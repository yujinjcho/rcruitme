# connections schema

# --- !Ups

create table connections (
  id serial,
  candidate_id int references users(id),
  recruiter_id int references users(id),
  created_at timestamp not null default now()
);

# --- !Downs

drop table connections;
