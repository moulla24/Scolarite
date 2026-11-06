-- 2025-2026 devient l'année scolaire courante (école 2).

UPDATE annees_scolaires SET active = FALSE WHERE ecole_id = 2 AND nom = '2024-2025';

UPDATE annees_scolaires SET active = TRUE WHERE ecole_id = 2 AND nom = '2025-2026';
