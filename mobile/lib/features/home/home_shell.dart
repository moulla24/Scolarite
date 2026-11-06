import 'package:ecoli_parent/providers/parent_cache.dart';
import 'package:ecoli_parent/providers/parent_polling.dart';
import 'package:ecoli_parent/providers/providers.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

class HomeShell extends ConsumerWidget {
  const HomeShell({super.key, required this.child});

  final Widget child;

  int _index(String location) {
    if (location.startsWith('/home/enfants')) return 1;
    if (location.startsWith('/home/notifications')) return 2;
    if (location.startsWith('/home/factures')) return 3;
    if (location.startsWith('/home/paiements')) return 4;
    return 0;
  }

  void _onTabSelected(int i, BuildContext context, WidgetRef ref) {
    refreshParentData(ref);
    switch (i) {
      case 0:
        context.go('/home/dashboard');
      case 1:
        context.go('/home/enfants');
      case 2:
        context.go('/home/notifications');
      case 3:
        context.go('/home/factures');
      case 4:
        context.go('/home/paiements');
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    ref.watch(parentLiveSyncProvider);
    final location = GoRouterState.of(context).matchedLocation;
    final user = ref.watch(authStateProvider).user;
    final unread = ref.watch(unreadCountProvider);

    return Scaffold(
      appBar: AppBar(
        title: Text(_title(location)),
        actions: [
          if (user != null)
            Padding(
              padding: const EdgeInsets.only(right: 8),
              child: CircleAvatar(
                child: Text(user.avatarInitials),
              ),
            ),
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: () async {
              await ref.read(authStateProvider.notifier).logout();
            },
          ),
        ],
      ),
      body: child,
      bottomNavigationBar: NavigationBar(
        selectedIndex: _index(location),
        onDestinationSelected: (i) => _onTabSelected(i, context, ref),
        destinations: [
          const NavigationDestination(
            icon: Icon(Icons.dashboard_outlined),
            selectedIcon: Icon(Icons.dashboard),
            label: 'Accueil',
          ),
          const NavigationDestination(
            icon: Icon(Icons.child_care_outlined),
            selectedIcon: Icon(Icons.child_care),
            label: 'Enfants',
          ),
          NavigationDestination(
            icon: Badge(
              isLabelVisible: unread.maybeWhen(
                data: (c) => c > 0,
                orElse: () => false,
              ),
              label: unread.maybeWhen(data: (c) => Text('$c'), orElse: () => null),
              child: const Icon(Icons.notifications_outlined),
            ),
            selectedIcon: const Icon(Icons.notifications),
            label: 'Notifs',
          ),
          const NavigationDestination(
            icon: Icon(Icons.receipt_long_outlined),
            selectedIcon: Icon(Icons.receipt_long),
            label: 'Factures',
          ),
          const NavigationDestination(
            icon: Icon(Icons.payments_outlined),
            selectedIcon: Icon(Icons.payments),
            label: 'Paiements',
          ),
        ],
      ),
    );
  }

  String _title(String location) {
    if (location.startsWith('/home/enfants/')) return 'Détail enfant';
    if (location.startsWith('/home/enfants')) return 'Mes enfants';
    if (location.startsWith('/home/notifications')) return 'Notifications';
    if (location.startsWith('/home/factures')) return 'Factures';
    if (location.startsWith('/home/paiements')) return 'Mes paiements';
    return 'Accueil';
  }
}
