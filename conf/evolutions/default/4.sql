# jobs schema

# --- !Ups

create table jobs (
  id serial,
  role varchar(255) not null,
  company varchar(255) not null,
  location varchar(255) not null,
  salary int not null,
  compensation text,
  description text not null,
  benefits text,
  viewed boolean not null default false,
  submitted_at timestamp not null
);

# --- !Downs

drop table jobs;
