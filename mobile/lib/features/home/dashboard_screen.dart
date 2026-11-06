import 'package:ecoli_parent/core/theme/app_colors.dart';
import 'package:ecoli_parent/models/parent_models.dart';
import 'package:ecoli_parent/providers/parent_polling.dart';
import 'package:ecoli_parent/providers/providers.dart';
import 'package:ecoli_parent/widgets/loading_error.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

final dashboardProvider = FutureProvider<ParentDashboard>((ref) async {
  ref.watch(parentDataPollTickProvider);
  if (!ref.watch(authStateProvider).isLoggedIn) {
    throw StateError('Non connecté');
  }
  return ref.read(parentAppServiceProvider).getDashboard();
});

final enfantsDashboardProvider = FutureProvider<List<EnfantResume>>((ref) async {
  ref.watch(parentDataPollTickProvider);
  if (!ref.watch(authStateProvider).isLoggedIn) return [];
  return ref.read(parentAppServiceProvider).getEnfants();
});

class DashboardScreen extends ConsumerWidget {
  const DashboardScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final dash = ref.watch(dashboardProvider);
    final enfants = ref.watch(enfantsDashboardProvider);
    final user = ref.watch(authStateProvider).user;

    return RefreshIndicator(
      onRefresh: () async {
        bumpParentDataPoll(ref);
      },
      child: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          if (user != null)
            Text(
              'Bonjour, ${user.prenom}',
              style: Theme.of(context).textTheme.titleLarge,
            ),
          const SizedBox(height: 16),
          dash.when(
            skipLoadingOnReload: true,
            data: (d) => _SummaryCards(dashboard: d),
            loading: () => const LoadingView(),
            error: (e, _) => ErrorView(
              message: e.toString(),
              onRetry: () => ref.invalidate(dashboardProvider),
            ),
          ),
          const SizedBox(height: 24),
          Text('Mes enfants', style: Theme.of(context).textTheme.titleMedium),
          const SizedBox(height: 8),
          enfants.when(
            data: (list) => list.isEmpty
                ? const EmptyView(message: 'Aucun enfant enregistré')
                : Column(
                    children: list
                        .map((e) => _EnfantCard(enfant: e))
                        .toList(),
                  ),
            loading: () => const LoadingView(),
            error: (e, _) => ErrorView(message: e.toString()),
          ),
        ],
      ),
    );
  }
}

class _SummaryCards extends StatelessWidget {
  const _SummaryCards({required this.dashboard});

  final ParentDashboard dashboard;

  @override
  Widget build(BuildContext context) {
    final enRetard = dashboard.statutCompte == 'EN_RETARD';
    return Row(
      children: [
        Expanded(
          child: Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                children: [
                  const Icon(Icons.child_care, color: Color(0xFF0D7C66)),
                  const SizedBox(height: 8),
                  Text('${dashboard.nombreEnfants}',
                      style: Theme.of(context).textTheme.headlineMedium),
                  const Text('Enfant(s)'),
                ],
              ),
            ),
          ),
        ),
        const SizedBox(width: 8),
        Expanded(
          child: Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                children: [
                  Icon(Icons.notifications,
                      color: dashboard.notificationsNonLues > 0
                          ? Colors.orange
                          : Colors.grey),
                  const SizedBox(height: 8),
                  Text('${dashboard.notificationsNonLues}',
                      style: Theme.of(context).textTheme.headlineMedium),
                  const Text('Notifications'),
                ],
              ),
            ),
          ),
        ),
        const SizedBox(width: 8),
        Expanded(
          child: Card(
            color: enRetard ? Colors.red.shade50 : null,
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                children: [
                  Icon(Icons.account_balance_wallet,
                      color: enRetard ? Colors.red : AppColors.primary),
                  const SizedBox(height: 8),
                  Text(
                    '${dashboard.soldeGlobal.toStringAsFixed(0)}',
                    style: Theme.of(context).textTheme.titleMedium,
                  ),
                  Text(enRetard ? 'En retard' : 'À jour',
                      style: TextStyle(
                          fontSize: 11,
                          color: enRetard ? Colors.red : Colors.green)),
                ],
              ),
            ),
          ),
        ),
      ],
    );
  }
}

class _EnfantCard extends StatelessWidget {
  const _EnfantCard({required this.enfant});

  final EnfantResume enfant;

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      child: ListTile(
        leading: CircleAvatar(
          child: Text(enfant.nomComplet.isNotEmpty ? enfant.nomComplet[0] : '?'),
        ),
        title: Text(enfant.nomComplet),
        subtitle: Text('${enfant.classe} · ${enfant.ecoleNom}'),
        trailing: enfant.statutFacturation == 'EN_RETARD'
            ? const Icon(Icons.warning, color: Colors.orange)
            : const Icon(Icons.chevron_right),
        onTap: () => context.go('/home/enfants/${enfant.id}'),
      ),
    );
  }
}
