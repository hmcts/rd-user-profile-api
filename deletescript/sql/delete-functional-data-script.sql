BEGIN;

SELECT COUNT(*) AS before_delete_records_count
FROM dbuserprofile.user_profile
WHERE email_address LIKE '%@prdfunctestuser.com'
   OR email_address LIKE '%@upfunctestuser.com'
   OR email_address LIKE 'cwr-rd-func-test-user-only%'
   OR email_address LIKE 'staff-rd-profile-func-test-user-only-%';

DELETE FROM dbuserprofile.user_profile
WHERE email_address LIKE '%@prdfunctestuser.com'
   OR email_address LIKE '%@upfunctestuser.com'
   OR email_address LIKE 'cwr-rd-func-test-user-only%'
   OR email_address LIKE 'staff-rd-profile-func-test-user-only-%';

SELECT COUNT(*) AS after_delete_records_count
FROM dbuserprofile.user_profile
WHERE email_address LIKE '%@prdfunctestuser.com'
   OR email_address LIKE '%@upfunctestuser.com'
   OR email_address LIKE 'cwr-rd-func-test-user-only%'
   OR email_address LIKE 'staff-rd-profile-func-test-user-only-%';

COMMIT;