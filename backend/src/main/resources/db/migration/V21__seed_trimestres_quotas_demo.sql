-- Trimestres de démonstration pour l'année active (école 2).

INSERT INTO trimestres (annee_scolaire_id, numero, nom, date_debut, date_fin, actif)
SELECT a.id, v.numero, v.nom, v.date_debut::date, v.date_fin::date, TRUE
FROM annees_scolaires a
CROSS JOIN (VALUES
    (1, 'Trimestre 1', '2024-09-01', '2024-12-20'),
    (2, 'Trimestre 2', '2025-01-06', '2025-03-28'),
    (3, 'Trimestre 3', '2025-04-01', '2025-06-30')
) AS v(numero, nom, date_debut, date_fin)
WHERE a.ecole_id = 2
  AND a.active = TRUE
  AND NOT EXISTS (
    SELECT 1 FROM trimestres t WHERE t.annee_scolaire_id = a.id
  );

-- Quotas exemple : Primaire T1 — 2 devoirs, 1 composition, 1 interrogation.
INSERT INTO trimestre_types_controles (trimestre_id, type_controle_id, nombre_prevu)
SELECT tr.id, tc.id, v.nombre
FROM trimestres tr
JOIN annees_scolaires a ON a.id = tr.annee_scolaire_id AND a.active = TRUE AND a.ecole_id = 2
JOIN niveaux n ON n.ecole_id = a.ecole_id AND n.nom = 'Primaire'
JOIN types_controles tc ON tc.niveau_id = n.id
JOIN (VALUES
    ('DEVOIR', 2),
    ('COMPOSITION', 1),
    ('INTERRO', 1)
) AS v(code, nombre) ON v.code = tc.code
WHERE tr.numero = 1
ON CONFLICT (trimestre_id, type_controle_id) DO NOTHING;

-- Collège T1 — 2 devoirs, 1 composition (pas d'interro).
INSERT INTO trimestre_types_controles (trimestre_id, type_controle_id, nombre_prevu)
SELECT tr.id, tc.id, v.nombre
FROM trimestres tr
JOIN annees_scolaires a ON a.id = tr.annee_scolaire_id AND a.active = TRUE AND a.ecole_id = 2
JOIN niveaux n ON n.ecole_id = a.ecole_id AND n.nom = 'Collège'
JOIN types_controles tc ON tc.niveau_id = n.id
JOIN (VALUES
    ('DEVOIR', 2),
    ('COMPOSITION', 1),
    ('INTERRO', 0)
) AS v(code, nombre) ON v.code = tc.code
WHERE tr.numero = 1
  AND v.nombre > 0
ON CONFLICT (trimestre_id, type_controle_id) DO NOTHING;
