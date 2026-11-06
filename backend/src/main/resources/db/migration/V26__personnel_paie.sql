-- Personnel de service (enseignants, agents d'entretien, gardiens, etc.) et paie mensuelle

CREATE TABLE personnel (
    id              BIGSERIAL PRIMARY KEY,
    nom             VARCHAR(80)    NOT NULL,
    prenom          VARCHAR(80)    NOT NULL,
    email           VARCHAR(120),
    telephone       VARCHAR(30),
    matricule       VARCHAR(30),
    categorie       VARCHAR(30)    NOT NULL,
    enseignant_id   BIGINT         UNIQUE REFERENCES enseignants (id) ON DELETE SET NULL,
    salaire_mensuel DECIMAL(12, 2) NOT NULL,
    date_embauche   DATE,
    statut          VARCHAR(20)    NOT NULL DEFAULT 'ACTIF',
    date_creation   TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_personnel_categorie ON personnel (categorie);
CREATE INDEX idx_personnel_statut ON personnel (statut);

CREATE TABLE paiements_personnel (
    id              BIGSERIAL PRIMARY KEY,
    personnel_id    BIGINT         NOT NULL REFERENCES personnel (id) ON DELETE CASCADE,
    mois            DATE           NOT NULL,
    montant         DECIMAL(12, 2) NOT NULL,
    mode_paiement   VARCHAR(30)    NOT NULL DEFAULT 'VIREMENT',
    date_paiement   DATE           NOT NULL DEFAULT CURRENT_DATE,
    reference       VARCHAR(80),
    date_creation   TIMESTAMP      NOT NULL DEFAULT NOW(),
    UNIQUE (personnel_id, mois)
);

CREATE INDEX idx_paiements_personnel_mois ON paiements_personnel (mois DESC);

-- Enseignants existants → fiche personnel
INSERT INTO personnel (nom, prenom, email, telephone, matricule, categorie, enseignant_id, salaire_mensuel, date_embauche, statut)
SELECT u.nom, u.prenom, u.email, u.telephone,
       'ENS-' || LPAD(e.id::text, 3, '0'), 'ENSEIGNANT', e.id,
       45000.00, e.date_embauche, 'ACTIF'
FROM enseignants e
JOIN utilisateurs u ON u.id = e.utilisateur_id;

-- Personnel de service (non enseignants)
INSERT INTO personnel (nom, prenom, email, telephone, matricule, categorie, salaire_mensuel, date_embauche, statut) VALUES
('Ould Vadel', 'Mohamed', 'gardien@ecoli.mr', '+222 45 22 33 44', 'SRV-001', 'GARDIEN', 25000.00, CURRENT_DATE - 400, 'ACTIF'),
('Mint Lemine', 'Fatimetou', 'entretien@ecoli.mr', '+222 45 33 44 55', 'SRV-002', 'AGENT_ENTRETIEN', 22000.00, CURRENT_DATE - 300, 'ACTIF'),
('Ould Sid''ahmed', 'Aichetou', 'secretaire@ecoli.mr', '+222 45 44 55 66', 'SRV-003', 'SECRETAIRE', 35000.00, CURRENT_DATE - 500, 'ACTIF');
