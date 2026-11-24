alter table user_profile drop constraint idam_status_ck;

alter table user_profile add constraint idam_status_ck check (idam_status in ('PENDING', 'ACTIVE', 'SUSPENDED' , 'ACTIVE_AND_LOCKED' , 'SUSPENDED_AND_LOCKED', 'DELETED'));