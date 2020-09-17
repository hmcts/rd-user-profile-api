delete from user_profile
where email_address like any (values('%@prdfunctestuser.com'), ('%@mailinator.com'));

commit;