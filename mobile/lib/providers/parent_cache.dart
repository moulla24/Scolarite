import 'package:ecoli_parent/providers/enfant_providers.dart';
import 'package:ecoli_parent/providers/parent_polling.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

/// Force le rechargement immédiat des données parent (factures, notifications…).
void refreshParentData(WidgetRef ref, {int? factureId}) {
  bumpParentDataPoll(ref);
  if (factureId != null) {
    ref.invalidate(factureDetailProvider(factureId));
  }
}
