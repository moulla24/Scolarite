# Ecoli Parents — Application mobile Flutter

Application mobile pour les parents, connectée au backend Spring Boot Ecoli.

## Prérequis

- Flutter 3.x
- Backend démarré sur le port **8081** (migration Flyway V11 appliquée)

## Lancer l'application

```bash
cd mobile
flutter pub get
flutter run
```

## URL de l'API

Par défaut (émulateur Android) : `http://10.0.2.2:8081/api`

Pour changer :

```bash
flutter run --dart-define=API_BASE_URL=http://192.168.1.10:8081/api
```

| Plateforme | URL typique |
|------------|-------------|
| Émulateur Android | `http://10.0.2.2:8081/api` |
| Simulateur iOS | `http://localhost:8081/api` |
| Chrome (Flutter Web) | `http://localhost:8081/api` |
| Appareil physique | `http://<IP_PC>:8081/api` |

**Flutter Web** : le navigateur utilise un port dynamique (`http://localhost:5xxxx`). Le backend autorise `http://localhost:*` en développement. Après modification CORS, **redémarrez** le backend.

```bash
flutter run -d chrome --dart-define=API_BASE_URL=http://localhost:8081/api
```

## Comptes de test

| Email | Mot de passe |
|-------|--------------|
| mohamed.ouldahmed@email.mr | admin123 |
| fatimetou.mintsalem@email.mr | admin123 |
| abdellahi.ouldcheikh@email.mr | admin123 |

## Fonctionnalités

- Connexion JWT (rôle PARENT uniquement)
- Tableau de bord (enfants, notifications, solde)
- Liste et détail des enfants (scolarité, notes, absences, emploi du temps)
- Notifications (marquer lu / tout marquer lu)
- Factures (liste + détail)

## Structure

```
lib/
  core/       # API, config, thème, stockage token
  models/     # DTOs alignés sur le backend
  services/   # AuthService, ParentAppService
  providers/  # Riverpod
  router/     # go_router
  features/   # Écrans par fonctionnalité
```

Documentation API : Swagger backend — http://localhost:8081/api/swagger-ui.html
