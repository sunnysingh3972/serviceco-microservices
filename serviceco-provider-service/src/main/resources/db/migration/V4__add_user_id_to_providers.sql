ALTER TABLE providers
ADD COLUMN user_id BIGINT NULL;

CREATE UNIQUE INDEX uk_provider_user
ON providers(user_id);