INSERT INTO matieres (nom, code, coefficient) VALUES
    ('Anglais', 'EN', 1.50),
    ('Éducation physique', 'EPS', 1.00),
    ('Arts plastiques', 'ART', 1.00),
    ('Informatique', 'INFO', 1.00),
    ('Éducation islamique', 'EI', 1.50)
ON CONFLICT (nom) DO NOTHING;
