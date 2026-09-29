CREATE TABLE provider_skills (
    id BIGINT NOT NULL AUTO_INCREMENT,
    provider_id BIGINT NOT NULL,
    skill_name VARCHAR(50) NOT NULL,

    CONSTRAINT pk_provider_skills
        PRIMARY KEY (id),

    CONSTRAINT fk_provider_skills_provider
        FOREIGN KEY (provider_id)
        REFERENCES providers(id),

    CONSTRAINT uk_provider_skill
        UNIQUE (provider_id, skill_name)
);