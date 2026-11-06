import 'package:ecoli_parent/core/api/api_client.dart';
import 'package:ecoli_parent/models/parent_models.dart';

class ParentAppService {
  ParentAppService(this._client);

  final ApiClient _client;

  Future<ParentDashboard> getDashboard() => _client.get(
        '/parent-app/dashboard',
        (json) => ParentDashboard.fromJson(json as Map<String, dynamic>),
      );

  Future<List<EnfantResume>> getEnfants() => _client.get(
        '/parent-app/enfants',
        (json) => parseList(json, EnfantResume.fromJson),
      );

  Future<Scolarite> getScolarite(int eleveId) => _client.get(
        '/parent-app/enfants/$eleveId/scolarite',
        (json) => Scolarite.fromJson(json as Map<String, dynamic>),
      );

  Future<Bulletin> getBulletin(int eleveId) => _client.get(
        '/parent-app/enfants/$eleveId/bulletin',
        (json) => Bulletin.fromJson(json as Map<String, dynamic>),
      );

  Future<List<Absence>> getAbsences(int eleveId) => _client.get(
        '/parent-app/enfants/$eleveId/absences',
        (json) => parseList(json, Absence.fromJson),
      );

  Future<List<SeanceEmploi>> getEmploiDuTemps(int eleveId) => _client.get(
        '/parent-app/enfants/$eleveId/emploi-du-temps',
        (json) => parseList(json, SeanceEmploi.fromJson),
      );

  Future<List<Facture>> getFactures() => _client.get(
        '/parent-app/factures',
        (json) => parseList(json, Facture.fromJson),
      );

  Future<Facture> getFacture(int factureId) => _client.get(
        '/parent-app/factures/$factureId',
        (json) => Facture.fromJson(json as Map<String, dynamic>),
      );

  Future<List<DemandePaiement>> getDemandesPaiement() => _client.get(
        '/parent-app/paiements',
        (json) => parseList(json, DemandePaiement.fromJson),
      );

  Future<DemandePaiement> declarerPaiement(DemandePaiementRequest request) =>
      _client.post(
        '/parent-app/paiements',
        request.toJson(),
        (json) => DemandePaiement.fromJson(json as Map<String, dynamic>),
      );

  Future<DemandePaiement> getDemandePaiement(int id) => _client.get(
        '/parent-app/paiements/$id',
        (json) => DemandePaiement.fromJson(json as Map<String, dynamic>),
      );

  Future<Map<String, int>> getPaiementConfig() async {
    final data = await _client.get<Map<String, dynamic>>(
      '/parent-app/paiements/config',
      (json) => json as Map<String, dynamic>,
    );
    return {'delaiSecondes': (data['delaiSecondes'] as num).toInt()};
  }

  Future<List<ParentNotification>> getNotifications() => _client.get(
        '/parent-app/notifications',
        (json) => parseList(json, ParentNotification.fromJson),
      );

  Future<int> getUnreadCount() async {
    final data = await _client.get<Map<String, dynamic>>(
      '/parent-app/notifications/unread-count',
      (json) => json as Map<String, dynamic>,
    );
    return (data['count'] as num).toInt();
  }

  Future<void> markNotificationRead(int id) =>
      _client.put('/parent-app/notifications/$id/lu');

  Future<void> markAllNotificationsRead() =>
      _client.put('/parent-app/notifications/lu-tout');

  Future<void> registerDeviceToken(String token, String plateforme) =>
      _client.postVoid(
        '/parent-app/device-token',
        body: {'token': token, 'plateforme': plateforme},
      );
}
