import 'package:ecoli_parent/core/theme/app_theme.dart';
import 'package:ecoli_parent/providers/providers.dart';
import 'package:ecoli_parent/router/app_router.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const ProviderScope(child: EcoliParentApp()));
}

class EcoliParentApp extends ConsumerWidget {
  const EcoliParentApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final router = ref.watch(routerProvider);

    return MaterialApp.router(
      title: 'Ecoli Parents',
      theme: AppTheme.light,
      routerConfig: router,
      debugShowCheckedModeBanner: false,
    );
  }
}
