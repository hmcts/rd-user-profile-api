BEGIN;

DELETE FROM dbuserprofile.user_profile
WHERE email_address LIKE '%@prdfunctestuser.com'
   OR email_address LIKE '%@upfunctestuser.com'
   OR email_address LIKE 'cwr-rd-func-test-user-only%'
   OR email_address LIKE 'staff-rd-profile-func-test-user-only-%';

COMMIT;