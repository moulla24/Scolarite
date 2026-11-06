import 'package:ecoli_parent/models/parent_models.dart';
import 'package:ecoli_parent/providers/parent_polling.dart';
import 'package:ecoli_parent/providers/providers.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

final notificationsProvider =
    FutureProvider<List<ParentNotification>>((ref) async {
  ref.watch(parentDataPollTickProvider);
  if (!ref.watch(authStateProvider).isLoggedIn) return [];
  return ref.read(parentAppServiceProvider).getNotifications();
});

final demandesPaiementProvider =
    FutureProvider<List<DemandePaiement>>((ref) async {
  ref.watch(parentDataPollTickProvider);
  if (!ref.watch(authStateProvider).isLoggedIn) return [];
  return ref.read(parentAppServiceProvider).getDemandesPaiement();
});
