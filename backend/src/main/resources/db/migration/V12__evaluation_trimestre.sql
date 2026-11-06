ALTER TABLE evaluations ADD COLUMN trimestre SMALLINT;

UPDATE evaluations SET trimestre = 1 WHERE trimestre IS NULL;

ALTER TABLE evaluations ALTER COLUMN trimestre SET NOT NULL;
ALTER TABLE evaluations ALTER COLUMN trimestre SET DEFAULT 1;

CREATE UNIQUE INDEX idx_evaluations_enseignement_type_trimestre
    ON evaluations (enseignement_id, type, trimestre);
