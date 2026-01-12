CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     username VARCHAR(255) NOT NULL,
    firstname VARCHAR(255) NOT NULL,
    lastname VARCHAR(255) NOT NULL,
    description TEXT
    );

CREATE TABLE IF NOT EXISTS article (
                                       id BIGSERIAL PRIMARY KEY,
                                       title VARCHAR(255) NOT NULL,
    headline VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    author_id BIGINT NOT NULL,
    slug VARCHAR(255) NOT NULL,
    added_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_article_author
    FOREIGN KEY (author_id)
    REFERENCES users(id)
    );
