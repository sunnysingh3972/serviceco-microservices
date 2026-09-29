CREATE INDEX idx_provider_location
    ON providers(location);

CREATE INDEX idx_provider_status
    ON providers(status);

CREATE INDEX idx_provider_skills_skill_name
    ON provider_skills(skill_name);

CREATE INDEX idx_provider_skills_provider_id
    ON provider_skills(provider_id);