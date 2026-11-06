ALTER TABLE presences_eleves
    ADD COLUMN classe_id BIGINT REFERENCES classes (id);

ALTER TABLE presences_eleves
    ADD COLUMN plage VARCHAR(2);

ALTER TABLE presences_eleves
    ALTER COLUMN seance_id DROP NOT NULL;

ALTER TABLE presences_eleves
    DROP CONSTRAINT IF EXISTS uq_presences_eleves_seance_eleve_date;

CREATE UNIQUE INDEX IF NOT EXISTS uq_presences_eleves_classe_eleve_date_plage
    ON presences_eleves (classe_id, eleve_id, date_seance, plage)
    WHERE plage IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_presences_eleves_classe_date
    ON presences_eleves (classe_id, date_seance);
