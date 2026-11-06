-- Données de test pour le flux paiement parent → comptable

DO $$
DECLARE
    v_facture_id BIGINT;
    v_tuteur_id  BIGINT;
BEGIN
    -- Codes paiement à jour sur toutes les factures
    UPDATE factures
    SET code_paiement = REPLACE(numero, 'FAC-', 'PAY-')
    WHERE code_paiement IS DISTINCT FROM REPLACE(numero, 'FAC-', 'PAY-');

    -- Nettoyer les demandes bloquantes pour retester FAC-2026-0007 (Fatimetou / parent mohamed)
    DELETE FROM demandes_paiement d
    USING factures f
    WHERE d.facture_id = f.id
      AND f.numero IN ('FAC-2026-0007', 'FAC-2026-0008', 'FAC-2026-0009');

    -- Facture partielle prête : reste 7100 MRU sur FAC-2026-0007
    UPDATE factures
    SET montant_paye = 9000.00,
        montant_total = 16100.00,
        statut = 'PARTIELLE'
    WHERE numero = 'FAC-2026-0007';

    -- Facture impayée dédiée aux tests paiement complet (élève 7, même parent)
    SELECT id INTO v_facture_id FROM factures WHERE numero = 'FAC-2026-0099';
    IF v_facture_id IS NULL THEN
        INSERT INTO factures (
            numero, code_paiement, eleve_id, annee_scolaire_id,
            date_emission, date_echeance,
            montant_brut, montant_remise, montant_penalite,
            montant_total, montant_paye, statut
        )
        SELECT
            'FAC-2026-0099',
            'PAY-2026-0099',
            7,
            f.annee_scolaire_id,
            CURRENT_DATE,
            CURRENT_DATE + 30,
            3500.00, 0, 0,
            3500.00, 0, 'IMPAYEE'
        FROM factures f
        WHERE f.numero = 'FAC-2026-0007'
        RETURNING id INTO v_facture_id;

        INSERT INTO lignes_facture (facture_id, libelle, montant) VALUES
        (v_facture_id, 'Cantine — test paiement parent', 3500.00);
    ELSE
        UPDATE factures
        SET montant_paye = 0,
            montant_total = 3500.00,
            statut = 'IMPAYEE',
            code_paiement = 'PAY-2026-0099'
        WHERE id = v_facture_id;
    END IF;

    -- Notification de bienvenue test pour le parent de l'élève 7
    SELECT t.id INTO v_tuteur_id FROM eleves e JOIN tuteurs t ON t.id = e.tuteur_id WHERE e.id = 7;
    IF v_tuteur_id IS NOT NULL THEN
        INSERT INTO notifications_parent (tuteur_id, eleve_id, type, titre, message, lu)
        SELECT v_tuteur_id, 7, 'INFO_PAIEMENT', 'Code de paiement',
               'Utilisez le code PAY affiché sur chaque facture comme référence dans votre application bancaire ou Mobile Money.',
               FALSE
        WHERE NOT EXISTS (
            SELECT 1 FROM notifications_parent
            WHERE tuteur_id = v_tuteur_id AND type = 'INFO_PAIEMENT'
              AND date_creation > CURRENT_DATE - INTERVAL '1 day'
        );
    END IF;
END $$;
