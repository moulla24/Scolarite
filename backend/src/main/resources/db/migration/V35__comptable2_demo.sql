-- Second compte comptable démo (mot de passe : admin123)
INSERT INTO utilisateurs (email, mot_de_passe, nom, prenom, telephone, role, actif)
SELECT 'comptable2@ecoli.mr', '$2b$10$rXRyOBEP3RKK3i/B/5ZQoemhIBM3iFwnvbZEfboXgkWja7kBtRJ1m', 'Diallo', 'Fatimetou', '+222 46 12 34 56', 'COMPTABLE', TRUE
WHERE NOT EXISTS (SELECT 1 FROM utilisateurs WHERE email = 'comptable2@ecoli.mr');

INSERT INTO comptables (utilisateur_id, matricule, date_embauche)
SELECT id, 'CPT-002', CURRENT_DATE - 180
FROM utilisateurs WHERE email = 'comptable2@ecoli.mr'
  AND NOT EXISTS (
    SELECT 1 FROM comptables c JOIN utilisateurs u ON u.id = c.utilisateur_id WHERE u.email = 'comptable2@ecoli.mr'
  );
