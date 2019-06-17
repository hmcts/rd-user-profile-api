CREATE schema if not exists dbuserprofile;

CREATE TABLE user_profile(
	id uuid NOT NULL,
	email varchar(255) NOT NULL,
	first_name varchar(255) NOT NULL,
	last_name varchar(255) NOT NULL,
	language_preference varchar(255) NOT NULL,
	email_comms_consent boolean NOT NULL DEFAULT false,
	email_comms_consent_ts timestamp,
	postal_comms_consent boolean NOT NULL DEFAULT false,
	postal_comms_consent_ts timestamp,
	user_category varchar(255) NOT NULL,
	user_type varchar(255) NOT NULL,
	extended_attributes json,
	idam_status varchar(255),
	idam_registration_response integer,
	created_ts timestamp NOT NULL,
	last_updated_ts timestamp NOT NULL,
	CONSTRAINT "user_profilePK" PRIMARY KEY (id),
	CONSTRAINT uc_email_col UNIQUE (email)

);