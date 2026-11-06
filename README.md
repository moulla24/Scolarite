# Scolarité Primaire — Ecoli

Monorepo de gestion de scolarité primaire (administration web, comptabilité, application parents).

## Structure

| Dossier | Technologie | Rôle |
|---------|-------------|------|
| [`backend/`](backend/) | Spring Boot 3 + PostgreSQL + Flyway | API REST (`/api`) |
| [`frontend/`](frontend/) | Angular 19 | Interface admin / comptable |
| [`mobile/`](mobile/) | Flutter | Application parents |

## Prérequis

- Java 17+, Maven 3.9+
- Node.js 20+ / npm
- Flutter 3.x
- PostgreSQL 14+ (ou Docker)

## Démarrage rapide

### 1. Base de données

```bash
cd backend
docker compose up -d
```

Ou créer manuellement la base `ecoli`, puis :

```bash
cd backend/src/main/resources
copy application-local.yml.example application-local.yml
```

Ajuster le mot de passe Postgres dans `application-local.yml` si besoin.

### 2. Backend

```bash
cd backend
mvn spring-boot:run
```

- API : http://localhost:8081/api  
- Swagger : http://localhost:8081/api/swagger-ui.html  

Le schéma et les données de démo sont créés par **Flyway** (`src/main/resources/db/migration/`).

### 3. Frontend

```bash
cd frontend
npm install
npm start
```

→ http://localhost:4200

### 4. Mobile parents

```bash
cd mobile
flutter pub get
flutter run -d chrome --dart-define=API_BASE_URL=http://localhost:8081/api
```

Voir [`mobile/README.md`](mobile/README.md) pour Android / iOS / appareil physique.

## Comptes de démonstration

| Rôle | Email | Mot de passe |
|------|-------|--------------|
| Admin | `admin@ecoli.mr` | `admin123` |
| Comptable | `comptable@ecoli.mr` | `admin123` |
| Parent | `mohamed.ouldahmed@email.mr` | `admin123` |

## Documentation

- Backend : [`backend/README.md`](backend/README.md)
- Frontend : [`frontend/README.md`](frontend/README.md)
- Mobile : [`mobile/README.md`](mobile/README.md)

## Notes importantes

- Ne pas versionner `application-local.yml` (secrets locaux).
- Ne pas modifier les migrations Flyway déjà appliquées : ajouter un nouveau fichier `Vxx__...sql`.
- Le paiement parent utilise un code facture `PAY-…` et une simulation bancaire (délai configurable), puis validation par le comptable.
