-- Application mobile parents : liaison tuteur ↔ utilisateur, notifications, tokens FCM

ALTER TABLE tuteurs
    ADD COLUMN utilisateur_id BIGINT UNIQUE REFERENCES utilisateurs (id);

CREATE TABLE notifications_parent (
    id              BIGSERIAL PRIMARY KEY,
    tuteur_id       BIGINT       NOT NULL REFERENCES tuteurs (id) ON DELETE CASCADE,
    eleve_id        BIGINT       REFERENCES eleves (id) ON DELETE SET NULL,
    type            VARCHAR(40)  NOT NULL,
    titre           VARCHAR(150) NOT NULL,
    message         TEXT         NOT NULL,
    lu              BOOLEAN      NOT NULL DEFAULT FALSE,
    date_creation   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_parent_tuteur ON notifications_parent (tuteur_id, lu, date_creation DESC);

CREATE TABLE parent_device_tokens (
    id                    BIGSERIAL PRIMARY KEY,
    tuteur_id             BIGINT       NOT NULL REFERENCES tuteurs (id) ON DELETE CASCADE,
    token                 VARCHAR(500) NOT NULL,
    plateforme            VARCHAR(20),
    date_enregistrement   TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE (tuteur_id, token)
);

-- Comptes parents (mot de passe démo : admin123 — même hash BCrypt que les autres comptes)
INSERT INTO utilisateurs (email, mot_de_passe, nom, prenom, telephone, role, actif) VALUES
('mohamed.ouldahmed@email.mr', '$2b$10$rXRyOBEP3RKK3i/B/5ZQoemhIBM3iFwnvbZEfboXgkWja7kBtRJ1m', 'Ould Ahmed', 'Mohamed', '+222 45 12 34 56', 'PARENT', TRUE),
('fatimetou.mintsalem@email.mr', '$2b$10$rXRyOBEP3RKK3i/B/5ZQoemhIBM3iFwnvbZEfboXgkWja7kBtRJ1m', 'Mint Salem', 'Fatimetou', '+222 46 23 45 67', 'PARENT', TRUE),
('abdellahi.ouldcheikh@email.mr', '$2b$10$rXRyOBEP3RKK3i/B/5ZQoemhIBM3iFwnvbZEfboXgkWja7kBtRJ1m', 'Ould Cheikh', 'Abdellahi', '+222 47 34 56 78', 'PARENT', TRUE);

UPDATE tuteurs t
SET utilisateur_id = u.id
FROM utilisateurs u
WHERE u.email = t.email AND u.role = 'PARENT';

-- Notifications de démonstration (eleve_id résolu par tuteur, IDs variables après migrations V4/V8)
INSERT INTO notifications_parent (tuteur_id, eleve_id, type, titre, message)
SELECT 1, e.id, 'NOTE', 'Nouvelle note — Français',
       e.prenom || ' ' || e.nom || ' a obtenu 15,5/20 au devoir de Français du 15/10/2025.'
FROM eleves e WHERE e.tuteur_id = 1 ORDER BY e.id LIMIT 1;

INSERT INTO notifications_parent (tuteur_id, eleve_id, type, titre, message)
SELECT 1, e.id, 'FACTURE', 'Facture en attente',
       'Une facture de scolarité reste à régler. Consultez l''onglet Facturation.'
FROM eleves e WHERE e.tuteur_id = 1 ORDER BY e.id LIMIT 1;

INSERT INTO notifications_parent (tuteur_id, eleve_id, type, titre, message)
SELECT 2, e.id, 'ABSENCE', 'Absence signalée',
       e.prenom || ' ' || e.nom || ' était absente le 20/10/2025.'
FROM eleves e WHERE e.tuteur_id = 2 ORDER BY e.id LIMIT 1;

INSERT INTO notifications_parent (tuteur_id, eleve_id, type, titre, message)
SELECT 2, e.id, 'INFO', 'Réunion parents',
       'Réunion parents-professeurs prévue le 15 novembre. Merci de confirmer votre présence.'
FROM eleves e WHERE e.tuteur_id = 2 ORDER BY e.id LIMIT 1;

INSERT INTO notifications_parent (tuteur_id, eleve_id, type, titre, message)
SELECT 3, e.id, 'NOTE', 'Nouvelle note — Mathématiques',
       e.prenom || ' ' || e.nom || ' a obtenu une note en mathématiques. Consultez le bulletin.'
FROM eleves e WHERE e.tuteur_id = 3 ORDER BY e.id LIMIT 1;
