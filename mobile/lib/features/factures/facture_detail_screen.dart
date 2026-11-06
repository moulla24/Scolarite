import 'package:ecoli_parent/providers/enfant_providers.dart';
import 'package:ecoli_parent/widgets/loading_error.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

class FactureDetailScreen extends ConsumerWidget {
  const FactureDetailScreen({super.key, required this.factureId});

  final int factureId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final async = ref.watch(factureDetailProvider(factureId));

    return Scaffold(
      appBar: AppBar(title: const Text('Détail facture')),
      body: async.when(
        data: (f) => ListView(
          padding: const EdgeInsets.all(16),
          children: [
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(f.numero,
                        style: Theme.of(context).textTheme.titleLarge),
                    const SizedBox(height: 8),
                    Container(
                      width: double.infinity,
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: Colors.blue.shade50,
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const Text('Code de paiement',
                              style: TextStyle(fontWeight: FontWeight.w600)),
                          const SizedBox(height: 4),
                          Text(
                            f.codePaiement,
                            style: Theme.of(context)
                                .textTheme
                                .titleLarge
                                ?.copyWith(fontWeight: FontWeight.bold),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            'À utiliser comme référence lors du paiement.',
                            style: Theme.of(context).textTheme.bodySmall,
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(height: 12),
                    Text('Élève : ${f.eleveNom}'),
                    Text('Émission : ${f.dateEmission}'),
                    Text('Échéance : ${f.dateEcheance}'),
                    const Divider(height: 24),
                    _line('Montant total', f.montantTotal),
                    _line('Montant payé', f.montantPaye),
                    _line('Reste à payer', f.resteAPayer, bold: true),
                    const SizedBox(height: 8),
                    Chip(label: Text(f.statut)),
                    if (f.statut != 'PAYEE') ...[
                      const SizedBox(height: 16),
                      FilledButton.icon(
                        onPressed: () =>
                            context.push('/factures/${f.id}/declarer'),
                        icon: const Icon(Icons.payments),
                        label: const Text('Déclarer un paiement'),
                      ),
                    ],
                  ],
                ),
              ),
            ),
          ],
        ),
        loading: () => const LoadingView(),
        error: (e, _) => ErrorView(message: e.toString()),
      ),
    );
  }

  Widget _line(String label, double value, {bool bold = false}) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label),
          Text(
            '${value.toStringAsFixed(0)} MRU',
            style: TextStyle(fontWeight: bold ? FontWeight.bold : null),
          ),
        ],
      ),
    );
  }
}
