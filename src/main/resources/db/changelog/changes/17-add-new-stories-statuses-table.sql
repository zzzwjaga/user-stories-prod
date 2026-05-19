DROP TABLE stories_statuses;

CREATE TABLE stories_statuses(
    id UUID PRIMARY KEY,
    story_id UUID REFERENCES stories(id) ON DELETE CASCADE,
    status_id UUID REFERENCES statuses(id),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);