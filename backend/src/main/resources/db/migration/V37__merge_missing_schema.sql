-- Correctifs schéma : migrations V12–V14 non appliquées sur certaines bases (historique Flyway divergent).

-- V13 — absences par plages
ALTER TABLE presences_eleves ADD COLUMN IF NOT EXISTS classe_id BIGINT REFERENCES classes (id);
ALTER TABLE presences_eleves ADD COLUMN IF NOT EXISTS plage VARCHAR(2);

ALTER TABLE presences_eleves ALTER COLUMN seance_id DROP NOT NULL;

ALTER TABLE presences_eleves DROP CONSTRAINT IF EXISTS uq_presences_eleves_seance_eleve_date;

CREATE UNIQUE INDEX IF NOT EXISTS uq_presences_eleves_classe_eleve_date_plage
    ON presences_eleves (classe_id, eleve_id, date_seance, plage)
    WHERE plage IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_presences_eleves_classe_date
    ON presences_eleves (classe_id, date_seance);

-- V14 — maquettes par classe
CREATE TABLE IF NOT EXISTS maquettes_classe (
    id         BIGSERIAL PRIMARY KEY,
    classe_id  BIGINT NOT NULL UNIQUE REFERENCES classes (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS maquette_matieres (
    id                 BIGSERIAL PRIMARY KEY,
    maquette_classe_id BIGINT         NOT NULL REFERENCES maquettes_classe (id) ON DELETE CASCADE,
    matiere_id         BIGINT         NOT NULL REFERENCES matieres (id),
    volume_horaire     DECIMAL(5, 2)  NOT NULL DEFAULT 3.00,
    UNIQUE (maquette_classe_id, matiere_id)
);

CREATE TABLE IF NOT EXISTS maquette_chapitres (
    id                  BIGSERIAL PRIMARY KEY,
    maquette_matiere_id BIGINT       NOT NULL REFERENCES maquette_matieres (id) ON DELETE CASCADE,
    ordre               SMALLINT     NOT NULL DEFAULT 1,
    titre               VARCHAR(200) NOT NULL,
    description         TEXT
);

CREATE INDEX IF NOT EXISTS idx_maquette_matieres_classe ON maquette_matieres (maquette_classe_id);
CREATE INDEX IF NOT EXISTS idx_maquette_chapitres_matiere ON maquette_chapitres (maquette_matiere_id);
