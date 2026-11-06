-- Alignement avec Hibernate (String → VARCHAR, pas CHAR/bpchar)
ALTER TABLE eleves
    ALTER COLUMN sexe TYPE VARCHAR(1) USING trim(sexe)::varchar;
