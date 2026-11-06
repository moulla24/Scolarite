-- Module comptable : comptes, journal comptable, notifications

CREATE TABLE comptables (
    id              BIGSERIAL PRIMARY KEY,
    utilisateur_id  BIGINT       NOT NULL UNIQUE REFERENCES utilisateurs (id),
    matricule       VARCHAR(30),
    date_embauche   DATE
);

CREATE TABLE ecritures_comptables (
    id              BIGSERIAL PRIMARY KEY,
    date_ecriture   DATE           NOT NULL DEFAULT CURRENT_DATE,
    numero_piece    VARCHAR(40)    NOT NULL,
    libelle         VARCHAR(200)   NOT NULL,
    compte_debit    VARCHAR(20)    NOT NULL,
    compte_credit   VARCHAR(20)    NOT NULL,
    montant         DECIMAL(12, 2) NOT NULL,
    reference_type  VARCHAR(30),
    reference_id    BIGINT,
    date_creation   TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ecritures_date ON ecritures_comptables (date_ecriture DESC);
CREATE INDEX idx_ecritures_ref ON ecritures_comptables (reference_type, reference_id);

CREATE TABLE notifications_comptable (
    id              BIGSERIAL PRIMARY KEY,
    utilisateur_id  BIGINT       NOT NULL REFERENCES utilisateurs (id) ON DELETE CASCADE,
    paiement_id     BIGINT       REFERENCES paiements (id) ON DELETE SET NULL,
    type            VARCHAR(40)  NOT NULL,
    titre           VARCHAR(150) NOT NULL,
    message         TEXT         NOT NULL,
    lu              BOOLEAN      NOT NULL DEFAULT FALSE,
    date_creation   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_comptable_user ON notifications_comptable (utilisateur_id, lu, date_creation DESC);

-- Compte comptable démo (mot de passe : admin123)
INSERT INTO utilisateurs (email, mot_de_passe, nom, prenom, telephone, role, actif) VALUES
('comptable@ecoli.mr', '$2b$10$rXRyOBEP3RKK3i/B/5ZQoemhIBM3iFwnvbZEfboXgkWja7kBtRJ1m', 'Mint Ahmed', 'Khadijetou', '+222 45 55 66 77', 'COMPTABLE', TRUE);

INSERT INTO comptables (utilisateur_id, matricule, date_embauche)
SELECT id, 'CPT-001', CURRENT_DATE - 365
FROM utilisateurs WHERE email = 'comptable@ecoli.mr';

INSERT INTO utilisateurs (email, mot_de_passe, nom, prenom, telephone, role, actif)
SELECT 'comptable2@ecoli.mr', '$2b$10$rXRyOBEP3RKK3i/B/5ZQoemhIBM3iFwnvbZEfboXgkWja7kBtRJ1m', 'Diallo', 'Fatimetou', '+222 46 12 34 56', 'COMPTABLE', TRUE
WHERE NOT EXISTS (SELECT 1 FROM utilisateurs WHERE email = 'comptable2@ecoli.mr');

INSERT INTO comptables (utilisateur_id, matricule, date_embauche)
SELECT id, 'CPT-002', CURRENT_DATE - 180
FROM utilisateurs WHERE email = 'comptable2@ecoli.mr'
  AND NOT EXISTS (SELECT 1 FROM comptables c JOIN utilisateurs u ON u.id = c.utilisateur_id WHERE u.email = 'comptable2@ecoli.mr');

-- Écritures comptables rétroactives à partir des paiements existants
INSERT INTO ecritures_comptables (date_ecriture, numero_piece, libelle, compte_debit, compte_credit, montant, reference_type, reference_id)
SELECT
    p.date_paiement,
    'EC-' || p.id,
    'Encaissement frais scolaires — ' || f.numero,
    '512',
    '706',
    p.montant,
    'PAIEMENT',
    p.id
FROM paiements p
JOIN factures f ON f.id = p.facture_id;

-- Notifications pour paiements existants (comptable démo)
INSERT INTO notifications_comptable (utilisateur_id, paiement_id, type, titre, message)
SELECT
    u.id,
    p.id,
    'PAIEMENT_RECU',
    'Paiement reçu',
    'Paiement de ' || p.montant || ' MRU pour la facture ' || f.numero || ' (' || e.nom || ' ' || e.prenom || ').'
FROM paiements p
JOIN factures f ON f.id = p.facture_id
JOIN eleves e ON e.id = f.eleve_id
CROSS JOIN utilisateurs u
WHERE u.email = 'comptable@ecoli.mr';
