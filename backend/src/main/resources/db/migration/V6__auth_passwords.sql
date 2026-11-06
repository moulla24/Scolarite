-- Mot de passe par défaut pour tous les comptes démo : admin123
UPDATE utilisateurs
SET mot_de_passe = '$2b$10$rXRyOBEP3RKK3i/B/5ZQoemhIBM3iFwnvbZEfboXgkWja7kBtRJ1m'
WHERE mot_de_passe LIKE '$2a$10$placeholder%' OR mot_de_passe = '{noop}';
