delete from user_profile where email_address like any (values('%@prdfunctestuser.com'));

commit;

