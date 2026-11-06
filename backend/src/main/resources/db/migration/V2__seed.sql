-- Données de démonstration

INSERT INTO ecoles (nom, code, adresse, telephone, email)
VALUES ('École Primaire Ecoli', 'ECOLI-01', 'Nouakchott', '+222 45 00 00 00', 'contact@ecoli.mr');

INSERT INTO utilisateurs (email, mot_de_passe, nom, prenom, telephone, role, actif) VALUES
('admin@ecoli.mr', '$2a$10$placeholder', 'Mohamed', 'Ahmed', '+222 45 11 11 11', 'ADMIN', TRUE),
('khadijetou@ecoli.mr', '$2a$10$placeholder', 'Mint Ely', 'Khadijetou', '+222 45 11 22 33', 'ENSEIGNANT', TRUE),
('m.sidaty@ecoli.mr', '$2a$10$placeholder', 'Ould Sidaty', 'Mohamed', '+222 46 22 33 44', 'ENSEIGNANT', TRUE);

INSERT INTO enseignants (utilisateur_id, specialite, date_embauche) VALUES
(2, 'Français / Arabe', '2020-09-01'),
(3, 'Mathématiques', '2019-09-01');

INSERT INTO tuteurs (nom, prenom, email, telephone) VALUES
('Ould Ahmed', 'Mohamed', 'mohamed.ouldahmed@email.mr', '+222 45 12 34 56'),
('Mint Salem', 'Fatimetou', 'fatimetou.mintsalem@email.mr', '+222 46 23 45 67'),
('Ould Cheikh', 'Abdellahi', 'abdellahi.ouldcheikh@email.mr', '+222 47 34 56 78');

INSERT INTO annees_scolaires (ecole_id, nom, date_debut, date_fin, active) VALUES
(1, '2024-2025', '2024-09-01', '2025-06-30', TRUE);

INSERT INTO niveaux (ecole_id, nom) VALUES
(1, 'CP'), (1, 'CE1'), (1, 'CE2'), (1, 'CM1'), (1, 'CM2');

INSERT INTO classes (niveau_id, nom, salle, capacite) VALUES
(1, 'A', 'Salle 101', 30),
(2, 'A', 'Salle 102', 30),
(2, 'B', 'Salle 103', 30),
(3, 'A', 'Salle 201', 30),
(4, 'A', 'Salle 202', 28),
(5, 'A', 'Salle 203', 28),
(5, 'B', 'Salle 204', 28);

INSERT INTO matieres (nom, code, coefficient) VALUES
('Français', 'FR', 2.00),
('Arabe', 'AR', 2.00),
('Mathématiques', 'MATH', 2.00),
('Sciences', 'SCI', 1.50),
('Histoire-Géographie', 'HG', 1.50);

INSERT INTO eleves (ecole_id, matricule, nom, prenom, date_naissance, sexe, tuteur_id) VALUES
(1, 'MAT-2024-001', 'Ould Ahmed', 'Fatimetou', '2014-03-12', 'F', 1),
(1, 'MAT-2024-002', 'Mint Salem', 'Aichetou', '2015-07-20', 'F', 2),
(1, 'MAT-2024-003', 'Ould Cheikh', 'Mohamed', '2016-01-05', 'M', 3);

INSERT INTO inscriptions (eleve_id, classe_id, annee_scolaire_id, statut) VALUES
(1, 7, 1, 'ACTIF'),
(2, 3, 1, 'ACTIF'),
(3, 1, 1, 'ACTIF');

INSERT INTO enseignements (classe_id, matiere_id, enseignant_id) VALUES
(1, 1, 1), (1, 3, 2), (3, 1, 1), (7, 3, 2);

INSERT INTO evaluations (enseignement_id, type, date_evaluation, note_max) VALUES
(1, 'Devoir', '2025-10-15', 20),
(4, 'Composition', '2025-10-20', 20);

INSERT INTO notes (evaluation_id, eleve_id, note_obtenue) VALUES
(1, 1, 15.5),
(1, 2, 14.0),
(2, 1, 16.0);

INSERT INTO maquettes_pedagogiques (titre, matiere_id, niveau_id, description, format, nb_pages) VALUES
('Progression annuelle — Français CP', 1, 1, 'Objectifs trimestriels CP', 'PDF', 24),
('Séquence Les nombres — CE1', 3, 2, '6 séances mathématiques', 'DOCX', 18);

INSERT INTO seances_presences (classe_id, matiere_id, enseignant_id, jour_semaine, heure_debut, heure_fin, salle, type) VALUES
(1, 1, 1, 1, '08:00', '09:00', 'Salle 101', 'COURS'),
(1, 3, 2, 1, '09:00', '10:00', 'Salle 101', 'COURS'),
(7, 3, 2, 2, '09:00', '10:00', 'Salle 203', 'COURS');
