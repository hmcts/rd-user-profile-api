
ALTER TABLE response DROP CONSTRAINT user_profile_id_fk1;
alter table response add constraint user_profile_id_fk1 foreign key (user_profile_id)references user_profile (id) on delete cascade;


