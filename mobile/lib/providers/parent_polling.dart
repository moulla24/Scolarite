import 'dart:async';

import 'package:ecoli_parent/providers/providers.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

/// Compteur incrémenté pour forcer le rechargement des FutureProvider parent.
final parentDataPollTickProvider = StateProvider<int>((ref) => 0);

void bumpParentDataPoll(WidgetRef ref) {
  ref.read(parentDataPollTickProvider.notifier).update((v) => v + 1);
}

void bumpParentDataPollRef(Ref ref) {
  ref.read(parentDataPollTickProvider.notifier).update((v) => v + 1);
}

/// Synchronisation périodique tant que l'utilisateur est connecté dans l'app.
final parentLiveSyncProvider = Provider<void>((ref) {
  final loggedIn = ref.watch(authStateProvider).isLoggedIn;
  if (!loggedIn) return;

  final timer = Timer.periodic(const Duration(seconds: 5), (_) {
    bumpParentDataPollRef(ref);
  });

  ref.onDispose(timer.cancel);
});
