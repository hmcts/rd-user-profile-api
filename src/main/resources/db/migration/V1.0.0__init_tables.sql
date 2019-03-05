-- TODO create a UUID if required.
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
--     id uuid DEFAULT uuid_generate_v4 ()


CREATE TABLE user_profile
(
  ID         VARCHAR(120) NOT NULL,
  IDAM_ID    VARCHAR(120) NOT NULL,
  EMAIL      VARCHAR(120) NOT NULL,
  FIRST_NAME VARCHAR(120) NOT NULL,
  LAST_NAME  VARCHAR(120) NOT NULL,

  PRIMARY KEY (ID)
);

INSERT INTO user_profile VALUES ('1', '12345', 'joe.bloggs@somewhere.com', 'Joe', 'Bloggs');