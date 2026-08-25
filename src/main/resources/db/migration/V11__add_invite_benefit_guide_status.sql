ALTER TABLE user_setting
    ADD COLUMN invite_benefit_guide_status VARCHAR(20) NOT NULL DEFAULT ''NOT_ELIGIBLE''
        AFTER membership_plan;
