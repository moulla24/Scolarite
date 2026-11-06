-- Correctif : colonne trimestre sur evaluations (si V12 n'a pas été appliquée sur cette base).
ALTER TABLE evaluations ADD COLUMN IF NOT EXISTS trimestre SMALLINT;

UPDATE evaluations SET trimestre = 1 WHERE trimestre IS NULL;

ALTER TABLE evaluations ALTER COLUMN trimestre SET NOT NULL;
ALTER TABLE evaluations ALTER COLUMN trimestre SET DEFAULT 1;

CREATE UNIQUE INDEX IF NOT EXISTS idx_evaluations_enseignement_type_trimestre
    ON evaluations (enseignement_id, type, trimestre);
