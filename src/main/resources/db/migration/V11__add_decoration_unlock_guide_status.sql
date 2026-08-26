ALTER TABLE user_setting
    ADD COLUMN decoration_unlock_guide_status VARCHAR(20) NOT NULL DEFAULT 'NOT_ELIGIBLE'
        AFTER membership_plan;
