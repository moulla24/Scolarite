CREATE TABLE trimestres (
    id                  BIGSERIAL PRIMARY KEY,
    annee_scolaire_id   BIGINT       NOT NULL REFERENCES annees_scolaires (id) ON DELETE CASCADE,
    numero              SMALLINT     NOT NULL CHECK (numero BETWEEN 1 AND 3),
    nom                 VARCHAR(80)  NOT NULL,
    date_debut          DATE         NOT NULL,
    date_fin            DATE         NOT NULL,
    actif               BOOLEAN      NOT NULL DEFAULT TRUE,
    UNIQUE (annee_scolaire_id, numero)
);

CREATE TABLE types_controles (
    id          BIGSERIAL PRIMARY KEY,
    ecole_id    BIGINT         NOT NULL REFERENCES ecoles (id) ON DELETE CASCADE,
    code        VARCHAR(30)    NOT NULL,
    libelle     VARCHAR(80)    NOT NULL,
    note_max    DECIMAL(5, 2)  NOT NULL DEFAULT 20.00,
    ordre       INTEGER        NOT NULL DEFAULT 0,
    actif       BOOLEAN        NOT NULL DEFAULT TRUE,
    UNIQUE (ecole_id, code)
);

INSERT INTO types_controles (ecole_id, code, libelle, note_max, ordre, actif)
SELECT 2, v.code, v.libelle, v.note_max, v.ordre, TRUE
FROM (VALUES
    ('DEVOIR', 'Devoir', 20.00, 1),
    ('COMPOSITION', 'Composition', 20.00, 2),
    ('INTERRO', 'Interrogation', 20.00, 3)
) AS v(code, libelle, note_max, ordre)
WHERE EXISTS (SELECT 1 FROM ecoles WHERE id = 2)
  AND NOT EXISTS (
    SELECT 1 FROM types_controles tc WHERE tc.ecole_id = 2 AND tc.code = v.code
  );
