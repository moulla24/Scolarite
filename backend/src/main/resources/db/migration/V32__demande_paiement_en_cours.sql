-- Statut intermédiaire : simulation traitement application bancaire (30 s)

ALTER TABLE demandes_paiement DROP CONSTRAINT IF EXISTS chk_demande_paiement_statut;

ALTER TABLE demandes_paiement
    ADD CONSTRAINT chk_demande_paiement_statut
    CHECK (statut IN ('EN_COURS', 'EN_ATTENTE', 'VALIDE', 'REFUSE'));
