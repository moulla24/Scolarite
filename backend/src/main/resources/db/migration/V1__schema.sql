-- Schéma Ecoli — modules sidebar (hors facturation / paiements)

CREATE TABLE ecoles (
    id          BIGSERIAL PRIMARY KEY,
    nom         VARCHAR(150) NOT NULL,
    code        VARCHAR(30)  NOT NULL UNIQUE,
    adresse     VARCHAR(255),
    telephone   VARCHAR(30),
    email       VARCHAR(120),
    logo_url    VARCHAR(500)
);

CREATE TABLE utilisateurs (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(120) NOT NULL UNIQUE,
    mot_de_passe  VARCHAR(255) NOT NULL,
    nom           VARCHAR(80)  NOT NULL,
    prenom        VARCHAR(80)  NOT NULL,
    telephone     VARCHAR(30),
    role          VARCHAR(30)  NOT NULL,
    actif         BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE tuteurs (
    id         BIGSERIAL PRIMARY KEY,
    nom        VARCHAR(80)  NOT NULL,
    prenom     VARCHAR(80),
    email      VARCHAR(120),
    telephone  VARCHAR(30)  NOT NULL,
    adresse    VARCHAR(255)
);

CREATE TABLE enseignants (
    id              BIGSERIAL PRIMARY KEY,
    utilisateur_id  BIGINT       NOT NULL UNIQUE REFERENCES utilisateurs (id),
    specialite      VARCHAR(100),
    date_embauche   DATE
);

CREATE TABLE annees_scolaires (
    id          BIGSERIAL PRIMARY KEY,
    ecole_id    BIGINT       NOT NULL REFERENCES ecoles (id),
    nom         VARCHAR(50)  NOT NULL,
    date_debut  DATE         NOT NULL,
    date_fin    DATE         NOT NULL,
    active      BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE niveaux (
    id        BIGSERIAL PRIMARY KEY,
    ecole_id  BIGINT       NOT NULL REFERENCES ecoles (id),
    nom       VARCHAR(30)  NOT NULL,
    UNIQUE (ecole_id, nom)
);

CREATE TABLE classes (
    id         BIGSERIAL PRIMARY KEY,
    niveau_id  BIGINT       NOT NULL REFERENCES niveaux (id),
    nom        VARCHAR(50)  NOT NULL,
    salle      VARCHAR(50),
    capacite   INTEGER      NOT NULL DEFAULT 30,
    UNIQUE (niveau_id, nom)
);

CREATE TABLE matieres (
    id           BIGSERIAL PRIMARY KEY,
    nom          VARCHAR(80)  NOT NULL UNIQUE,
    code         VARCHAR(20)  NOT NULL UNIQUE,
    coefficient  DECIMAL(4,2) NOT NULL DEFAULT 1.00
);

CREATE TABLE eleves (
    id               BIGSERIAL PRIMARY KEY,
    ecole_id         BIGINT       NOT NULL REFERENCES ecoles (id),
    matricule        VARCHAR(30)  NOT NULL UNIQUE,
    nom              VARCHAR(80)  NOT NULL,
    prenom           VARCHAR(80)  NOT NULL,
    date_naissance   DATE,
    sexe             CHAR(1),
    tuteur_id        BIGINT       NOT NULL REFERENCES tuteurs (id)
);

CREATE TABLE inscriptions (
    id                 BIGSERIAL PRIMARY KEY,
    eleve_id           BIGINT       NOT NULL REFERENCES eleves (id),
    classe_id          BIGINT       NOT NULL REFERENCES classes (id),
    annee_scolaire_id  BIGINT       NOT NULL REFERENCES annees_scolaires (id),
    statut             VARCHAR(20)  NOT NULL DEFAULT 'ACTIF',
    date_inscription   DATE         NOT NULL DEFAULT CURRENT_DATE,
    UNIQUE (eleve_id, annee_scolaire_id)
);

CREATE TABLE enseignements (
    id             BIGSERIAL PRIMARY KEY,
    classe_id      BIGINT NOT NULL REFERENCES classes (id),
    matiere_id     BIGINT NOT NULL REFERENCES matieres (id),
    enseignant_id  BIGINT NOT NULL REFERENCES enseignants (id),
    UNIQUE (classe_id, matiere_id)
);

CREATE TABLE evaluations (
    id               BIGSERIAL PRIMARY KEY,
    enseignement_id  BIGINT       NOT NULL REFERENCES enseignements (id),
    type             VARCHAR(40)  NOT NULL,
    date_evaluation  DATE         NOT NULL,
    note_max         DECIMAL(5,2) NOT NULL DEFAULT 20.00
);

CREATE TABLE notes (
    id             BIGSERIAL PRIMARY KEY,
    evaluation_id  BIGINT       NOT NULL REFERENCES evaluations (id),
    eleve_id       BIGINT       NOT NULL REFERENCES eleves (id),
    note_obtenue   DECIMAL(5,2) NOT NULL,
    UNIQUE (evaluation_id, eleve_id)
);

CREATE TABLE maquettes_pedagogiques (
    id          BIGSERIAL PRIMARY KEY,
    titre       VARCHAR(200) NOT NULL,
    matiere_id  BIGINT       NOT NULL REFERENCES matieres (id),
    niveau_id   BIGINT       NOT NULL REFERENCES niveaux (id),
    description TEXT,
    format      VARCHAR(10)  NOT NULL DEFAULT 'PDF',
    fichier_url VARCHAR(500),
    nb_pages    INTEGER      NOT NULL DEFAULT 0,
    date_maj    DATE         NOT NULL DEFAULT CURRENT_DATE
);

CREATE TABLE seances_presences (
    id             BIGSERIAL PRIMARY KEY,
    classe_id      BIGINT       NOT NULL REFERENCES classes (id),
    matiere_id     BIGINT       NOT NULL REFERENCES matieres (id),
    enseignant_id  BIGINT       NOT NULL REFERENCES enseignants (id),
    jour_semaine   SMALLINT     NOT NULL CHECK (jour_semaine BETWEEN 1 AND 5),
    heure_debut    TIME         NOT NULL,
    heure_fin      TIME         NOT NULL,
    salle          VARCHAR(50),
    type           VARCHAR(30)  NOT NULL DEFAULT 'COURS'
);

CREATE TABLE presences_eleves (
    id         BIGSERIAL PRIMARY KEY,
    seance_id  BIGINT       NOT NULL REFERENCES seances_presences (id),
    eleve_id   BIGINT       NOT NULL REFERENCES eleves (id),
    statut     VARCHAR(20)  NOT NULL,
    motif      VARCHAR(255),
    UNIQUE (seance_id, eleve_id)
);

CREATE TABLE presences_enseignants (
    id             BIGSERIAL PRIMARY KEY,
    seance_id      BIGINT       NOT NULL REFERENCES seances_presences (id),
    enseignant_id  BIGINT       NOT NULL REFERENCES enseignants (id),
    statut         VARCHAR(20)  NOT NULL,
    motif          VARCHAR(255),
    UNIQUE (seance_id, enseignant_id)
);

CREATE INDEX idx_inscriptions_classe ON inscriptions (classe_id);
CREATE INDEX idx_inscriptions_annee ON inscriptions (annee_scolaire_id);
CREATE INDEX idx_notes_eleve ON notes (eleve_id);
CREATE INDEX idx_seances_classe ON seances_presences (classe_id);
