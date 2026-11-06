-- Semaines calendaires par trimestre (lundi → vendredi).
CREATE TABLE IF NOT EXISTS semaines (
    id           BIGSERIAL PRIMARY KEY,
    trimestre_id BIGINT    NOT NULL REFERENCES trimestres (id) ON DELETE CASCADE,
    numero       SMALLINT  NOT NULL,
    date_debut   DATE      NOT NULL,
    date_fin     DATE      NOT NULL,
    UNIQUE (trimestre_id, numero)
);

CREATE INDEX IF NOT EXISTS idx_semaines_trimestre ON semaines (trimestre_id);
CREATE INDEX IF NOT EXISTS idx_semaines_dates ON semaines (date_debut, date_fin);

-- Génération des semaines pour chaque trimestre existant (si aucune semaine).
DO $$
DECLARE
    t RECORD;
    monday DATE;
    friday DATE;
    week_num INT;
BEGIN
    IF EXISTS (SELECT 1 FROM semaines LIMIT 1) THEN
        RETURN;
    END IF;

    FOR t IN SELECT id, date_debut, date_fin FROM trimestres ORDER BY id LOOP
        week_num := 1;
        monday := t.date_debut - (EXTRACT(ISODOW FROM t.date_debut)::INT - 1);
        WHILE monday <= t.date_fin LOOP
            friday := monday + 4;
            IF friday >= t.date_debut AND monday <= t.date_fin THEN
                INSERT INTO semaines (trimestre_id, numero, date_debut, date_fin)
                VALUES (t.id, week_num, monday, friday);
                week_num := week_num + 1;
            END IF;
            monday := monday + 7;
        END LOOP;
    END LOOP;
END $$;

ALTER TABLE seances_presences ADD COLUMN IF NOT EXISTS semaine_id BIGINT REFERENCES semaines (id);

-- Affecter les séances existantes à la semaine courante du trimestre courant.
UPDATE seances_presences sp
SET semaine_id = sub.id
FROM (
    SELECT s.id
    FROM semaines s
    JOIN trimestres tr ON tr.id = s.trimestre_id
    WHERE CURRENT_DATE BETWEEN tr.date_debut AND tr.date_fin
      AND CURRENT_DATE BETWEEN s.date_debut AND s.date_fin
    ORDER BY s.id
    LIMIT 1
) sub
WHERE sp.semaine_id IS NULL;

UPDATE seances_presences
SET semaine_id = (SELECT id FROM semaines ORDER BY id LIMIT 1)
WHERE semaine_id IS NULL;

ALTER TABLE seances_presences ALTER COLUMN semaine_id SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_seance_creneau_semaine
    ON seances_presences (semaine_id, classe_id, jour_semaine, heure_debut);
