-- Demandes de paiement parent → validation comptable

CREATE TABLE IF NOT EXISTS demandes_paiement (
    id                BIGSERIAL PRIMARY KEY,
    facture_id        BIGINT         NOT NULL REFERENCES factures (id) ON DELETE CASCADE,
    tuteur_id         BIGINT         NOT NULL REFERENCES tuteurs (id) ON DELETE CASCADE,
    montant           DECIMAL(12, 2) NOT NULL,
    date_paiement     DATE           NOT NULL DEFAULT CURRENT_DATE,
    mode_paiement     VARCHAR(30)    NOT NULL DEFAULT 'ESPECES',
    reference         VARCHAR(120),
    preuve_reference  VARCHAR(255),
    statut            VARCHAR(20)    NOT NULL DEFAULT 'EN_ATTENTE',
    motif_refus       TEXT,
    paiement_id       BIGINT         REFERENCES paiements (id) ON DELETE SET NULL,
    comptable_id      BIGINT         REFERENCES comptables (id) ON DELETE SET NULL,
    date_creation     TIMESTAMP      NOT NULL DEFAULT NOW(),
    date_traitement   TIMESTAMP,
    CONSTRAINT chk_demande_paiement_statut CHECK (statut IN ('EN_ATTENTE', 'VALIDE', 'REFUSE'))
);

CREATE INDEX IF NOT EXISTS idx_demandes_paiement_statut ON demandes_paiement (statut, date_creation DESC);
CREATE INDEX IF NOT EXISTS idx_demandes_paiement_tuteur ON demandes_paiement (tuteur_id, date_creation DESC);
CREATE INDEX IF NOT EXISTS idx_demandes_paiement_facture ON demandes_paiement (facture_id);

ALTER TABLE notifications_comptable
    ADD COLUMN IF NOT EXISTS demande_paiement_id BIGINT REFERENCES demandes_paiement (id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_notifications_demande ON notifications_comptable (demande_paiement_id);
