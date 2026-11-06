-- Compte administrateur : Amine (mot de passe par défaut : admin123)
INSERT INTO utilisateurs (email, mot_de_passe, nom, prenom, telephone, role, actif)
SELECT
    'amine@ecoli.mr',
    '$2b$10$rXRyOBEP3RKK3i/B/5ZQoemhIBM3iFwnvbZEfboXgkWja7kBtRJ1m',
    'Admin',
    'Amine',
    '+222 45 00 00 01',
    'ADMIN',
    TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM utilisateurs WHERE email = 'amine@ecoli.mr'
);
