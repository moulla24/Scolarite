-- Données de test comptabilité : factures réalistes, paiements, reçus, remises,
-- échéances mensuelles, retards, pénalités et dépenses.

DO $$
DECLARE
    v_annee_id      BIGINT;
    v_annee_debut   DATE;
    v_annee_fin     DATE;
    v_comptable_id  BIGINT;
    v_ecole_id      BIGINT := 2;
    v_classe_1af    BIGINT;
    v_classe_2af    BIGINT;
    v_classe_3af    BIGINT;
    v_classe_5af    BIGINT;
    v_facture_id    BIGINT;
    v_paiement_id   BIGINT;
    v_montant_brut  DECIMAL(12, 2);
    v_remise        DECIMAL(12, 2);
    v_total         DECIMAL(12, 2);
    v_paye          DECIMAL(12, 2);
    v_mensualite    DECIMAL(12, 2);
    v_mois          DATE;
    v_echeance      DATE;
    v_recu          INT := 120;
    v_penalite      DECIMAL(12, 2);
    rec             RECORD;
    mois_rec        RECORD;
BEGIN
    SELECT id, date_debut, date_fin
    INTO v_annee_id, v_annee_debut, v_annee_fin
    FROM annees_scolaires
    WHERE ecole_id = v_ecole_id AND active = TRUE
    LIMIT 1;

    SELECT id INTO v_comptable_id FROM comptables LIMIT 1;
    SELECT id INTO v_classe_1af FROM classes WHERE nom = '1AF' LIMIT 1;
    SELECT id INTO v_classe_2af FROM classes WHERE nom = '2AF' LIMIT 1;
    SELECT id INTO v_classe_3af FROM classes WHERE nom = '3AF' LIMIT 1;
    SELECT id INTO v_classe_5af FROM classes WHERE nom = '5AF' LIMIT 1;

    IF v_annee_id IS NULL THEN
        RETURN;
    END IF;

    -- Données de démo liées à des élèves précis (ids 7–14) : ignorer si la base n'a pas le jeu complet.
    IF (SELECT COUNT(*) FROM eleves WHERE id IN (7, 8, 9, 10, 12, 13, 14)) < 7 THEN
        RETURN;
    END IF;

    -- Frais supplémentaires 3AF / 5AF
    IF v_classe_3af IS NOT NULL THEN
        INSERT INTO frais_scolaires (ecole_id, annee_scolaire_id, libelle, montant, actif, type_frais, classe_id, jour_echeance)
        SELECT v_ecole_id, v_annee_id, 'Inscription — 3ème année', 5500.00, TRUE, 'INSCRIPTION', v_classe_3af, 10
        WHERE NOT EXISTS (SELECT 1 FROM frais_scolaires WHERE classe_id = v_classe_3af AND type_frais = 'INSCRIPTION');

        INSERT INTO frais_scolaires (ecole_id, annee_scolaire_id, libelle, montant, actif, type_frais, classe_id, jour_echeance)
        SELECT v_ecole_id, v_annee_id, 'Mensualité — 3ème année', 1600.00, TRUE, 'MENSUALITE', v_classe_3af, 10
        WHERE NOT EXISTS (SELECT 1 FROM frais_scolaires WHERE classe_id = v_classe_3af AND type_frais = 'MENSUALITE');

        INSERT INTO frais_scolaires (ecole_id, annee_scolaire_id, libelle, montant, actif, type_frais, classe_id, jour_echeance)
        SELECT v_ecole_id, v_annee_id, 'Cantine — 3ème année', 1200.00, TRUE, 'CANTINE', v_classe_3af, 10
        WHERE NOT EXISTS (SELECT 1 FROM frais_scolaires WHERE classe_id = v_classe_3af AND type_frais = 'CANTINE');
    END IF;

    IF v_classe_5af IS NOT NULL THEN
        INSERT INTO frais_scolaires (ecole_id, annee_scolaire_id, libelle, montant, actif, type_frais, classe_id, jour_echeance)
        SELECT v_ecole_id, v_annee_id, 'Inscription — 5ème année', 6500.00, TRUE, 'INSCRIPTION', v_classe_5af, 10
        WHERE NOT EXISTS (SELECT 1 FROM frais_scolaires WHERE classe_id = v_classe_5af AND type_frais = 'INSCRIPTION');

        INSERT INTO frais_scolaires (ecole_id, annee_scolaire_id, libelle, montant, actif, type_frais, classe_id, jour_echeance)
        SELECT v_ecole_id, v_annee_id, 'Mensualité — 5ème année', 2000.00, TRUE, 'MENSUALITE', v_classe_5af, 10
        WHERE NOT EXISTS (SELECT 1 FROM frais_scolaires WHERE classe_id = v_classe_5af AND type_frais = 'MENSUALITE');
    END IF;

    -- Réinitialiser les données comptables de démo
    DELETE FROM historique_retards;
    DELETE FROM echeances_mensuelles;
    DELETE FROM notifications_comptable;
    DELETE FROM ecritures_comptables WHERE reference_type IN ('PAIEMENT', 'DEPENSE');
    DELETE FROM paiements;
    DELETE FROM lignes_facture;
    DELETE FROM factures;
    DELETE FROM remises_eleve;
    DELETE FROM depenses_scolaires WHERE ecole_id = v_ecole_id;

    -- Compteur reçu
    INSERT INTO compteur_recu (annee, dernier_numero)
    VALUES (2026, v_recu)
    ON CONFLICT (annee) DO UPDATE SET dernier_numero = GREATEST(compteur_recu.dernier_numero, v_recu);

    -- Remises / bourses
    INSERT INTO remises_eleve (eleve_id, type_remise, libelle, pourcentage, actif) VALUES
    (7,  'FAMILIALE',        'Réduction familiale — 3 enfants inscrits', 25.00, TRUE),
    (10, 'BOURSE_PARTIELLE', 'Bourse sociale partielle',                  20.00, TRUE),
    (14, 'FAMILIALE',        'Réduction fratrie',                         10.00, TRUE);

    -- ── Élève 7 — 1AF : partiel + remise 25 % ───────────────────────────────
    v_montant_brut := 20800.00;  -- 5000 + 15000 + 800
    v_remise       := 5200.00;   -- 25 %
    v_total        := 15600.00;
    v_paye         := 9000.00;
    v_mensualite   := 1500.00;

    INSERT INTO factures (numero, eleve_id, annee_scolaire_id, date_emission, date_echeance,
                          montant_brut, montant_remise, montant_penalite, montant_total, montant_paye, statut)
    VALUES ('FAC-2026-0007', 7, v_annee_id, CURRENT_DATE - 60, CURRENT_DATE + 15,
            v_montant_brut, v_remise, 500.00, 16100.00, v_paye, 'PARTIELLE')
    RETURNING id INTO v_facture_id;

    INSERT INTO lignes_facture (facture_id, libelle, montant) VALUES
    (v_facture_id, 'Inscription — 1ère année', 5000.00),
    (v_facture_id, 'Mensualité — 1ère année (10 mois)', 15000.00),
    (v_facture_id, 'Transport — 1ère année', 800.00);

    v_recu := v_recu + 1;
    INSERT INTO paiements (facture_id, montant, date_paiement, mode_paiement, reference, numero_recu, comptable_id)
    VALUES (v_facture_id, 5000.00, CURRENT_DATE - 45, 'ESPECES', 'REF-007-1', '2026-' || LPAD(v_recu::text, 5, '0'), v_comptable_id)
    RETURNING id INTO v_paiement_id;

    v_recu := v_recu + 1;
    INSERT INTO paiements (facture_id, montant, date_paiement, mode_paiement, reference, numero_recu, comptable_id)
    VALUES (v_facture_id, 4000.00, CURRENT_DATE - 20, 'MOBILE_MONEY', 'BKY-4521', '2026-' || LPAD(v_recu::text, 5, '0'), v_comptable_id);

    -- Échéances mensuelles élève 7
    FOR mois_rec IN
        SELECT gs::date AS premier_jour
        FROM generate_series(v_annee_debut, v_annee_fin, '1 month'::interval) gs
    LOOP
        v_echeance := (date_trunc('month', mois_rec.premier_jour) + interval '9 days')::date;
        INSERT INTO echeances_mensuelles (eleve_id, annee_scolaire_id, mois, type_frais, montant, date_echeance, statut, facture_id)
        VALUES (7, v_annee_id, date_trunc('month', mois_rec.premier_jour)::date, 'MENSUALITE', v_mensualite, v_echeance,
                CASE WHEN mois_rec.premier_jour <= '2024-11-01' THEN 'PAYEE' ELSE 'IMPAYEE' END,
                v_facture_id);
    END LOOP;

    INSERT INTO historique_retards (facture_id, eleve_id, libelle_echeance, date_echeance, jours_retard, montant_penalite, date_constat)
    VALUES (v_facture_id, 7, 'Mensualité Mars', '2025-03-10', 458, 500.00, CURRENT_DATE);

    -- ── Élève 9 — 1AF : totalement impayé ───────────────────────────────────
    INSERT INTO factures (numero, eleve_id, annee_scolaire_id, date_emission, date_echeance,
                          montant_brut, montant_remise, montant_penalite, montant_total, montant_paye, statut)
    VALUES ('FAC-2026-0009', 9, v_annee_id, CURRENT_DATE - 30, CURRENT_DATE - 15,
            20800.00, 0, 1500.00, 22300.00, 0, 'IMPAYEE')
    RETURNING id INTO v_facture_id;

    INSERT INTO lignes_facture (facture_id, libelle, montant) VALUES
    (v_facture_id, 'Inscription — 1ère année', 5000.00),
    (v_facture_id, 'Mensualité — 1ère année (10 mois)', 15000.00),
    (v_facture_id, 'Transport — 1ère année', 800.00);

    FOR mois_rec IN SELECT gs::date AS premier_jour FROM generate_series(v_annee_debut, v_annee_fin, '1 month'::interval) gs LOOP
        v_echeance := (date_trunc('month', mois_rec.premier_jour) + interval '9 days')::date;
        INSERT INTO echeances_mensuelles (eleve_id, annee_scolaire_id, mois, type_frais, montant, date_echeance, statut, facture_id)
        VALUES (9, v_annee_id, date_trunc('month', mois_rec.premier_jour)::date, 'MENSUALITE', 1500.00, v_echeance, 'IMPAYEE', v_facture_id);
    END LOOP;

    INSERT INTO historique_retards (facture_id, eleve_id, libelle_echeance, date_echeance, jours_retard, montant_penalite, date_constat)
    SELECT v_facture_id, 9,
           'Mensualité ' || trim(to_char(gs, 'TMMonth')),
           (date_trunc('month', gs) + interval '9 days')::date,
           (CURRENT_DATE - (date_trunc('month', gs) + interval '9 days')::date)::int,
           500.00, CURRENT_DATE
    FROM generate_series(v_annee_debut, v_annee_fin, '1 month'::interval) gs
    WHERE (date_trunc('month', gs) + interval '9 days')::date < CURRENT_DATE - 30
    LIMIT 3;

    -- ── Élève 10 — 2AF : payé (bourse 20 %) ─────────────────────────────────
    v_montant_brut := 24000.00;
    v_remise       := 4800.00;
    v_total        := 19200.00;

    INSERT INTO factures (numero, eleve_id, annee_scolaire_id, date_emission, date_echeance,
                          montant_brut, montant_remise, montant_penalite, montant_total, montant_paye, statut)
    VALUES ('FAC-2026-0010', 10, v_annee_id, CURRENT_DATE - 90, CURRENT_DATE - 60,
            v_montant_brut, v_remise, 0, v_total, v_total, 'PAYEE')
    RETURNING id INTO v_facture_id;

    INSERT INTO lignes_facture (facture_id, libelle, montant) VALUES
    (v_facture_id, 'Inscription — 2ème année', 6000.00),
    (v_facture_id, 'Mensualité — 2ème année (10 mois)', 18000.00);

    v_recu := v_recu + 1;
    INSERT INTO paiements (facture_id, montant, date_paiement, mode_paiement, reference, numero_recu, comptable_id)
    VALUES (v_facture_id, 10000.00, CURRENT_DATE - 70, 'BANQUE', 'VIR-2026-010', '2026-' || LPAD(v_recu::text, 5, '0'), v_comptable_id);

    v_recu := v_recu + 1;
    INSERT INTO paiements (facture_id, montant, date_paiement, mode_paiement, reference, numero_recu, comptable_id)
    VALUES (v_facture_id, 9200.00, CURRENT_DATE, 'ESPECES', NULL, '2026-' || LPAD(v_recu::text, 5, '0'), v_comptable_id);

    FOR mois_rec IN SELECT gs::date AS premier_jour FROM generate_series(v_annee_debut, v_annee_fin, '1 month'::interval) gs LOOP
        INSERT INTO echeances_mensuelles (eleve_id, annee_scolaire_id, mois, type_frais, montant, date_echeance, statut, facture_id)
        VALUES (10, v_annee_id, date_trunc('month', mois_rec.premier_jour)::date, 'MENSUALITE', 1800.00,
                (date_trunc('month', mois_rec.premier_jour) + interval '9 days')::date, 'PAYEE', v_facture_id);
    END LOOP;

    -- ── Élève 14 — 2AF : partiel + remise 10 % ──────────────────────────────
    v_montant_brut := 24000.00;
    v_remise       := 2400.00;
    v_total        := 21600.00;
    v_paye         := 12000.00;

    INSERT INTO factures (numero, eleve_id, annee_scolaire_id, date_emission, date_echeance,
                          montant_brut, montant_remise, montant_penalite, montant_total, montant_paye, statut)
    VALUES ('FAC-2026-0014', 14, v_annee_id, CURRENT_DATE - 45, CURRENT_DATE + 10,
            v_montant_brut, v_remise, 0, v_total, v_paye, 'PARTIELLE')
    RETURNING id INTO v_facture_id;

    INSERT INTO lignes_facture (facture_id, libelle, montant) VALUES
    (v_facture_id, 'Inscription — 2ème année', 6000.00),
    (v_facture_id, 'Mensualité — 2ème année (10 mois)', 18000.00);

    v_recu := v_recu + 1;
    INSERT INTO paiements (facture_id, montant, date_paiement, mode_paiement, reference, numero_recu, comptable_id)
    VALUES (v_facture_id, 12000.00, CURRENT_DATE - 10, 'CHEQUE', 'CHQ-8842', '2026-' || LPAD(v_recu::text, 5, '0'), v_comptable_id);

    FOR mois_rec IN SELECT gs::date AS premier_jour FROM generate_series(v_annee_debut, v_annee_fin, '1 month'::interval) gs LOOP
        INSERT INTO echeances_mensuelles (eleve_id, annee_scolaire_id, mois, type_frais, montant, date_echeance, statut, facture_id)
        VALUES (14, v_annee_id, date_trunc('month', mois_rec.premier_jour)::date, 'MENSUALITE', 1800.00,
                (date_trunc('month', mois_rec.premier_jour) + interval '9 days')::date,
                CASE WHEN mois_rec.premier_jour <= '2024-12-01' THEN 'PAYEE' ELSE 'IMPAYEE' END,
                v_facture_id);
    END LOOP;

    -- ── Élève 8 — 3AF : payé ────────────────────────────────────────────────
    v_montant_brut := 22700.00;  -- 5500 + 16000 + 1200
    INSERT INTO factures (numero, eleve_id, annee_scolaire_id, date_emission, date_echeance,
                          montant_brut, montant_remise, montant_penalite, montant_total, montant_paye, statut)
    VALUES ('FAC-2026-0008', 8, v_annee_id, CURRENT_DATE - 80, CURRENT_DATE - 50,
            v_montant_brut, 0, 0, v_montant_brut, v_montant_brut, 'PAYEE')
    RETURNING id INTO v_facture_id;

    INSERT INTO lignes_facture (facture_id, libelle, montant) VALUES
    (v_facture_id, 'Inscription — 3ème année', 5500.00),
    (v_facture_id, 'Mensualité — 3ème année (10 mois)', 16000.00),
    (v_facture_id, 'Cantine — 3ème année', 1200.00);

    v_recu := v_recu + 1;
    INSERT INTO paiements (facture_id, montant, date_paiement, mode_paiement, reference, numero_recu, comptable_id)
    VALUES (v_facture_id, 22700.00, CURRENT_DATE - 5, 'BANQUE', 'VIR-008', '2026-' || LPAD(v_recu::text, 5, '0'), v_comptable_id);

    -- ── Élève 11 — 5AF : impayé ─────────────────────────────────────────────
    v_montant_brut := 26500.00;  -- 6500 + 20000
    INSERT INTO factures (numero, eleve_id, annee_scolaire_id, date_emission, date_echeance,
                          montant_brut, montant_remise, montant_penalite, montant_total, montant_paye, statut)
    VALUES ('FAC-2026-0011', 11, v_annee_id, CURRENT_DATE - 20, CURRENT_DATE + 20,
            v_montant_brut, 0, 0, v_montant_brut, 0, 'IMPAYEE')
    RETURNING id INTO v_facture_id;

    INSERT INTO lignes_facture (facture_id, libelle, montant) VALUES
    (v_facture_id, 'Inscription — 5ème année', 6500.00),
    (v_facture_id, 'Mensualité — 5ème année (10 mois)', 20000.00);

    -- ── Élève 13 — 3AF : impayé ─────────────────────────────────────────────
    INSERT INTO factures (numero, eleve_id, annee_scolaire_id, date_emission, date_echeance,
                          montant_brut, montant_remise, montant_penalite, montant_total, montant_paye, statut)
    VALUES ('FAC-2026-0013', 13, v_annee_id, CURRENT_DATE - 15, CURRENT_DATE + 25,
            22700.00, 0, 0, 22700.00, 0, 'IMPAYEE')
    RETURNING id INTO v_facture_id;

    INSERT INTO lignes_facture (facture_id, libelle, montant) VALUES
    (v_facture_id, 'Inscription — 3ème année', 5500.00),
    (v_facture_id, 'Mensualité — 3ème année (10 mois)', 16000.00),
    (v_facture_id, 'Cantine — 3ème année', 1200.00);

    -- ── Élève 12 — 1AS : bourse totale (gratuit) ────────────────────────────
    v_montant_brut := 30000.00;
    v_remise       := 30000.00;
    INSERT INTO factures (numero, eleve_id, annee_scolaire_id, date_emission, date_echeance,
                          montant_brut, montant_remise, montant_penalite, montant_total, montant_paye, statut)
    VALUES ('FAC-2026-0012', 12, v_annee_id, CURRENT_DATE - 100, CURRENT_DATE - 70,
            v_montant_brut, v_remise, 0, 0, 0, 'PAYEE')
    RETURNING id INTO v_facture_id;

    INSERT INTO remises_eleve (eleve_id, type_remise, libelle, pourcentage, actif)
    VALUES (12, 'BOURSE_TOTALE', 'Bourse d''excellence — frais intégralement couverts', 100.00, TRUE);

    INSERT INTO lignes_facture (facture_id, libelle, montant) VALUES
    (v_facture_id, 'Frais scolaires annuels — Collège', 30000.00);

    -- Dépenses de l'école
    INSERT INTO depenses_scolaires (ecole_id, categorie, date_depense, montant, description, justificatif, comptable_id) VALUES
    (v_ecole_id, 'SALAIRES',     CURRENT_DATE - 5,  45000.00, 'Salaire enseignant Français — Mai 2026',     'BUL-ENS-05', v_comptable_id),
    (v_ecole_id, 'FOURNITURES',  CURRENT_DATE - 12,  8500.00, 'Cahiers et stylos — commande trimestre',    'FAC-FOUR-112', v_comptable_id),
    (v_ecole_id, 'ELECTRICITE',  CURRENT_DATE - 20, 12000.00, 'Facture SOMELEC — Avril 2026',              'SOMELEC-0426', v_comptable_id),
    (v_ecole_id, 'INTERNET',     CURRENT_DATE - 18,  3500.00, 'Abonnement fibre — Mai 2026',               'MATTEL-MAI', v_comptable_id),
    (v_ecole_id, 'MAINTENANCE',  CURRENT_DATE - 8,   6000.00, 'Réparation climatiseur salle 101',          'DEVIS-MAINT-08', v_comptable_id),
    (v_ecole_id, 'TRANSPORT',    CURRENT_DATE - 3,  15000.00, 'Carburant bus scolaire — Juin 2026',        'STATION-0606', v_comptable_id);

    -- Écritures comptables liées aux paiements
    INSERT INTO ecritures_comptables (date_ecriture, numero_piece, libelle, compte_debit, compte_credit, montant, reference_type, reference_id)
    SELECT p.date_paiement, 'EC-' || p.id,
           'Encaissement frais scolaires — ' || f.numero,
           '512', '706', p.montant, 'PAIEMENT', p.id
    FROM paiements p
    JOIN factures f ON f.id = p.facture_id;

    -- Notifications comptable
    INSERT INTO notifications_comptable (utilisateur_id, type, titre, message, lu, paiement_id)
    SELECT u.id, 'PAIEMENT_RECU', 'Paiement reçu',
           'Paiement de ' || p.montant || ' MRU — reçu ' || COALESCE(p.numero_recu, 'N/A'),
           FALSE, p.id
    FROM paiements p
    CROSS JOIN utilisateurs u
    WHERE u.email = 'comptable@ecoli.mr'
      AND p.date_paiement >= CURRENT_DATE - 7
    LIMIT 5;

    -- Mise à jour compteur reçu final
    UPDATE compteur_recu SET dernier_numero = v_recu WHERE annee = 2026;

END $$;
