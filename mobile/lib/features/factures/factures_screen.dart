import 'package:ecoli_parent/models/parent_models.dart';
import 'package:ecoli_parent/providers/parent_polling.dart';
import 'package:ecoli_parent/providers/providers.dart';
import 'package:ecoli_parent/widgets/loading_error.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

final facturesProvider = FutureProvider<List<Facture>>((ref) async {
  ref.watch(parentDataPollTickProvider);
  if (!ref.watch(authStateProvider).isLoggedIn) return [];
  return ref.read(parentAppServiceProvider).getFactures();
});

class FacturesScreen extends ConsumerWidget {
  const FacturesScreen({super.key});

  Color _statutColor(String statut) => switch (statut) {
        'PAYEE' => Colors.green,
        'PARTIELLE' => Colors.orange,
        _ => Colors.red,
      };

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final async = ref.watch(facturesProvider);

    return async.when(
      skipLoadingOnReload: true,
      data: (list) {
        if (list.isEmpty) {
          return const EmptyView(message: 'Aucune facture', icon: Icons.receipt_long);
        }
        return RefreshIndicator(
          onRefresh: () async => bumpParentDataPoll(ref),
          child: ListView.builder(
            padding: const EdgeInsets.all(16),
            itemCount: list.length,
            itemBuilder: (_, i) {
              final f = list[i];
              return Card(
                margin: const EdgeInsets.only(bottom: 8),
                child: ListTile(
                  title: Text(f.numero),
                  subtitle: Text('${f.eleveNom}\nÉchéance : ${f.dateEcheance}'),
                  isThreeLine: true,
                  trailing: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    crossAxisAlignment: CrossAxisAlignment.end,
                    children: [
                      Text(
                        '${f.resteAPayer.toStringAsFixed(0)} MRU',
                        style: const TextStyle(fontWeight: FontWeight.bold),
                      ),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                        decoration: BoxDecoration(
                          color: _statutColor(f.statut).withValues(alpha: 0.15),
                          borderRadius: BorderRadius.circular(4),
                        ),
                        child: Text(
                          f.statut,
                          style: TextStyle(
                              fontSize: 10, color: _statutColor(f.statut)),
                        ),
                      ),
                    ],
                  ),
                  onTap: () => context.push('/factures/${f.id}'),
                ),
              );
            },
          ),
        );
      },
      loading: () => const LoadingView(),
      error: (e, _) => ErrorView(
        message: e.toString(),
        onRetry: () => ref.invalidate(facturesProvider),
      ),
    );
  }
}
