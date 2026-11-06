# Ecoli — Frontend (Scolarité primaire)

Application Angular de gestion de scolarité primaire, connectée à l’API Spring Boot.

## Démarrage

```bash
cd frontend
npm install
npm start
```

Ouvrir [http://localhost:4200](http://localhost:4200).

**Connexion démo :** `admin@ecoli.mr` / `admin123`

Le proxy dev redirige `/api` vers `http://localhost:8081`.

## Navigation

| Route | Page |
|-------|------|
| `/login` | Connexion |
| `/dashboard` | Tableau de bord |
| `/parents` | Parents (CRUD, import/export CSV) |
| `/students` | Élèves (CRUD, import/export CSV) |
| `/notes` | Notes et bulletins (saisie, import/export) |
| `/maquettes` | Maquettes pédagogiques (upload, CRUD) |
| `/enseignants` | Enseignants (CRUD) |
| `/classes` | Classes (CRUD) |
| `/emploi-du-temps` | Emploi du temps (CRUD, impression) |
| `/parametres` | Niveaux, années scolaires, écoles |

## Architecture

- **Auth** : JWT (`AuthService`, intercepteur, gardes de routes)
- **Data providers** : services API injectés via tokens
- **Facades** : état UI par feature
- **Composants partagés** : `PageHeader`, `SearchField`, `FilterCard`, `RowActionsMenu`, etc.
