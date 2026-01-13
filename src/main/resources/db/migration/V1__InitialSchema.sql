CREATE TABLE IF NOT EXISTS "users"
(
    "ID"
                  BIGINT
        GENERATED
            BY
            DEFAULT AS
            IDENTITY
        PRIMARY
            KEY,
    "USERNAME"
                  VARCHAR
                      (
                      255
                      ) NOT NULL,
    "FIRSTNAME" VARCHAR
                      (
                      255
                      ) NOT NULL,
    "LASTNAME" VARCHAR
                      (
                      255
                      ) NOT NULL,
    "DESCRIPTION" TEXT
);

CREATE TABLE IF NOT EXISTS "article"
(
    "ID"
                BIGINT
        GENERATED
            BY
            DEFAULT AS
            IDENTITY
        PRIMARY
            KEY,
    "TITLE"
                VARCHAR
                    (
                    255
                    ) NOT NULL,
    "HEADLINE" VARCHAR
                    (
                    255
                    ) NOT NULL,
    "CONTENT" TEXT NOT NULL,
    "AUTHOR_ID" BIGINT NOT NULL,
    "SLUG" VARCHAR
                    (
                    255
                    ) NOT NULL,
    "ADDED_AT" TIMESTAMP NOT NULL,
    CONSTRAINT "FK_ARTICLE_AUTHOR"
        FOREIGN KEY
            (
             "AUTHOR_ID"
                )
            REFERENCES "users"
                (
                 "ID"
                    )
);