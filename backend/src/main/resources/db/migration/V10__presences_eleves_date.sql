-- Assiduité : une saisie par élève, séance et date calendaire
ALTER TABLE presences_eleves
    ADD COLUMN IF NOT EXISTS date_seance DATE NOT NULL DEFAULT CURRENT_DATE;

ALTER TABLE presences_eleves
    DROP CONSTRAINT IF EXISTS presences_eleves_seance_id_eleve_id_key;

ALTER TABLE presences_eleves
    DROP CONSTRAINT IF EXISTS uq_presences_eleves_seance_eleve_date;

ALTER TABLE presences_eleves
    ADD CONSTRAINT uq_presences_eleves_seance_eleve_date
        UNIQUE (seance_id, eleve_id, date_seance);

CREATE INDEX IF NOT EXISTS idx_presences_eleves_date ON presences_eleves (date_seance);
CREATE INDEX IF NOT EXISTS idx_presences_eleves_seance_date ON presences_eleves (seance_id, date_seance);
