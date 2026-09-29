CREATE TABLE providers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    experience INT,
    location VARCHAR(255) NOT NULL,
    hourly_rate DECIMAL(19,2) NOT NULL,
    rating DOUBLE,
    status VARCHAR(50) NOT NULL,

    CONSTRAINT pk_providers
        PRIMARY KEY (id),

    CONSTRAINT uk_providers_email
        UNIQUE (email)
);