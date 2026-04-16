CREATE TABLE IF NOT EXISTS INVESTresults
(
    stories_id UUID REFERENCES stories(id) ON DELETE CASCADE,
    checked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    independent_score INT CHECK (independent_score >= 0 AND independent_score <=10) DEFAULT 0,
    negotiable_score INT CHECK (negotiable_score >= 0 AND negotiable_score <=10) DEFAULT 0,
    valuable_score INT CHECK (valuable_score >= 0 AND valuable_score <=10) DEFAULT 0,
    estimable_score INT CHECK (estimable_score >= 0 AND estimable_score <=10) DEFAULT 0,
    small_score INT CHECK (small_score >= 0 AND small_score <=10) DEFAULT 0,
    testable_score INT CHECK (testable_score >= 0 AND testable_score <=10) DEFAULT 0,
    issues TEXT,
    suggestions TEXT,
    PRIMARY KEY(stories_id ,checked_at)
);