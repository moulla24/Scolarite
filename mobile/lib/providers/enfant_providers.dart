import 'package:ecoli_parent/models/parent_models.dart';
import 'package:ecoli_parent/providers/providers.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

final scolariteProvider = FutureProvider.family<Scolarite, int>((ref, eleveId) {
  return ref.watch(parentAppServiceProvider).getScolarite(eleveId);
});

final bulletinProvider = FutureProvider.family<Bulletin, int>((ref, eleveId) {
  return ref.watch(parentAppServiceProvider).getBulletin(eleveId);
});

final absencesProvider = FutureProvider.family<List<Absence>, int>((ref, eleveId) {
  return ref.watch(parentAppServiceProvider).getAbsences(eleveId);
});

final emploiProvider = FutureProvider.family<List<SeanceEmploi>, int>((ref, eleveId) {
  return ref.watch(parentAppServiceProvider).getEmploiDuTemps(eleveId);
});

final factureDetailProvider = FutureProvider.family<Facture, int>((ref, id) {
  return ref.watch(parentAppServiceProvider).getFacture(id);
});
