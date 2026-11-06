-- Nettoyage : désactiver les anciens frais génériques (V7/V8) en conflit avec V14
-- Les frais par classe (1AF, 2AF) restent actifs.

UPDATE frais_scolaires
SET actif = FALSE
WHERE actif = TRUE
  AND classe_id IS NULL
  AND niveau_id IS NULL
  AND libelle LIKE 'Frais scolaires%';

-- Réinitialiser échéances/retards générés avec les mauvais montants
DELETE FROM historique_retards;
DELETE FROM echeances_mensuelles;

-- Recalcul montant_brut / total sans pénalités sur factures existantes
UPDATE factures
SET montant_brut = COALESCE(montant_brut, montant_total),
    montant_penalite = 0,
    montant_total = COALESCE(montant_brut, montant_total) - montant_remise
WHERE montant_penalite > 0 OR montant_brut IS NULL;
