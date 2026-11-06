-- École id = 2 uniquement : niveaux Primaire / Collège / Lycée et classes associées.

-- Nettoyage école 1 (si encore présente)
DELETE FROM presences_eleves
WHERE seance_id IN (
    SELECT sp.id FROM seances_presences sp
    JOIN classes c ON c.id = sp.classe_id
    JOIN niveaux n ON n.id = c.niveau_id
    WHERE n.ecole_id = 1
);
DELETE FROM presences_enseignants
WHERE seance_id IN (
    SELECT sp.id FROM seances_presences sp
    JOIN classes c ON c.id = sp.classe_id
    JOIN niveaux n ON n.id = c.niveau_id
    WHERE n.ecole_id = 1
);
DELETE FROM notes
WHERE evaluation_id IN (
    SELECT ev.id FROM evaluations ev
    JOIN enseignements ens ON ens.id = ev.enseignement_id
    JOIN classes c ON c.id = ens.classe_id
    JOIN niveaux n ON n.id = c.niveau_id
    WHERE n.ecole_id = 1
)
OR eleve_id IN (SELECT id FROM eleves WHERE ecole_id = 1);
DELETE FROM evaluations
WHERE enseignement_id IN (
    SELECT ens.id FROM enseignements ens
    JOIN classes c ON c.id = ens.classe_id
    JOIN niveaux n ON n.id = c.niveau_id
    WHERE n.ecole_id = 1
);
DELETE FROM enseignements
WHERE classe_id IN (
    SELECT c.id FROM classes c
    JOIN niveaux n ON n.id = c.niveau_id
    WHERE n.ecole_id = 1
);
DELETE FROM inscriptions
WHERE eleve_id IN (SELECT id FROM eleves WHERE ecole_id = 1)
   OR annee_scolaire_id IN (SELECT id FROM annees_scolaires WHERE ecole_id = 1)
   OR classe_id IN (
       SELECT c.id FROM classes c
       JOIN niveaux n ON n.id = c.niveau_id
       WHERE n.ecole_id = 1
   );
DELETE FROM maquettes_pedagogiques
WHERE niveau_id IN (SELECT id FROM niveaux WHERE ecole_id = 1);
DELETE FROM seances_presences
WHERE classe_id IN (
    SELECT c.id FROM classes c
    JOIN niveaux n ON n.id = c.niveau_id
    WHERE n.ecole_id = 1
);
DELETE FROM eleves WHERE ecole_id = 1;
DELETE FROM classes WHERE niveau_id IN (SELECT id FROM niveaux WHERE ecole_id = 1);
DELETE FROM niveaux WHERE ecole_id = 1;
DELETE FROM annees_scolaires WHERE ecole_id = 1;
DELETE FROM ecoles WHERE id = 1;

-- Nettoyage données pédagogiques école 2 (référentiel niveaux / classes)
DELETE FROM presences_eleves
WHERE seance_id IN (
    SELECT sp.id FROM seances_presences sp
    JOIN classes c ON c.id = sp.classe_id
    JOIN niveaux n ON n.id = c.niveau_id
    WHERE n.ecole_id = 2
);
DELETE FROM presences_enseignants
WHERE seance_id IN (
    SELECT sp.id FROM seances_presences sp
    JOIN classes c ON c.id = sp.classe_id
    JOIN niveaux n ON n.id = c.niveau_id
    WHERE n.ecole_id = 2
);
DELETE FROM notes
WHERE evaluation_id IN (
    SELECT ev.id FROM evaluations ev
    JOIN enseignements ens ON ens.id = ev.enseignement_id
    JOIN classes c ON c.id = ens.classe_id
    JOIN niveaux n ON n.id = c.niveau_id
    WHERE n.ecole_id = 2
)
OR eleve_id IN (SELECT id FROM eleves WHERE ecole_id = 2);
DELETE FROM evaluations
WHERE enseignement_id IN (
    SELECT ens.id FROM enseignements ens
    JOIN classes c ON c.id = ens.classe_id
    JOIN niveaux n ON n.id = c.niveau_id
    WHERE n.ecole_id = 2
);
DELETE FROM enseignements
WHERE classe_id IN (
    SELECT c.id FROM classes c
    JOIN niveaux n ON n.id = c.niveau_id
    WHERE n.ecole_id = 2
);
DELETE FROM inscriptions
WHERE eleve_id IN (SELECT id FROM eleves WHERE ecole_id = 2)
   OR annee_scolaire_id IN (SELECT id FROM annees_scolaires WHERE ecole_id = 2)
   OR classe_id IN (
       SELECT c.id FROM classes c
       JOIN niveaux n ON n.id = c.niveau_id
       WHERE n.ecole_id = 2
   );
DELETE FROM maquettes_pedagogiques
WHERE niveau_id IN (SELECT id FROM niveaux WHERE ecole_id = 2);
DELETE FROM seances_presences
WHERE classe_id IN (
    SELECT c.id FROM classes c
    JOIN niveaux n ON n.id = c.niveau_id
    WHERE n.ecole_id = 2
);
DELETE FROM eleves WHERE ecole_id = 2;
DELETE FROM classes
WHERE niveau_id IN (SELECT id FROM niveaux WHERE ecole_id = 2);
DELETE FROM niveaux WHERE ecole_id = 2;

-- École 2
INSERT INTO ecoles (id, nom, code, adresse, telephone, email)
OVERRIDING SYSTEM VALUE
VALUES (
    2,
    'École Primaire Ecoli',
    'ECOLI-02',
    'Nouakchott',
    '+222 45 00 00 00',
    'contact@ecoli.mr'
)
ON CONFLICT (id) DO UPDATE SET
    nom       = EXCLUDED.nom,
    code      = EXCLUDED.code,
    adresse   = EXCLUDED.adresse,
    telephone = EXCLUDED.telephone,
    email     = EXCLUDED.email;

SELECT setval(pg_get_serial_sequence('ecoles', 'id'), (SELECT COALESCE(MAX(id), 1) FROM ecoles));

-- Année scolaire active (conserve une seule année active pour l'école 2)
DELETE FROM annees_scolaires WHERE ecole_id = 2;

INSERT INTO annees_scolaires (ecole_id, nom, date_debut, date_fin, active)
VALUES (2, '2024-2025', '2024-09-01', '2025-06-30', TRUE);

DO $$
DECLARE
    n_primaire BIGINT;
    n_college  BIGINT;
    n_lycee    BIGINT;
BEGIN
    INSERT INTO niveaux (ecole_id, nom) VALUES (2, 'Primaire') RETURNING id INTO n_primaire;
    INSERT INTO niveaux (ecole_id, nom) VALUES (2, 'Collège') RETURNING id INTO n_college;
    INSERT INTO niveaux (ecole_id, nom) VALUES (2, 'Lycée') RETURNING id INTO n_lycee;

    -- Primaire : 1AF … 6AF
    INSERT INTO classes (niveau_id, nom, salle, capacite) VALUES
    (n_primaire, '1AF', 'Bât. A', 35),
    (n_primaire, '2AF', 'Bât. A', 35),
    (n_primaire, '3AF', 'Bât. A', 35),
    (n_primaire, '4AF', 'Bât. A', 35),
    (n_primaire, '5AF', 'Bât. A', 35),
    (n_primaire, '6AF', 'Bât. A', 35);

    -- Collège : 1AS … 4AS
    INSERT INTO classes (niveau_id, nom, salle, capacite) VALUES
    (n_college, '1AS', 'Bât. B', 40),
    (n_college, '2AS', 'Bât. B', 40),
    (n_college, '3AS', 'Bât. B', 40),
    (n_college, '4AS', 'Bât. B', 40);

    -- Lycée : 5AS, 6AS, 7C, 7A, 7O, 7D
    INSERT INTO classes (niveau_id, nom, salle, capacite) VALUES
    (n_lycee, '5AS', 'Bât. C', 40),
    (n_lycee, '6AS', 'Bât. C', 40),
    (n_lycee, '7C', 'Bât. C', 38),
    (n_lycee, '7A', 'Bât. C', 38),
    (n_lycee, '7O', 'Bât. C', 38),
    (n_lycee, '7D', 'Bât. C', 38);
END $$;
