-- Parent système pour les élèves sans tuteur assigné (tuteur_id NOT NULL).
INSERT INTO tuteurs (nom, prenom, email, telephone, adresse)
SELECT 'À affecter', 'Système', NULL, '00000000', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM tuteurs WHERE nom = 'À affecter' AND telephone = '00000000'
);
