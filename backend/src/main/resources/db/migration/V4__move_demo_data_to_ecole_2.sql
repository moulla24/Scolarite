-- Supprime toutes les données liées à l'école id = 1, supprime cette école,
-- puis insère les données de démonstration sur l'école id = 2.

-- ========== Suppression école id = 1 (ordre FK) ==========

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

DELETE FROM classes
WHERE niveau_id IN (SELECT id FROM niveaux WHERE ecole_id = 1);

DELETE FROM niveaux WHERE ecole_id = 1;

DELETE FROM annees_scolaires WHERE ecole_id = 1;

DELETE FROM ecoles WHERE id = 1;

-- ========== École id = 2 + données de démonstration ==========

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

-- Réinitialise les données démo sur l'école 2 (évite les doublons si migration relancée manuellement)
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

DELETE FROM annees_scolaires WHERE ecole_id = 2;

DO $$
DECLARE
    v_annee_id   BIGINT;
    n_cp         BIGINT;
    n_ce1        BIGINT;
    n_ce2        BIGINT;
    n_cm1        BIGINT;
    n_cm2        BIGINT;
    c_cp_a       BIGINT;
    c_ce1_a      BIGINT;
    c_ce1_b      BIGINT;
    c_ce2_a      BIGINT;
    c_cm1_a      BIGINT;
    c_cm2_a      BIGINT;
    c_cm2_b      BIGINT;
    ens_fr_cp    BIGINT;
    ens_math_cp  BIGINT;
    ens_fr_ce1b  BIGINT;
    ens_math_cm2 BIGINT;
    eval_devoir  BIGINT;
    eval_compo   BIGINT;
    e1           BIGINT;
    e2           BIGINT;
    e3           BIGINT;
BEGIN
    INSERT INTO annees_scolaires (ecole_id, nom, date_debut, date_fin, active)
    VALUES (2, '2024-2025', '2024-09-01', '2025-06-30', TRUE)
    RETURNING id INTO v_annee_id;

    INSERT INTO niveaux (ecole_id, nom) VALUES (2, 'CP') RETURNING id INTO n_cp;
    INSERT INTO niveaux (ecole_id, nom) VALUES (2, 'CE1') RETURNING id INTO n_ce1;
    INSERT INTO niveaux (ecole_id, nom) VALUES (2, 'CE2') RETURNING id INTO n_ce2;
    INSERT INTO niveaux (ecole_id, nom) VALUES (2, 'CM1') RETURNING id INTO n_cm1;
    INSERT INTO niveaux (ecole_id, nom) VALUES (2, 'CM2') RETURNING id INTO n_cm2;

    INSERT INTO classes (niveau_id, nom, salle, capacite) VALUES (n_cp, 'A', 'Salle 101', 30) RETURNING id INTO c_cp_a;
    INSERT INTO classes (niveau_id, nom, salle, capacite) VALUES (n_ce1, 'A', 'Salle 102', 30) RETURNING id INTO c_ce1_a;
    INSERT INTO classes (niveau_id, nom, salle, capacite) VALUES (n_ce1, 'B', 'Salle 103', 30) RETURNING id INTO c_ce1_b;
    INSERT INTO classes (niveau_id, nom, salle, capacite) VALUES (n_ce2, 'A', 'Salle 201', 30) RETURNING id INTO c_ce2_a;
    INSERT INTO classes (niveau_id, nom, salle, capacite) VALUES (n_cm1, 'A', 'Salle 202', 28) RETURNING id INTO c_cm1_a;
    INSERT INTO classes (niveau_id, nom, salle, capacite) VALUES (n_cm2, 'A', 'Salle 203', 28) RETURNING id INTO c_cm2_a;
    INSERT INTO classes (niveau_id, nom, salle, capacite) VALUES (n_cm2, 'B', 'Salle 204', 28) RETURNING id INTO c_cm2_b;

    INSERT INTO eleves (ecole_id, matricule, nom, prenom, date_naissance, sexe, tuteur_id)
    VALUES (2, 'MAT-2024-001', 'Ould Ahmed', 'Fatimetou', '2014-03-12', 'F', 1)
    RETURNING id INTO e1;
    INSERT INTO eleves (ecole_id, matricule, nom, prenom, date_naissance, sexe, tuteur_id)
    VALUES (2, 'MAT-2024-002', 'Mint Salem', 'Aichetou', '2015-07-20', 'F', 2)
    RETURNING id INTO e2;
    INSERT INTO eleves (ecole_id, matricule, nom, prenom, date_naissance, sexe, tuteur_id)
    VALUES (2, 'MAT-2024-003', 'Ould Cheikh', 'Mohamed', '2016-01-05', 'M', 3)
    RETURNING id INTO e3;

    INSERT INTO inscriptions (eleve_id, classe_id, annee_scolaire_id, statut) VALUES
    (e1, c_cm2_a, v_annee_id, 'ACTIF'),
    (e2, c_ce1_b, v_annee_id, 'ACTIF'),
    (e3, c_cp_a, v_annee_id, 'ACTIF');

    INSERT INTO enseignements (classe_id, matiere_id, enseignant_id) VALUES
    (c_cp_a, 1, 1) RETURNING id INTO ens_fr_cp;
    INSERT INTO enseignements (classe_id, matiere_id, enseignant_id) VALUES
    (c_cp_a, 3, 2) RETURNING id INTO ens_math_cp;
    INSERT INTO enseignements (classe_id, matiere_id, enseignant_id) VALUES
    (c_ce1_b, 1, 1) RETURNING id INTO ens_fr_ce1b;
    INSERT INTO enseignements (classe_id, matiere_id, enseignant_id) VALUES
    (c_cm2_a, 3, 2) RETURNING id INTO ens_math_cm2;

    INSERT INTO evaluations (enseignement_id, type, date_evaluation, note_max) VALUES
    (ens_fr_cp, 'Devoir', '2025-10-15', 20) RETURNING id INTO eval_devoir;
    INSERT INTO evaluations (enseignement_id, type, date_evaluation, note_max) VALUES
    (ens_math_cm2, 'Composition', '2025-10-20', 20) RETURNING id INTO eval_compo;

    INSERT INTO notes (evaluation_id, eleve_id, note_obtenue) VALUES
    (eval_devoir, e1, 15.5),
    (eval_devoir, e2, 14.0),
    (eval_compo, e1, 16.0);

    INSERT INTO maquettes_pedagogiques (titre, matiere_id, niveau_id, description, format, nb_pages) VALUES
    ('Progression annuelle — Français CP', 1, n_cp, 'Objectifs trimestriels CP', 'PDF', 24),
    ('Séquence Les nombres — CE1', 3, n_ce1, '6 séances mathématiques', 'DOCX', 18);

    INSERT INTO seances_presences (classe_id, matiere_id, enseignant_id, jour_semaine, heure_debut, heure_fin, salle, type) VALUES
    (c_cp_a, 1, 1, 1, '08:00', '09:00', 'Salle 101', 'COURS'),
    (c_cp_a, 3, 2, 1, '09:00', '10:00', 'Salle 101', 'COURS'),
    (c_cm2_a, 3, 2, 2, '09:00', '10:00', 'Salle 203', 'COURS');
END $$;
