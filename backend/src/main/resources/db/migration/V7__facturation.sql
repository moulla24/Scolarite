-- Module facturation : frais, factures, lignes, paiements

CREATE TABLE frais_scolaires (
    id                  BIGSERIAL PRIMARY KEY,
    ecole_id            BIGINT         NOT NULL REFERENCES ecoles (id),
    annee_scolaire_id   BIGINT         NOT NULL REFERENCES annees_scolaires (id),
    libelle             VARCHAR(120)   NOT NULL,
    montant             DECIMAL(12, 2) NOT NULL,
    actif               BOOLEAN        NOT NULL DEFAULT TRUE
);

CREATE TABLE factures (
    id                  BIGSERIAL PRIMARY KEY,
    numero              VARCHAR(40)    NOT NULL UNIQUE,
    eleve_id            BIGINT         NOT NULL REFERENCES eleves (id),
    annee_scolaire_id   BIGINT         NOT NULL REFERENCES annees_scolaires (id),
    date_emission       DATE           NOT NULL DEFAULT CURRENT_DATE,
    date_echeance       DATE           NOT NULL,
    montant_total       DECIMAL(12, 2) NOT NULL,
    montant_paye        DECIMAL(12, 2) NOT NULL DEFAULT 0,
    statut              VARCHAR(20)    NOT NULL DEFAULT 'IMPAYEE'
);

CREATE TABLE lignes_facture (
    id          BIGSERIAL PRIMARY KEY,
    facture_id  BIGINT         NOT NULL REFERENCES factures (id) ON DELETE CASCADE,
    libelle     VARCHAR(120)   NOT NULL,
    montant     DECIMAL(12, 2) NOT NULL
);

CREATE TABLE paiements (
    id              BIGSERIAL PRIMARY KEY,
    facture_id      BIGINT         NOT NULL REFERENCES factures (id),
    montant         DECIMAL(12, 2) NOT NULL,
    date_paiement   DATE           NOT NULL DEFAULT CURRENT_DATE,
    mode_paiement   VARCHAR(30)    NOT NULL DEFAULT 'ESPECES',
    reference       VARCHAR(60)
);

CREATE INDEX idx_factures_eleve ON factures (eleve_id);
CREATE INDEX idx_factures_statut ON factures (statut);
CREATE INDEX idx_paiements_facture ON paiements (facture_id);
CREATE INDEX idx_paiements_date ON paiements (date_paiement DESC);

-- Données de démonstration pour l'école 2
DO $$
DECLARE
    v_annee_id BIGINT;
    v_frais_id BIGINT;
    r RECORD;
    v_facture_id BIGINT;
    idx INT := 0;
BEGIN
    SELECT id INTO v_annee_id FROM annees_scolaires WHERE ecole_id = 2 AND active = TRUE LIMIT 1;
    IF v_annee_id IS NULL THEN
        RETURN;
    END IF;

    INSERT INTO frais_scolaires (ecole_id, annee_scolaire_id, libelle, montant, actif)
    VALUES (2, v_annee_id, 'Frais scolaires — Trimestre 1', 15000.00, TRUE)
    RETURNING id INTO v_frais_id;

    FOR r IN
        SELECT i.eleve_id, i.annee_scolaire_id
        FROM inscriptions i
        JOIN eleves e ON e.id = i.eleve_id
        WHERE e.ecole_id = 2 AND i.statut = 'ACTIF'
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
