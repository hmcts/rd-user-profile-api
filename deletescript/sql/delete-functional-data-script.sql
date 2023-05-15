delete from user_profile where
        email_address like any (values('%@prdfunctestuser.com'))
       or email_address like any (values('cwr-rd-func-test-user-only%'))
       or email_address like any(values('staff-rd-profile-func-test-user-only-%'));

commit;

