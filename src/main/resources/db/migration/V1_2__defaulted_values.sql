alter table user_profile drop column language_preference;

alter table user_profile add column language_preference varchar(255) not null default 'EN';

alter table user_profile add constraint language_preference_check check (language_preference in ('CY', 'EN'));


alter table user_profile drop column user_category;

alter table user_profile add column user_category varchar(255) not null;

alter table user_profile add constraint user_category_check check (user_category in ('PROFESSIONAL', 'CASEWORKER', 'JUDICIAL', 'CITIZEN'));


alter table user_profile drop column user_type;

alter table user_profile add column user_type varchar(255) not null;

alter table user_profile add constraint user_type_check check (user_type in ('INTERNAL', 'EXTERNAL', 'EXTERNAL_APP'));


alter table user_profile drop column idam_status;

alter table user_profile add column idam_status varchar(255) default 'PENDING';

alter table user_profile add constraint idam_status_check check (idam_status in ('PENDING', 'ACTIVE', 'BLOCKED' , 'DELETED'));