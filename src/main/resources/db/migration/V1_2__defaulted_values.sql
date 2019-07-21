alter table user_profile alter column language_preference set default 'EN';

alter table user_profile alter column language_preference set not null;

alter table user_profile add constraint language_preference_ck check (language_preference in ('CY', 'EN'));

alter table user_profile alter column user_category set not null;

alter table user_profile add constraint user_category_ck check (user_category in ('PROFESSIONAL', 'CASEWORKER', 'JUDICIAL', 'CITIZEN'));

alter table user_profile alter column user_type set not null;

alter table user_profile add constraint user_type_ck check (user_type in ('INTERNAL', 'EXTERNAL', 'EXTERNAL_APP'));

alter table user_profile alter column idam_status set default 'PENDING';

alter table user_profile alter column idam_status set not null;

alter table user_profile add constraint idam_status_ck check (idam_status in ('PENDING', 'ACTIVE', 'BLOCKED' , 'DELETED'));