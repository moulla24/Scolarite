import 'package:ecoli_parent/providers/parent_data_providers.dart';
import 'package:ecoli_parent/providers/parent_polling.dart';
import 'package:ecoli_parent/widgets/loading_error.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

class PaymentDemandsScreen extends ConsumerWidget {
  const PaymentDemandsScreen({super.key});

  String _statutLabel(String statut) => switch (statut) {
        'EN_COURS' => 'Banque (30 s)…',
        'EN_ATTENTE' => 'Chez le comptable',
        'VALIDE' => 'Confirmé',
        'REFUSE' => 'Refusée',
        _ => statut,
      };

  Color _statutColor(String statut) => switch (statut) {
        'VALIDE' => Colors.green,
        'REFUSE' => Colors.red,
        'EN_COURS' => Colors.blue,
        _ => Colors.orange,
      };

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final async = ref.watch(demandesPaiementProvider);

    ref.watch(parentLiveSyncProvider);
    return async.when(
      skipLoadingOnReload: true,
      data: (list) {
        if (list.isEmpty) {
          return const EmptyView(
            message: 'Aucune déclaration de paiement',
            icon: Icons.payments_outlined,
          );
        }
        return RefreshIndicator(
          onRefresh: () async => bumpParentDataPoll(ref),
          child: ListView.builder(
            padding: const EdgeInsets.all(16),
            itemCount: list.length,
            itemBuilder: (_, i) {
              final d = list[i];
              return Card(
                margin: const EdgeInsets.only(bottom: 8),
                child: ListTile(
                  title: Text('${d.factureNumero} — ${d.eleveNom}'),
                  subtitle: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        '${d.montant.toStringAsFixed(0)} MRU · ${d.modePaiement}',
                      ),
                      Text('Déclaré le ${d.dateCreation.substring(0, 10)}'),
                      if (d.motifRefus != null)
                        Text(
                          'Motif : ${d.motifRefus}',
                          style: TextStyle(color: Colors.red.shade700),
                        ),
                      if (d.numeroRecu != null)
                        Text('Reçu : ${d.numeroRecu}'),
                    ],
                  ),
                  isThreeLine: true,
                  trailing: Container(
                    padding:
                        const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                    decoration: BoxDecoration(
                      color: _statutColor(d.statut).withValues(alpha: 0.15),
                      borderRadius: BorderRadius.circular(4),
                    ),
                    child: Text(
                      _statutLabel(d.statut),
                      style: TextStyle(
                        color: _statutColor(d.statut),
                        fontSize: 12,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ),
                  onTap: () {
                    if (d.statut == 'EN_COURS' || d.statut == 'EN_ATTENTE') {
                      context.push('/paiements/${d.id}/suivi');
                    }
                  },
                ),
              );
            },
          ),
        );
      },
      loading: () => const LoadingView(),
      error: (e, _) => ErrorView(
        message: e.toString(),
        onRetry: () => bumpParentDataPoll(ref),
      ),
    );
  }
}
