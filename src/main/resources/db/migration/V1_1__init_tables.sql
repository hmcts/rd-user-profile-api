create schema if not exists dbuserprofile;

create table user_profile(
	id bigint not null,
	idam_id uuid,
	email_address varchar(255) not null,
	first_name varchar(255) not null,
	last_name varchar(255) not null,
	language_preference varchar(255) not null,
	email_comms_consent boolean not null default false,
	email_comms_consent_ts timestamp,
	postal_comms_consent boolean not null default false,
	postal_comms_consent_ts timestamp,
	user_category varchar(255) not null,
	user_type varchar(255) not null,
	extended_attributes json,
	idam_status varchar(255),
	created timestamp not null,
	last_updated timestamp not null,
	constraint user_profile_pk primary key (id),
	constraint email_address_uq1 unique (email_address),
	constraint idam_id_uq1 unique (idam_id)

);

create table response(
	id bigint,
	idam_registration_response integer,
	status_message varchar(1024),
	user_profile_id bigint,
	source varchar(50) not null,
	audit_ts timestamp not null,
	constraint id primary key (id)

);

create sequence user_profile_id_seq
	increment by 1
	minvalue 0
	maxvalue 2147483647
	start with 1
	cache 1
	no cycle;


create sequence response_id_seq
	increment by 1
	minvalue 0
	maxvalue 2147483647
	start with 1
	cache 1
	no cycle;

alter table response add constraint user_profile_id_fk1 foreign key (user_profile_id)
references user_profile (id);




