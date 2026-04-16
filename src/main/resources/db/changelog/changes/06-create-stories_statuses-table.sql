CREATE TABLE IF NOT EXISTS stories_statuses(
    status_id UUID REFERENCES statuses(id) ON DELETE CASCADE,
    stories_id UUID REFERENCES stories(id) ON DELETE CASCADE,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (status_id,stories_id)
);