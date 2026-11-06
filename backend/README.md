# Ecoli — Backend API

API REST Spring Boot pour la gestion de scolarité primaire, connectée à PostgreSQL.

## Prérequis

- Java 17+
- Maven 3.9+
- PostgreSQL 14+

## Base de données

### Erreur « la base de données ecoli n'existe pas »

PostgreSQL est démarré, mais la base **`ecoli`** doit être créée **manuellement une fois** avant `mvn spring-boot:run`. Flyway crée ensuite les tables à l'intérieur de cette base.

**Option A — pgAdmin (recommandé sous Windows)**

1. Ouvrir pgAdmin → se connecter au serveur PostgreSQL
2. Clic droit sur **Databases** → **Create** → **Database**
3. Nom : `ecoli` → **Save**

**Option B — SQL**

Connecté à la base système `postgres` (pgAdmin / `psql`) :

```sql
CREATE DATABASE ecoli;
```

**Option C — Docker**

```bash
cd backend
docker compose up -d
```

Docker crée automatiquement la base `ecoli` (voir `docker-compose.yml`).

Configurer le mot de passe PostgreSQL **localement** (jamais dans Git) :

```bash
cd backend/src/main/resources
copy application-local.yml.example application-local.yml
```

Avec Docker Compose, le mot de passe par défaut est `postgres`.  
`application-local.yml` est ignoré par Git (voir `.gitignore` à la racine).

Les migrations Flyway (`src/main/resources/db/migration/V*.sql`) créent le schéma et les données de démo au démarrage. Ne pas utiliser d’anciens scripts SQL manuels : Flyway est la seule source de vérité.

### Erreur « Migration checksum mismatch »

Flyway refuse de démarrer si un fichier de migration **déjà exécuté** a été modifié. **Ne changez pas** `V1__`, `V2__`… : ajoutez `V4__ma_modif.sql` à la place.

**Solution rapide (développement)** : relancer après restauration des fichiers d’origine, ou recréer la base :

```sql
DROP DATABASE ecoli;
CREATE DATABASE ecoli;
```

Puis `mvn spring-boot:run`.

## Lancement

```bash
cd backend
mvn spring-boot:run
```

- API : [http://localhost:8081/api](http://localhost:8081/api)
- Swagger UI : [http://localhost:8081/api/swagger-ui.html](http://localhost:8081/api/swagger-ui.html)

> Le port **8081** est utilisé par défaut car **8080** est souvent pris par Apache/XAMPP sur Windows.

## Endpoints (modules sidebar)

Tous les corps de requête CRUD sont documentés dans **Swagger UI**. Les créations renvoient **201** avec l’URL de la ressource ; les suppressions renvoient **204**.

| Module | Opérations | URL de base |
|--------|------------|-------------|
| Auth | login, profil | `/api/auth/login`, `/api/auth/me` |
| Dashboard | stats, activité | `/api/dashboard/stats`, `/api/dashboard/activity` |
| Parents | CRUD | `/api/parents` — liste : `?search=&niveauId=` |
| Élèves | CRUD | `/api/eleves` — liste : `?search=&niveauId=&classeId=` |
| Classes | CRUD | `/api/classes` — liste : `?search=&niveauId=` |
| Enseignants | CRUD | `/api/enseignants` — liste : `?search=` |
| Maquettes | CRUD | `/api/maquettes` — liste : `?search=&matiereId=&niveauId=` |
| Emploi du temps | CRUD | `/api/emploi-du-temps` — liste : `?classeId=` |
| Notes | bulletins + CRUD évaluations/notes | voir ci-dessous |
| Enseignements | liste | `/api/enseignements?classeId=&matiereId=` |
| Facturation | factures, paiements | `/api/factures`, `/api/factures/paiements` |
| Écoles | CRUD | `/api/ecoles` |
| Niveaux | CRUD | `/api/niveaux` |
| Matières | liste | `/api/matieres` |
| Années scolaires | CRUD | `/api/annees-scolaires` |

### Notes

| Méthode | URL |
|---------|-----|
| GET | `/api/notes/bulletins?classeId=&niveauId=` |
| GET / POST | `/api/notes/evaluations` — liste : `?classeId=` |
| GET / PUT / DELETE | `/api/notes/evaluations/{id}` |
| GET | `/api/notes/evaluations/{evaluationId}/notes` |
| GET / POST | `/api/notes/notes` |
| GET / PUT / DELETE | `/api/notes/notes/{id}` |

### Écoles

| Méthode | URL |
|---------|-----|
| GET / POST | `/api/ecoles` |
| GET / PUT / DELETE | `/api/ecoles/{id}` |

> L’établissement de référence a **`id: 2`** (`École Primaire Ecoli`, code `ECOLI-02`). La migration **V5** définit les niveaux **Primaire**, **Collège**, **Lycée** et leurs classes (1AF–6AF, 1AS–4AS, 5AS–7D, etc.).

### Niveaux

| Méthode | URL |
|---------|-----|
| GET / POST | `/api/niveaux` |
| GET / PUT / DELETE | `/api/niveaux/{id}` |

### Matières

| Méthode | URL |
|---------|-----|
| GET | `/api/matieres` |

### Années scolaires

| Méthode | URL |
|---------|-----|
| GET / POST | `/api/annees-scolaires` |
| GET / PUT / DELETE | `/api/annees-scolaires/{id}` |

Corps `AnneeScolaireRequest` : `ecoleId`, `nom`, `dateDebut`, `dateFin`, `active`. Si `active` est `true` à la création ou la mise à jour, les autres années de la même école sont désactivées. La suppression est refusée lorsque des inscriptions existent pour l’année.

## Architecture (module par entité)

Chaque entité métier dispose d’un package `mr.ecoli.<module>` regroupant entité JPA, repository, service, contrôleur, DTO et requêtes au même niveau (sans sous-dossiers, sauf exceptions ci-dessous).

```
mr.ecoli/
├── EcoliApplication.java
├── common/                 # EntityReferenceService, exceptions, EntityMapper
│   ├── exception/
│   └── mapper/
├── infrastructure/config/  # WebConfig, OpenApiConfig
├── dashboard/
├── ecole/
├── anneescolaire/
├── niveau/
├── matiere/
├── tuteur/
├── eleve/
├── inscription/
├── classe/
├── utilisateur/
├── enseignant/
├── enseignement/
├── maquette/
├── seance/
├── evaluation/
└── note/
```

- **common** : références partagées (`EntityReferenceService`), gestion d’erreurs, mapping DTO
- **infrastructure.config** : configuration transverse (CORS, Swagger)
- Un module = une entité = un point d’entrée API lorsque l’entité est exposée

## Tables

`ecoles`, `utilisateurs`, `tuteurs`, `enseignants`, `annees_scolaires`, `niveaux`, `classes`, `matieres`, `eleves`, `inscriptions`, `enseignements`, `evaluations`, `notes`, `maquettes_pedagogiques`, `seances_presences`, `presences_eleves`, `presences_enseignants`, `frais_scolaires`, `factures`, `lignes_facture`, `paiements`.

Compte démo : `admin@ecoli.mr` / `admin123`.
