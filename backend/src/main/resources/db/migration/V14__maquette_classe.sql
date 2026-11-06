CREATE TABLE maquettes_classe (
    id         BIGSERIAL PRIMARY KEY,
    classe_id  BIGINT NOT NULL UNIQUE REFERENCES classes (id) ON DELETE CASCADE
);

CREATE TABLE maquette_matieres (
    id                 BIGSERIAL PRIMARY KEY,
    maquette_classe_id BIGINT         NOT NULL REFERENCES maquettes_classe (id) ON DELETE CASCADE,
    matiere_id         BIGINT         NOT NULL REFERENCES matieres (id),
    volume_horaire     DECIMAL(5, 2)  NOT NULL DEFAULT 3.00,
    UNIQUE (maquette_classe_id, matiere_id)
);

CREATE TABLE maquette_chapitres (
    id                  BIGSERIAL PRIMARY KEY,
    maquette_matiere_id BIGINT       NOT NULL REFERENCES maquette_matieres (id) ON DELETE CASCADE,
    ordre               SMALLINT     NOT NULL DEFAULT 1,
    titre               VARCHAR(200) NOT NULL,
    description         TEXT
);

CREATE INDEX idx_maquette_matieres_classe ON maquette_matieres (maquette_classe_id);
CREATE INDEX idx_maquette_chapitres_matiere ON maquette_chapitres (maquette_matiere_id);
