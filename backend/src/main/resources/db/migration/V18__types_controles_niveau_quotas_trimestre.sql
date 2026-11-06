-- Types de contrôles liés au niveau (Primaire, Collège, Lycée…) et quotas par trimestre.

CREATE TABLE trimestre_types_controles (
    id                  BIGSERIAL PRIMARY KEY,
    trimestre_id        BIGINT       NOT NULL REFERENCES trimestres (id) ON DELETE CASCADE,
    type_controle_id    BIGINT       NOT NULL REFERENCES types_controles (id) ON DELETE CASCADE,
    nombre_prevu        SMALLINT     NOT NULL DEFAULT 1 CHECK (nombre_prevu >= 0),
    UNIQUE (trimestre_id, type_controle_id)
);

ALTER TABLE types_controles ADD COLUMN niveau_id BIGINT REFERENCES niveaux (id) ON DELETE CASCADE;

ALTER TABLE types_controles DROP CONSTRAINT IF EXISTS types_controles_ecole_id_code_key;

INSERT INTO types_controles (ecole_id, niveau_id, code, libelle, note_max, ordre, actif)
SELECT tc.ecole_id, n.id, tc.code, tc.libelle, tc.note_max, tc.ordre, tc.actif
FROM types_controles tc
JOIN niveaux n ON n.ecole_id = tc.ecole_id
WHERE tc.niveau_id IS NULL;

DELETE FROM types_controles WHERE niveau_id IS NULL;

ALTER TABLE types_controles DROP COLUMN ecole_id;
ALTER TABLE types_controles ALTER COLUMN niveau_id SET NOT NULL;
ALTER TABLE types_controles ADD CONSTRAINT types_controles_niveau_id_code_key UNIQUE (niveau_id, code);
