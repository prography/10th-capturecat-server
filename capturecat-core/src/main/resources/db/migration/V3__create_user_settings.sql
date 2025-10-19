create table if not exists user_settings
(
    user_id                        BIGINT       NOT NULL PRIMARY KEY,
    screenshot_auto_delete_enabled BOOLEAN      NOT NULL DEFAULT FALSE,
    created_date                   timestamp(6) not null,
    last_modified_date             timestamp(6) not null,
    CONSTRAINT fk_user_settings_user FOREIGN KEY (user_id) REFERENCES users (id)
);
