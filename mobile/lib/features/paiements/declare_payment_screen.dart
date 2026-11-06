import 'package:ecoli_parent/models/parent_models.dart';
import 'package:ecoli_parent/providers/enfant_providers.dart';
import 'package:ecoli_parent/providers/parent_cache.dart';
import 'package:ecoli_parent/providers/providers.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';

class DeclarePaymentScreen extends ConsumerStatefulWidget {
  const DeclarePaymentScreen({super.key, required this.factureId});

  final int factureId;

  @override
  ConsumerState<DeclarePaymentScreen> createState() =>
      _DeclarePaymentScreenState();
}

class _DeclarePaymentScreenState extends ConsumerState<DeclarePaymentScreen> {
  final _formKey = GlobalKey<FormState>();
  final _montantCtrl = TextEditingController();
  final _referenceCtrl = TextEditingController();
  final _preuveCtrl = TextEditingController();
  String _mode = 'MOBILE_MONEY';
  DateTime _date = DateTime.now();
  bool _saving = false;

  @override
  void dispose() {
    _montantCtrl.dispose();
    _referenceCtrl.dispose();
    _preuveCtrl.dispose();
    super.dispose();
  }

  Future<void> _submit(Facture facture) async {
    if (!_formKey.currentState!.validate()) return;
    setState(() => _saving = true);
    try {
      final request = DemandePaiementRequest(
        factureId: facture.id,
        montant: double.parse(_montantCtrl.text),
        datePaiement: DateFormat('yyyy-MM-dd').format(_date),
        modePaiement: _mode,
        reference: facture.codePaiement,
        preuveReference: _preuveCtrl.text.trim().isEmpty
            ? null
            : _preuveCtrl.text.trim(),
      );
      final created =
          await ref.read(parentAppServiceProvider).declarerPaiement(request);
      refreshParentData(ref, factureId: facture.id);
      if (!mounted) return;
      context.go('/paiements/${created.id}/suivi');
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(e.toString())),
      );
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final async = ref.watch(factureDetailProvider(widget.factureId));

    return Scaffold(
      appBar: AppBar(title: const Text('Déclarer un paiement')),
      body: async.when(
        data: (facture) {
          if (facture.statut == 'PAYEE') {
            return const Center(
              child: Text('Cette facture est déjà payée.'),
            );
          }
          if (_montantCtrl.text.isEmpty) {
            _montantCtrl.text = facture.resteAPayer.toStringAsFixed(0);
          }
          if (_referenceCtrl.text != facture.codePaiement) {
            _referenceCtrl.text = facture.codePaiement;
          }
          return Form(
            key: _formKey,
            child: ListView(
              padding: const EdgeInsets.all(16),
              children: [
                Card(
                  color: Colors.blue.shade50,
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(facture.numero,
                            style: Theme.of(context).textTheme.titleMedium),
                        Text('${facture.eleveNom}'),
                        Text(
                          'Reste à payer : ${facture.resteAPayer.toStringAsFixed(0)} MRU',
                          style: const TextStyle(fontWeight: FontWeight.bold),
                        ),
                        const Divider(height: 24),
                        Text(
                          'Code de paiement',
                          style: Theme.of(context).textTheme.labelLarge,
                        ),
                        const SizedBox(height: 4),
                        Row(
                          children: [
                            Expanded(
                              child: SelectableText(
                                facture.codePaiement,
                                style: Theme.of(context)
                                    .textTheme
                                    .headlineSmall
                                    ?.copyWith(
                                      fontWeight: FontWeight.bold,
                                      letterSpacing: 1.2,
                                    ),
                              ),
                            ),
                            IconButton(
                              tooltip: 'Copier le code',
                              onPressed: () {
                                Clipboard.setData(
                                    ClipboardData(text: facture.codePaiement));
                                ScaffoldMessenger.of(context).showSnackBar(
                                  const SnackBar(
                                    content: Text('Code copié'),
                                    duration: Duration(seconds: 1),
                                  ),
                                );
                              },
                              icon: const Icon(Icons.copy),
                            ),
                          ],
                        ),
                        const SizedBox(height: 4),
                        Text(
                          'Saisissez ce code comme référence dans votre banque ou Mobile Money.',
                          style: Theme.of(context).textTheme.bodySmall,
                        ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(height: 16),
                TextFormField(
                  controller: _montantCtrl,
                  decoration: const InputDecoration(
                    labelText: 'Montant (MRU) *',
                    border: OutlineInputBorder(),
                  ),
                  keyboardType: TextInputType.number,
                  validator: (v) {
                    final n = double.tryParse(v ?? '');
                    if (n == null || n <= 0) return 'Montant invalide';
                    if (n > facture.resteAPayer) {
                      return 'Dépasse le reste à payer';
                    }
                    return null;
                  },
                ),
                const SizedBox(height: 12),
                ListTile(
                  contentPadding: EdgeInsets.zero,
                  title: const Text('Date du paiement'),
                  subtitle: Text(DateFormat('dd/MM/yyyy').format(_date)),
                  trailing: const Icon(Icons.calendar_today),
                  onTap: () async {
                    final picked = await showDatePicker(
                      context: context,
                      initialDate: _date,
                      firstDate: DateTime(2020),
                      lastDate: DateTime.now(),
                    );
                    if (picked != null) setState(() => _date = picked);
                  },
                ),
                const SizedBox(height: 8),
                DropdownButtonFormField<String>(
                  value: _mode,
                  decoration: const InputDecoration(
                    labelText: 'Mode de paiement',
                    border: OutlineInputBorder(),
                  ),
                  items: const [
                    DropdownMenuItem(value: 'ESPECES', child: Text('Espèces')),
                    DropdownMenuItem(value: 'BANQUE', child: Text('Banque')),
                    DropdownMenuItem(
                        value: 'MOBILE_MONEY', child: Text('Mobile Money')),
                    DropdownMenuItem(value: 'CHEQUE', child: Text('Chèque')),
                  ],
                  onChanged: (v) => setState(() => _mode = v ?? 'MOBILE_MONEY'),
                ),
                const SizedBox(height: 12),
                TextFormField(
                  controller: _referenceCtrl,
                  readOnly: true,
                  decoration: const InputDecoration(
                    labelText: 'Référence transaction (code facture)',
                    border: OutlineInputBorder(),
                    filled: true,
                  ),
                ),
                const SizedBox(height: 12),
                TextFormField(
                  controller: _preuveCtrl,
                  decoration: const InputDecoration(
                    labelText: 'N° reçu banque / capture (optionnel)',
                    border: OutlineInputBorder(),
                  ),
                  maxLines: 2,
                ),
                const SizedBox(height: 24),
                FilledButton.icon(
                  onPressed: _saving ? null : () => _submit(facture),
                  icon: const Icon(Icons.account_balance),
                  label: Text(
                    _saving ? 'Connexion…' : 'Payer via application bancaire',
                  ),
                ),
              ],
            ),
          );
        },
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text(e.toString())),
      ),
    );
  }
}
