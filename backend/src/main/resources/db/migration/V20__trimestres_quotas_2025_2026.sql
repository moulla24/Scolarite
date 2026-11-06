-- Trimestres et quotas pour l'année courante 2025-2026 (école 2).

INSERT INTO trimestres (annee_scolaire_id, numero, nom, date_debut, date_fin, actif)
SELECT a.id, v.numero, v.nom, v.date_debut::date, v.date_fin::date, TRUE
FROM annees_scolaires a
CROSS JOIN (VALUES
    (1, 'Trimestre 1', '2025-09-01', '2025-12-20'),
    (2, 'Trimestre 2', '2026-01-06', '2026-03-28'),
    (3, 'Trimestre 3', '2026-04-01', '2026-06-30')
) AS v(numero, nom, date_debut, date_fin)
WHERE a.ecole_id = 2
  AND a.nom = '2025-2026'
  AND NOT EXISTS (
    SELECT 1 FROM trimestres t WHERE t.annee_scolaire_id = a.id
  );

-- Quotas T1 Primaire
INSERT INTO trimestre_types_controles (trimestre_id, type_controle_id, nombre_prevu)
SELECT tr.id, tc.id, v.nombre
FROM trimestres tr
JOIN annees_scolaires a ON a.id = tr.annee_scolaire_id AND a.ecole_id = 2 AND a.nom = '2025-2026'
JOIN niveaux n ON n.ecole_id = a.ecole_id AND n.nom = 'Primaire'
JOIN types_controles tc ON tc.niveau_id = n.id
JOIN (VALUES
    ('DEVOIR', 2),
    ('COMPOSITION', 1),
    ('INTERRO', 1)
) AS v(code, nombre) ON v.code = tc.code
WHERE tr.numero = 1
ON CONFLICT (trimestre_id, type_controle_id) DO NOTHING;

-- Quotas T1 Collège
INSERT INTO trimestre_types_controles (trimestre_id, type_controle_id, nombre_prevu)
SELECT tr.id, tc.id, v.nombre
FROM trimestres tr
JOIN annees_scolaires a ON a.id = tr.annee_scolaire_id AND a.ecole_id = 2 AND a.nom = '2025-2026'
JOIN niveaux n ON n.ecole_id = a.ecole_id AND n.nom = 'Collège'
JOIN types_controles tc ON tc.niveau_id = n.id
JOIN (VALUES
    ('DEVOIR', 2),
    ('COMPOSITION', 1)
) AS v(code, nombre) ON v.code = tc.code
WHERE tr.numero = 1
ON CONFLICT (trimestre_id, type_controle_id) DO NOTHING;

-- Quotas T1 Lycée
INSERT INTO trimestre_types_controles (trimestre_id, type_controle_id, nombre_prevu)
SELECT tr.id, tc.id, v.nombre
FROM trimestres tr
JOIN annees_scolaires a ON a.id = tr.annee_scolaire_id AND a.ecole_id = 2 AND a.nom = '2025-2026'
JOIN niveaux n ON n.ecole_id = a.ecole_id AND n.nom = 'Lycée'
JOIN types_controles tc ON tc.niveau_id = n.id
JOIN (VALUES
    ('DEVOIR', 2),
    ('COMPOSITION', 1),
    ('INTERRO', 1)
) AS v(code, nombre) ON v.code = tc.code
WHERE tr.numero = 1
ON CONFLICT (trimestre_id, type_controle_id) DO NOTHING;
