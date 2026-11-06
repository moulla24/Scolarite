import 'dart:async';

import 'package:ecoli_parent/models/parent_models.dart';
import 'package:ecoli_parent/providers/parent_cache.dart';
import 'package:ecoli_parent/providers/parent_polling.dart';
import 'package:ecoli_parent/providers/providers.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

class PaymentProcessingScreen extends ConsumerStatefulWidget {
  const PaymentProcessingScreen({super.key, required this.demandeId});

  final int demandeId;

  @override
  ConsumerState<PaymentProcessingScreen> createState() =>
      _PaymentProcessingScreenState();
}

class _PaymentProcessingScreenState extends ConsumerState<PaymentProcessingScreen> {
  DemandePaiement? _demande;
  int _delaiTotal = 30;
  int _secondesRestantes = 30;
  Timer? _countdown;
  Timer? _poll;
  String? _error;
  String? _lastStatut;

  @override
  void initState() {
    super.initState();
    _init();
  }

  Future<void> _init() async {
    try {
      final config = await ref.read(parentAppServiceProvider).getPaiementConfig();
      final delai = config['delaiSecondes'] ?? 30;
      setState(() {
        _delaiTotal = delai;
        _secondesRestantes = delai;
      });
      await _refresh();
      _startCountdown();
      _startPolling();
    } catch (e) {
      setState(() => _error = e.toString());
    }
  }

  void _startCountdown() {
    _countdown?.cancel();
    _countdown = Timer.periodic(const Duration(seconds: 1), (t) {
      if (!mounted) return;
      if (_secondesRestantes > 0) {
        setState(() => _secondesRestantes--);
      } else {
        t.cancel();
      }
    });
  }

  void _startPolling() {
    _poll?.cancel();
    _poll = Timer.periodic(const Duration(seconds: 2), (_) => _refresh());
  }

  Future<void> _refresh() async {
    try {
      final d = await ref
          .read(parentAppServiceProvider)
          .getDemandePaiement(widget.demandeId);
      if (!mounted) return;
      final statutChanged = _lastStatut != null && _lastStatut != d.statut;
      setState(() {
        _demande = d;
        _error = null;
        _lastStatut = d.statut;
      });
      if (statutChanged || d.statut == 'VALIDE') {
        refreshParentData(ref, factureId: d.factureId);
      } else {
        bumpParentDataPoll(ref);
      }
      if (d.statut == 'VALIDE') {
        _poll?.cancel();
        _countdown?.cancel();
        _showConfirmed(d);
      } else if (d.statut == 'REFUSE') {
        _poll?.cancel();
        _countdown?.cancel();
      }
    } catch (e) {
      if (mounted) setState(() => _error = e.toString());
    }
  }

  void _showConfirmed(DemandePaiement d) {
    showDialog<void>(
      context: context,
      barrierDismissible: false,
      builder: (ctx) => AlertDialog(
        icon: const Icon(Icons.check_circle, color: Colors.green, size: 48),
        title: const Text('Paiement confirmé'),
        content: Text(
          'Votre paiement est validé.\nReçu officiel n° ${d.numeroRecu ?? '—'}',
        ),
        actions: [
          FilledButton(
            onPressed: () {
              Navigator.of(ctx).pop();
              context.go('/home/paiements');
            },
            child: const Text('Voir mes paiements'),
          ),
        ],
      ),
    );
  }

  @override
  void dispose() {
    _countdown?.cancel();
    _poll?.cancel();
    super.dispose();
  }

  String _stepLabel() {
    final s = _demande?.statut;
    if (s == 'EN_COURS') {
      return 'Connexion à votre application bancaire…';
    }
    if (s == 'EN_ATTENTE') {
      return 'Notification envoyée au comptable — en attente de validation';
    }
    if (s == 'VALIDE') return 'Paiement confirmé';
    if (s == 'REFUSE') return 'Paiement refusé';
    return 'Traitement en cours…';
  }

  @override
  Widget build(BuildContext context) {
    final d = _demande;
    final progress = _delaiTotal > 0
        ? 1 - (_secondesRestantes / _delaiTotal)
        : 1.0;

    return Scaffold(
      appBar: AppBar(title: const Text('Paiement en cours')),
      body: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          children: [
            if (_error != null)
              Padding(
                padding: const EdgeInsets.only(bottom: 16),
                child: Text(_error!, style: const TextStyle(color: Colors.red)),
              ),
            const Icon(Icons.account_balance, size: 72, color: Colors.blue),
            const SizedBox(height: 24),
            Text(
              _stepLabel(),
              textAlign: TextAlign.center,
              style: Theme.of(context).textTheme.titleMedium,
            ),
            if (d != null) ...[
              const SizedBox(height: 8),
              Text(
                '${d.montant.toStringAsFixed(0)} MRU — ${d.factureNumero}',
                style: Theme.of(context).textTheme.bodyLarge,
              ),
            ],
            const SizedBox(height: 32),
            if (d?.statut == 'EN_COURS') ...[
              LinearProgressIndicator(value: progress.clamp(0.0, 1.0)),
              const SizedBox(height: 12),
              Text(
                'Simulation banque : $_secondesRestantes s',
                style: Theme.of(context).textTheme.bodySmall,
              ),
            ],
            if (d?.statut == 'EN_ATTENTE') ...[
              const CircularProgressIndicator(),
              const SizedBox(height: 12),
              const Text('Le comptable va confirmer votre paiement…'),
            ],
            if (d?.statut == 'REFUSE') ...[
              Icon(Icons.cancel, color: Colors.red.shade700, size: 48),
              const SizedBox(height: 8),
              Text(d?.motifRefus ?? 'Refusé par le comptable'),
            ],
            const Spacer(),
            if (d?.statut != 'VALIDE')
              TextButton(
                onPressed: () => context.go('/home/paiements'),
                child: const Text('Voir mes paiements'),
              ),
          ],
        ),
      ),
    );
  }
}
