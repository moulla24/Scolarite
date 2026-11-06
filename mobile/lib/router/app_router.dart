import 'package:ecoli_parent/features/auth/login_screen.dart';
import 'package:ecoli_parent/features/enfants/enfant_detail_screen.dart';
import 'package:ecoli_parent/features/enfants/enfants_list_screen.dart';
import 'package:ecoli_parent/features/factures/facture_detail_screen.dart';
import 'package:ecoli_parent/features/factures/factures_screen.dart';
import 'package:ecoli_parent/features/home/dashboard_screen.dart';
import 'package:ecoli_parent/features/home/home_shell.dart';
import 'package:ecoli_parent/features/notifications/notifications_screen.dart';
import 'package:ecoli_parent/features/paiements/declare_payment_screen.dart';
import 'package:ecoli_parent/features/paiements/payment_demands_screen.dart';
import 'package:ecoli_parent/features/paiements/payment_processing_screen.dart';
import 'package:ecoli_parent/features/splash/splash_screen.dart';
import 'package:ecoli_parent/providers/providers.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

final routerProvider = Provider<GoRouter>((ref) {
  final auth = ref.watch(authStateProvider);

  return GoRouter(
    initialLocation: '/splash',
    refreshListenable: _AuthRefresh(ref),
    redirect: (context, state) {
      final loc = state.matchedLocation;
      final ready = auth.initialized;
      final loggedIn = auth.isLoggedIn;

      if (!ready) {
        return loc == '/splash' ? null : '/splash';
      }
      if (!loggedIn) {
        return loc == '/login' ? null : '/login';
      }
      if (loc == '/splash' || loc == '/login') return '/home';
      return null;
    },
    routes: [
      GoRoute(path: '/splash', builder: (_, __) => const SplashScreen()),
      GoRoute(path: '/login', builder: (_, __) => const LoginScreen()),
      ShellRoute(
        builder: (_, __, child) => HomeShell(child: child),
        routes: [
          GoRoute(
            path: '/home',
            redirect: (_, __) => '/home/dashboard',
          ),
          GoRoute(
            path: '/home/dashboard',
            builder: (_, __) => const DashboardScreen(),
          ),
          GoRoute(
            path: '/home/enfants',
            builder: (_, __) => const EnfantsListScreen(),
          ),
          GoRoute(
            path: '/home/enfants/:id',
            builder: (_, state) => EnfantDetailScreen(
              eleveId: int.parse(state.pathParameters['id']!),
            ),
          ),
          GoRoute(
            path: '/home/notifications',
            builder: (_, __) => const NotificationsScreen(),
          ),
          GoRoute(
            path: '/home/factures',
            builder: (_, __) => const FacturesScreen(),
          ),
          GoRoute(
            path: '/home/paiements',
            builder: (_, __) => const PaymentDemandsScreen(),
          ),
        ],
      ),
      GoRoute(
        path: '/factures/:id',
        builder: (_, state) => FactureDetailScreen(
          factureId: int.parse(state.pathParameters['id']!),
        ),
      ),
      GoRoute(
        path: '/factures/:id/declarer',
        builder: (_, state) => DeclarePaymentScreen(
          factureId: int.parse(state.pathParameters['id']!),
        ),
      ),
      GoRoute(
        path: '/paiements/:id/suivi',
        builder: (_, state) => PaymentProcessingScreen(
          demandeId: int.parse(state.pathParameters['id']!),
        ),
      ),
    ],
  );
});

class _AuthRefresh extends ChangeNotifier {
  _AuthRefresh(this._ref) {
    _ref.listen(authStateProvider, (_, __) => notifyListeners());
  }
  final Ref _ref;
}
