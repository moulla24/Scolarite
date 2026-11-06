-- Année scolaire 2025-2026 (inactive, planifiable à l'avance).

INSERT INTO annees_scolaires (ecole_id, nom, date_debut, date_fin, active)
SELECT 2, '2025-2026', '2025-09-01', '2026-06-30', FALSE
WHERE EXISTS (SELECT 1 FROM ecoles WHERE id = 2)
  AND NOT EXISTS (
    SELECT 1 FROM annees_scolaires WHERE ecole_id = 2 AND nom = '2025-2026'
  );
