-- Complète les affectations : chaque matière × chaque classe de l'école 2
-- Français, Arabe, Histoire-Géo → enseignant 1 | Mathématiques, Sciences → enseignant 2

INSERT INTO enseignements (classe_id, matiere_id, enseignant_id)
SELECT c.id,
       m.id,
       CASE WHEN m.id IN (1, 2, 5) THEN 1 ELSE 2 END
FROM classes c
JOIN niveaux n ON n.id = c.niveau_id
CROSS JOIN matieres m
WHERE n.ecole_id = 2
  AND NOT EXISTS (
      SELECT 1 FROM enseignements en
      WHERE en.classe_id = c.id AND en.matiere_id = m.id
  );
