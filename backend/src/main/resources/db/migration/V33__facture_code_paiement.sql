-- Code de paiement unique affiché sur la facture (référence banque / mobile money)

ALTER TABLE factures
    ADD COLUMN IF NOT EXISTS code_paiement VARCHAR(30);

UPDATE factures
SET code_paiement = REPLACE(numero, 'FAC-', 'PAY-')
WHERE code_paiement IS NULL OR TRIM(code_paiement) = '';

ALTER TABLE factures
    ALTER COLUMN code_paiement SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_factures_code_paiement ON factures (code_paiement);
