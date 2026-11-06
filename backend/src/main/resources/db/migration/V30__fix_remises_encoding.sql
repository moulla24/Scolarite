-- Correction des libellés de remises (encodage UTF-8)
UPDATE remises_eleve
SET libelle = 'Réduction familiale — 3 enfants inscrits'
WHERE eleve_id = 7 AND type_remise = 'FAMILIALE';

UPDATE remises_eleve
SET libelle = 'Réduction fratrie'
WHERE eleve_id = 14 AND type_remise = 'FAMILIALE';

UPDATE remises_eleve
SET libelle = 'Bourse d''excellence — frais intégralement couverts'
WHERE eleve_id = 12 AND type_remise = 'BOURSE_TOTALE';
