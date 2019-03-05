# add user ids to jobs

# --- !Ups

alter table jobs add column candidate_id int references users(id);
alter table jobs add column recruiter_id int references users(id);

# --- !Downs

alter table jobs drop column candidate_id;
alter table jobs drop column recruiter_id;
