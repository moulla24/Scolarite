import 'package:ecoli_parent/core/theme/app_colors.dart';
import 'package:ecoli_parent/models/parent_models.dart';
import 'package:ecoli_parent/providers/enfant_providers.dart';
import 'package:ecoli_parent/widgets/loading_error.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

class EnfantDetailScreen extends ConsumerWidget {
  const EnfantDetailScreen({super.key, required this.eleveId});

  final int eleveId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return DefaultTabController(
      length: 4,
      child: Column(
        children: [
          const TabBar(
            isScrollable: true,
            tabs: [
              Tab(text: 'Scolarité'),
              Tab(text: 'Notes'),
              Tab(text: 'Absences'),
              Tab(text: 'Emploi'),
            ],
          ),
          Expanded(
            child: TabBarView(
              children: [
                _ScolariteTab(eleveId: eleveId),
                _BulletinTab(eleveId: eleveId),
                _AbsencesTab(eleveId: eleveId),
                _EmploiTab(eleveId: eleveId),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _ScolariteTab extends ConsumerWidget {
  const _ScolariteTab({required this.eleveId});
  final int eleveId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final async = ref.watch(scolariteProvider(eleveId));

    return async.when(
      data: (s) => ListView(
        padding: const EdgeInsets.all(16),
        children: [
          _Section('Élève', [
            _row('Nom', s.nomComplet),
            _row('Matricule', s.matricule),
          ]),
          _Section('École', [
            _row('Nom', s.ecoleNom),
            _row('Code', s.ecoleCode),
            if (s.ecoleTelephone != null) _row('Téléphone', s.ecoleTelephone!),
            if (s.ecoleEmail != null) _row('Email', s.ecoleEmail!),
          ]),
          _Section('Classe', [
            _row('Classe', s.classe),
            _row('Niveau', s.niveau),
            if (s.salle != null) _row('Salle', s.salle!),
          ]),
          _Section('Année scolaire', [
            _row('Année', s.anneeScolaire),
            if (s.anneeDebut != null) _row('Début', s.anneeDebut!),
            if (s.anneeFin != null) _row('Fin', s.anneeFin!),
            _row('Inscription', s.statutInscription),
          ]),
        ],
      ),
      loading: () => const LoadingView(),
      error: (e, _) => ErrorView(message: e.toString()),
    );
  }

  Widget _row(String label, String value) => Padding(
        padding: const EdgeInsets.symmetric(vertical: 4),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            SizedBox(
                width: 100,
                child: Text(label,
                    style: const TextStyle(fontWeight: FontWeight.w600))),
            Expanded(child: Text(value)),
          ],
        ),
      );
}

class _Section extends StatelessWidget {
  const _Section(this.title, this.children);
  final String title;
  final List<Widget> children;

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(title,
                style: Theme.of(context)
                    .textTheme
                    .titleMedium
                    ?.copyWith(color: AppColors.primary)),
            const Divider(),
            ...children,
          ],
        ),
      ),
    );
  }
}

class _BulletinTab extends ConsumerWidget {
  const _BulletinTab({required this.eleveId});
  final int eleveId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final async = ref.watch(bulletinProvider(eleveId));

    return async.when(
      data: (b) {
        if (b.lignes.isEmpty) {
          return const EmptyView(message: 'Aucune note pour le moment', icon: Icons.grade);
        }
        return ListView.builder(
          padding: const EdgeInsets.all(16),
          itemCount: b.lignes.length,
          itemBuilder: (_, i) {
            final l = b.lignes[i];
            final pct = l.noteMax > 0 ? (l.noteObtenue / l.noteMax * 100) : 0.0;
            return Card(
              margin: const EdgeInsets.only(bottom: 8),
              child: ListTile(
                title: Text(l.matiere),
                subtitle: Text('${l.type} · ${l.dateEvaluation ?? ''}'),
                trailing: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Text(
                      '${l.noteObtenue}/${l.noteMax}',
                      style: const TextStyle(
                          fontWeight: FontWeight.bold, fontSize: 16),
                    ),
                    Text('${pct.toStringAsFixed(0)}%',
                        style: const TextStyle(fontSize: 12)),
                  ],
                ),
              ),
            );
          },
        );
      },
      loading: () => const LoadingView(),
      error: (e, _) => ErrorView(message: e.toString()),
    );
  }
}

class _AbsencesTab extends ConsumerWidget {
  const _AbsencesTab({required this.eleveId});
  final int eleveId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final async = ref.watch(absencesProvider(eleveId));

    return async.when(
      data: (list) {
        if (list.isEmpty) {
          return const EmptyView(message: 'Aucune saisie de présence', icon: Icons.event_busy);
        }
        return ListView.builder(
          padding: const EdgeInsets.all(16),
          itemCount: list.length,
          itemBuilder: (_, i) {
            final a = list[i];
            return Card(
              color: a.isAbsent ? Colors.red.shade50 : null,
              margin: const EdgeInsets.only(bottom: 8),
              child: ListTile(
                leading: Icon(
                  a.isAbsent ? Icons.cancel : Icons.check_circle,
                  color: a.isAbsent ? Colors.red : Colors.green,
                ),
                title: Text(a.matiere),
                subtitle: Text('${a.date} · ${a.horaire}'),
                trailing: Text(
                  a.isAbsent ? 'Absent' : 'Présent',
                  style: TextStyle(
                    fontWeight: FontWeight.bold,
                    color: a.isAbsent ? Colors.red : Colors.green,
                  ),
                ),
              ),
            );
          },
        );
      },
      loading: () => const LoadingView(),
      error: (e, _) => ErrorView(message: e.toString()),
    );
  }
}

class _EmploiTab extends ConsumerWidget {
  const _EmploiTab({required this.eleveId});
  final int eleveId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final async = ref.watch(emploiProvider(eleveId));

    return async.when(
      data: (list) {
        if (list.isEmpty) {
          return const EmptyView(message: 'Emploi du temps non disponible', icon: Icons.calendar_today);
        }
        final byDay = <int, List<SeanceEmploi>>{};
        for (final s in list) {
          byDay.putIfAbsent(s.jourIndex, () => []).add(s);
        }
        final days = byDay.keys.toList()..sort();
        return ListView.builder(
          padding: const EdgeInsets.all(16),
          itemCount: days.length,
          itemBuilder: (_, i) {
            final day = days[i];
            final seances = byDay[day]!;
            return Card(
              margin: const EdgeInsets.only(bottom: 12),
              child: Padding(
                padding: const EdgeInsets.all(12),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(seances.first.jour,
                        style: const TextStyle(
                            fontWeight: FontWeight.bold, fontSize: 16)),
                    const Divider(),
                    ...seances.map((s) => Padding(
                          padding: const EdgeInsets.symmetric(vertical: 4),
                          child: Row(
                            children: [
                              SizedBox(
                                  width: 100,
                                  child: Text(s.horaire,
                                      style: const TextStyle(fontSize: 12))),
                              Expanded(
                                child: Text('${s.matiere} — ${s.enseignant}'),
                              ),
                            ],
                          ),
                        )),
                  ],
                ),
              ),
            );
          },
        );
      },
      loading: () => const LoadingView(),
      error: (e, _) => ErrorView(message: e.toString()),
    );
  }
}
