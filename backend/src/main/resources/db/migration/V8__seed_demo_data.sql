-- Réinjecte les données de démonstration supprimées par V5 (élèves, notes, maquettes, emploi du temps, facturation).

DO $$
DECLARE
    v_ecole_id   BIGINT := 2;
    v_annee_id   BIGINT;
    n_primaire   BIGINT;
    n_college    BIGINT;
    c_1af        BIGINT;
    c_2af        BIGINT;
    c_3af        BIGINT;
    c_5af        BIGINT;
    c_1as        BIGINT;
    e1           BIGINT;
    e2           BIGINT;
    e3           BIGINT;
    e4           BIGINT;
    e5           BIGINT;
    e6           BIGINT;
    e7           BIGINT;
    e8           BIGINT;
    ens_fr_1af   BIGINT;
    ens_math_1af BIGINT;
    ens_fr_3af   BIGINT;
    ens_math_5af BIGINT;
    ens_fr_1as   BIGINT;
    eval_devoir  BIGINT;
    eval_compo   BIGINT;
    eval_interro BIGINT;
    v_frais_id   BIGINT;
    v_facture_id BIGINT;
    idx          INT := 0;
    r            RECORD;
BEGIN
    IF EXISTS (SELECT 1 FROM eleves WHERE ecole_id = v_ecole_id LIMIT 1) THEN
        RETURN;
    END IF;

    SELECT id INTO v_annee_id
    FROM annees_scolaires
    WHERE ecole_id = v_ecole_id AND active = TRUE
    LIMIT 1;

    IF v_annee_id IS NULL THEN
        RETURN;
    END IF;

    SELECT id INTO n_primaire FROM niveaux WHERE ecole_id = v_ecole_id AND nom = 'Primaire' LIMIT 1;
    SELECT id INTO n_college  FROM niveaux WHERE ecole_id = v_ecole_id AND nom = 'Collège' LIMIT 1;

    SELECT c.id INTO c_1af FROM classes c
    JOIN niveaux n ON n.id = c.niveau_id
    WHERE n.ecole_id = v_ecole_id AND c.nom = '1AF' LIMIT 1;

    SELECT c.id INTO c_2af FROM classes c
    JOIN niveaux n ON n.id = c.niveau_id
    WHERE n.ecole_id = v_ecole_id AND c.nom = '2AF' LIMIT 1;

    SELECT c.id INTO c_3af FROM classes c
    JOIN niveaux n ON n.id = c.niveau_id
    WHERE n.ecole_id = v_ecole_id AND c.nom = '3AF' LIMIT 1;

    SELECT c.id INTO c_5af FROM classes c
    JOIN niveaux n ON n.id = c.niveau_id
    WHERE n.ecole_id = v_ecole_id AND c.nom = '5AF' LIMIT 1;

    SELECT c.id INTO c_1as FROM classes c
    JOIN niveaux n ON n.id = c.niveau_id
    WHERE n.ecole_id = v_ecole_id AND c.nom = '1AS' LIMIT 1;

    -- Élèves
    INSERT INTO eleves (ecole_id, matricule, nom, prenom, date_naissance, sexe, tuteur_id)
    VALUES (v_ecole_id, 'MAT-2024-001', 'Ould Ahmed', 'Fatimetou', '2017-03-12', 'F', 1)
    RETURNING id INTO e1;

    INSERT INTO eleves (ecole_id, matricule, nom, prenom, date_naissance, sexe, tuteur_id)
    VALUES (v_ecole_id, 'MAT-2024-002', 'Mint Salem', 'Aichetou', '2016-07-20', 'F', 2)
    RETURNING id INTO e2;

    INSERT INTO eleves (ecole_id, matricule, nom, prenom, date_naissance, sexe, tuteur_id)
    VALUES (v_ecole_id, 'MAT-2024-003', 'Ould Cheikh', 'Mohamed', '2018-01-05', 'M', 3)
    RETURNING id INTO e3;

    INSERT INTO eleves (ecole_id, matricule, nom, prenom, date_naissance, sexe, tuteur_id)
    VALUES (v_ecole_id, 'MAT-2024-004', 'Mint Moussa', 'Khadijetou', '2017-09-14', 'F', 1)
    RETURNING id INTO e4;

    INSERT INTO eleves (ecole_id, matricule, nom, prenom, date_naissance, sexe, tuteur_id)
    VALUES (v_ecole_id, 'MAT-2024-005', 'Ould Med', 'Elhoussein', '2014-11-02', 'M', 2)
    RETURNING id INTO e5;

    INSERT INTO eleves (ecole_id, matricule, nom, prenom, date_naissance, sexe, tuteur_id)
    VALUES (v_ecole_id, 'MAT-2024-006', 'Mint Abdallahi', 'Aminetou', '2012-04-18', 'F', 3)
    RETURNING id INTO e6;

    INSERT INTO eleves (ecole_id, matricule, nom, prenom, date_naissance, sexe, tuteur_id)
    VALUES (v_ecole_id, 'MAT-2024-007', 'Ould Brahim', 'Sidi', '2016-12-30', 'M', 1)
    RETURNING id INTO e7;

    INSERT INTO eleves (ecole_id, matricule, nom, prenom, date_naissance, sexe, tuteur_id)
    VALUES (v_ecole_id, 'MAT-2024-008', 'Mint Ely', 'Mariem', '2017-06-08', 'F', 2)
    RETURNING id INTO e8;

    -- Inscriptions
    INSERT INTO inscriptions (eleve_id, classe_id, annee_scolaire_id, statut) VALUES
    (e1, c_1af, v_annee_id, 'ACTIF'),
    (e2, c_3af, v_annee_id, 'ACTIF'),
    (e3, c_1af, v_annee_id, 'ACTIF'),
    (e4, c_2af, v_annee_id, 'ACTIF'),
    (e5, c_5af, v_annee_id, 'ACTIF'),
    (e6, c_1as, v_annee_id, 'ACTIF'),
    (e7, c_3af, v_annee_id, 'ACTIF'),
    (e8, c_2af, v_annee_id, 'ACTIF');

    -- Enseignements
    INSERT INTO enseignements (classe_id, matiere_id, enseignant_id) VALUES
    (c_1af, 1, 1) RETURNING id INTO ens_fr_1af;
    INSERT INTO enseignements (classe_id, matiere_id, enseignant_id) VALUES
    (c_1af, 3, 2) RETURNING id INTO ens_math_1af;
    INSERT INTO enseignements (classe_id, matiere_id, enseignant_id) VALUES
    (c_3af, 1, 1) RETURNING id INTO ens_fr_3af;
    INSERT INTO enseignements (classe_id, matiere_id, enseignant_id) VALUES
    (c_5af, 3, 2) RETURNING id INTO ens_math_5af;
    INSERT INTO enseignements (classe_id, matiere_id, enseignant_id) VALUES
    (c_2af, 2, 1);
    INSERT INTO enseignements (classe_id, matiere_id, enseignant_id) VALUES
    (c_1as, 1, 1) RETURNING id INTO ens_fr_1as;
    INSERT INTO enseignements (classe_id, matiere_id, enseignant_id) VALUES
    (c_1as, 3, 2);

    -- Évaluations et notes
    INSERT INTO evaluations (enseignement_id, type, date_evaluation, note_max) VALUES
    (ens_fr_1af, 'Devoir', '2025-10-15', 20) RETURNING id INTO eval_devoir;
    INSERT INTO evaluations (enseignement_id, type, date_evaluation, note_max) VALUES
    (ens_math_5af, 'Composition', '2025-10-20', 20) RETURNING id INTO eval_compo;
    INSERT INTO evaluations (enseignement_id, type, date_evaluation, note_max) VALUES
    (ens_fr_3af, 'Interrogation', '2025-11-05', 10) RETURNING id INTO eval_interro;

    INSERT INTO notes (evaluation_id, eleve_id, note_obtenue) VALUES
    (eval_devoir, e1, 15.5),
    (eval_devoir, e3, 13.0),
    (eval_compo, e5, 16.0),
    (eval_interro, e2, 8.5),
    (eval_interro, e7, 9.0);

    -- Maquettes pédagogiques
    INSERT INTO maquettes_pedagogiques (titre, matiere_id, niveau_id, description, format, nb_pages) VALUES
    ('Progression annuelle — Français 1AF', 1, n_primaire, 'Objectifs trimestriels première année fondamentale', 'PDF', 24),
    ('Séquence Les nombres — 3AF', 3, n_primaire, '6 séances mathématiques sur les fractions', 'PDF', 18),
    ('Grammaire arabe — 2AF', 2, n_primaire, 'Accords et conjugaison de base', 'DOCX', 12),
    ('Sciences vivantes — 5AF', 4, n_primaire, 'Le corps humain et la nutrition', 'PDF', 20),
    ('Programme histoire-géo — 1AS', 5, n_college, 'Mauritanie et Afrique de l''Ouest', 'PDF', 32);

    -- Emploi du temps
    INSERT INTO seances_presences (classe_id, matiere_id, enseignant_id, jour_semaine, heure_debut, heure_fin, salle, type) VALUES
    (c_1af, 1, 1, 1, '08:00', '09:00', 'Bât. A', 'COURS'),
    (c_1af, 3, 2, 1, '09:00', '10:00', 'Bât. A', 'COURS'),
    (c_1af, 2, 1, 2, '08:00', '09:00', 'Bât. A', 'COURS'),
    (c_3af, 1, 1, 1, '10:00', '11:00', 'Bât. A', 'COURS'),
    (c_5af, 3, 2, 3, '09:00', '10:00', 'Bât. A', 'COURS'),
    (c_1as, 1, 1, 4, '08:00', '09:00', 'Bât. B', 'COURS'),
    (c_1as, 3, 2, 4, '09:00', '10:00', 'Bât. B', 'COURS'),
    (c_2af, 2, 1, 5, '08:00', '09:00', 'Bât. A', 'COURS');

    -- Facturation (V7 n'a rien inséré faute d'élèves inscrits)
    INSERT INTO frais_scolaires (ecole_id, annee_scolaire_id, libelle, montant, actif)
    VALUES (v_ecole_id, v_annee_id, 'Frais scolaires — Trimestre 1', 15000.00, TRUE)
    RETURNING id INTO v_frais_id;

    FOR r IN
        SELECT i.eleve_id, i.annee_scolaire_id
        FROM inscriptions i
        JOIN eleves e ON e.id = i.eleve_id
        WHERE e.ecole_id = v_ecole_id AND i.statut = 'ACTIF'
        ORDER BY i.eleve_id
    LOOP
        idx := idx + 1;
        INSERT INTO factures (numero, eleve_id, annee_scolaire_id, date_emission, date_echeance, montant_total, montant_paye, statut)
        VALUES (
            'FAC-' || to_char(CURRENT_DATE, 'YYYY') || '-' || LPAD(r.eleve_id::text, 4, '0'),
            r.eleve_id,
            r.annee_scolaire_id,
            CURRENT_DATE - 10,
            CURRENT_DATE + 20,
            15000.00,
            CASE WHEN idx % 2 = 0 THEN 15000.00 WHEN idx % 3 = 0 THEN 5000.00 ELSE 0 END,
            CASE WHEN idx % 2 = 0 THEN 'PAYEE' WHEN idx % 3 = 0 THEN 'PARTIELLE' ELSE 'IMPAYEE' END
        )
        RETURNING id INTO v_facture_id;

        INSERT INTO lignes_facture (facture_id, libelle, montant)
        VALUES (v_facture_id, 'Frais scolaires — Trimestre 1', 15000.00);

        IF idx % 2 = 0 OR idx % 3 = 0 THEN
            INSERT INTO paiements (facture_id, montant, date_paiement, mode_paiement, reference)
            VALUES (
                v_facture_id,
                CASE WHEN idx % 2 = 0 THEN 15000.00 ELSE 5000.00 END,
                CURRENT_DATE - (idx % 5),
                CASE WHEN idx % 2 = 0 THEN 'ESPECES' ELSE 'MOBILE' END,
                'PAY-' || v_facture_id
            );
        END IF;
    END LOOP;
END $$;
