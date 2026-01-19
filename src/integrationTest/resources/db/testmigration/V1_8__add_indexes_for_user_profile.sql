CREATE INDEX IF NOT EXISTS idx_user_profile_user_category
    ON user_profile (user_category);

CREATE INDEX IF NOT EXISTS idx_user_profile_audit_user_profile_id
    ON user_profile_audit (user_profile_id);
