CREATE TABLE IF NOT EXISTS search_usage(month TEXT PRIMARY KEY, credits INTEGER NOT NULL CHECK(credits>=0 AND credits<=900));
