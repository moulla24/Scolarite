-- Module comptabilité complet : frais typés, remises, pénalités, échéances, dépenses, reçus

-- ── Frais scolaires enrichis ────────────────────────────────────────────────
ALTER TABLE frais_scolaires
    ADD COLUMN IF NOT EXISTS type_frais   VARCHAR(30) NOT NULL DEFAULT 'MENSUALITE',
    ADD COLUMN IF NOT EXISTS niveau_id    BIGINT REFERENCES niveaux (id),
    ADD COLUMN IF NOT EXISTS classe_id    BIGINT REFERENCES classes (id),
    ADD COLUMN IF NOT EXISTS jour_echeance INT NOT NULL DEFAULT 10;

CREATE INDEX IF NOT EXISTS idx_frais_type ON frais_scolaires (type_frais);
CREATE INDEX IF NOT EXISTS idx_frais_niveau ON frais_scolaires (niveau_id);
CREATE INDEX IF NOT EXISTS idx_frais_classe ON frais_scolaires (classe_id);

-- ── Factures : montants détaillés ───────────────────────────────────────────
ALTER TABLE factures
    ADD COLUMN IF NOT EXISTS montant_brut     DECIMAL(12, 2),
    ADD COLUMN IF NOT EXISTS montant_remise    DECIMAL(12, 2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS montant_penalite  DECIMAL(12, 2) NOT NULL DEFAULT 0;

UPDATE factures SET montant_brut = montant_total WHERE montant_brut IS NULL;

-- ── Paiements : reçu + agent collecteur ───────────────────────────────────────
ALTER TABLE paiements
    ADD COLUMN IF NOT EXISTS numero_recu  VARCHAR(30) UNIQUE,
    ADD COLUMN IF NOT EXISTS comptable_id BIGINT REFERENCES comptables (id);

CREATE INDEX IF NOT EXISTS idx_paiements_recu ON paiements (numero_recu);

CREATE TABLE IF NOT EXISTS compteur_recu (
    annee           INT PRIMARY KEY,
    dernier_numero  INT NOT NULL DEFAULT 0
);

INSERT INTO compteur_recu (annee, dernier_numero)
SELECT EXTRACT(YEAR FROM CURRENT_DATE)::INT, 0
WHERE NOT EXISTS (
    SELECT 1 FROM compteur_recu WHERE annee = EXTRACT(YEAR FROM CURRENT_DATE)::INT
);

-- ── Remises et bourses ────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS remises_eleve (
    id              BIGSERIAL PRIMARY KEY,
    eleve_id        BIGINT         NOT NULL REFERENCES eleves (id) ON DELETE CASCADE,
    type_remise     VARCHAR(30)    NOT NULL,
    libelle         VARCHAR(120),
    pourcentage     DECIMAL(5, 2),
    montant_fixe    DECIMAL(12, 2),
    actif           BOOLEAN        NOT NULL DEFAULT TRUE,
    date_debut      DATE,
    date_fin        DATE,
    date_creation   TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_remises_eleve ON remises_eleve (eleve_id, actif);

-- ── Règles de pénalité de retard ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS regles_penalite (
    id              BIGSERIAL PRIMARY KEY,
    ecole_id        BIGINT         NOT NULL REFERENCES ecoles (id),
    jours_retard_min INT           NOT NULL DEFAULT 30,
    type_penalite   VARCHAR(20)    NOT NULL DEFAULT 'FIXE',
    valeur          DECIMAL(12, 2) NOT NULL,
    actif           BOOLEAN        NOT NULL DEFAULT TRUE
);

-- ── Historique des retards ────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS historique_retards (
    id              BIGSERIAL PRIMARY KEY,
    facture_id      BIGINT         REFERENCES factures (id) ON DELETE SET NULL,
    eleve_id        BIGINT         NOT NULL REFERENCES eleves (id),
    libelle_echeance VARCHAR(120)  NOT NULL,
    date_echeance   DATE           NOT NULL,
    jours_retard    INT            NOT NULL,
    montant_penalite DECIMAL(12, 2) NOT NULL DEFAULT 0,
    date_constat    DATE           NOT NULL DEFAULT CURRENT_DATE
);

CREATE INDEX IF NOT EXISTS idx_retards_eleve ON historique_retards (eleve_id, date_constat DESC);

-- ── Échéances mensuelles ──────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS echeances_mensuelles (
    id                  BIGSERIAL PRIMARY KEY,
    eleve_id            BIGINT         NOT NULL REFERENCES eleves (id),
    annee_scolaire_id   BIGINT         NOT NULL REFERENCES annees_scolaires (id),
    mois                DATE           NOT NULL,
    type_frais          VARCHAR(30)    NOT NULL DEFAULT 'MENSUALITE',
    montant             DECIMAL(12, 2) NOT NULL,
    date_echeance       DATE           NOT NULL,
    statut              VARCHAR(20)    NOT NULL DEFAULT 'IMPAYEE',
    facture_id          BIGINT         REFERENCES factures (id) ON DELETE SET NULL,
    UNIQUE (eleve_id, mois, type_frais)
);

CREATE INDEX IF NOT EXISTS idx_echeances_eleve ON echeances_mensuelles (eleve_id, mois);

-- ── Dépenses de l'école ───────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS depenses_scolaires (
    id              BIGSERIAL PRIMARY KEY,
    ecole_id        BIGINT         NOT NULL REFERENCES ecoles (id),
    categorie       VARCHAR(30)    NOT NULL,
    date_depense    DATE           NOT NULL DEFAULT CURRENT_DATE,
    montant         DECIMAL(12, 2) NOT NULL,
    description     VARCHAR(250)   NOT NULL,
    justificatif    VARCHAR(255),
    comptable_id    BIGINT         REFERENCES comptables (id),
    ecriture_id     BIGINT         REFERENCES ecritures_comptables (id) ON DELETE SET NULL,
    date_creation   TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_depenses_categorie ON depenses_scolaires (categorie, date_depense DESC);

-- ── Données démo : frais par classe (1AF / 2AF) ─────────────────────────────
DO $$
DECLARE
    v_annee_id BIGINT;
    v_classe_1af BIGINT;
    v_classe_2af BIGINT;
BEGIN
    SELECT id INTO v_annee_id FROM annees_scolaires WHERE ecole_id = 2 AND active = TRUE LIMIT 1;
    SELECT id INTO v_classe_1af FROM classes WHERE nom = '1AF' LIMIT 1;
    SELECT id INTO v_classe_2af FROM classes WHERE nom = '2AF' LIMIT 1;

    IF v_annee_id IS NULL THEN RETURN; END IF;

    -- Règle pénalité démo : retard > 30 jours → 500 MRU
    INSERT INTO regles_penalite (ecole_id, jours_retard_min, type_penalite, valeur, actif)
    SELECT 2, 30, 'FIXE', 500.00, TRUE
    WHERE NOT EXISTS (SELECT 1 FROM regles_penalite WHERE ecole_id = 2);

    IF v_classe_1af IS NOT NULL THEN
        INSERT INTO frais_scolaires (ecole_id, annee_scolaire_id, libelle, montant, actif, type_frais, classe_id, jour_echeance)
        SELECT 2, v_annee_id, 'Inscription — 1ère année', 5000.00, TRUE, 'INSCRIPTION', v_classe_1af, 10
        WHERE NOT EXISTS (SELECT 1 FROM frais_scolaires WHERE classe_id = v_classe_1af AND type_frais = 'INSCRIPTION');

        INSERT INTO frais_scolaires (ecole_id, annee_scolaire_id, libelle, montant, actif, type_frais, classe_id, jour_echeance)
        SELECT 2, v_annee_id, 'Mensualité — 1ère année', 1500.00, TRUE, 'MENSUALITE', v_classe_1af, 10
        WHERE NOT EXISTS (SELECT 1 FROM frais_scolaires WHERE classe_id = v_classe_1af AND type_frais = 'MENSUALITE');

        INSERT INTO frais_scolaires (ecole_id, annee_scolaire_id, libelle, montant, actif, type_frais, classe_id, jour_echeance)
        SELECT 2, v_annee_id, 'Transport — 1ère année', 800.00, TRUE, 'TRANSPORT', v_classe_1af, 10
        WHERE NOT EXISTS (SELECT 1 FROM frais_scolaires WHERE classe_id = v_classe_1af AND type_frais = 'TRANSPORT');
    END IF;

    IF v_classe_2af IS NOT NULL THEN
        INSERT INTO frais_scolaires (ecole_id, annee_scolaire_id, libelle, montant, actif, type_frais, classe_id, jour_echeance)
        SELECT 2, v_annee_id, 'Inscription — 2ème année', 6000.00, TRUE, 'INSCRIPTION', v_classe_2af, 10
        WHERE NOT EXISTS (SELECT 1 FROM frais_scolaires WHERE classe_id = v_classe_2af AND type_frais = 'INSCRIPTION');

        INSERT INTO frais_scolaires (ecole_id, annee_scolaire_id, libelle, montant, actif, type_frais, classe_id, jour_echeance)
        SELECT 2, v_annee_id, 'Mensualité — 2ème année', 1800.00, TRUE, 'MENSUALITE', v_classe_2af, 10
        WHERE NOT EXISTS (SELECT 1 FROM frais_scolaires WHERE classe_id = v_classe_2af AND type_frais = 'MENSUALITE');
    END IF;
END $$;
