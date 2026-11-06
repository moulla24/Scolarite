import 'package:ecoli_parent/core/theme/app_colors.dart';
import 'package:ecoli_parent/models/parent_models.dart';
import 'package:ecoli_parent/providers/providers.dart';
import 'package:ecoli_parent/widgets/loading_error.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

final enfantsListProvider = FutureProvider<List<EnfantResume>>((ref) async {
  return ref.watch(parentAppServiceProvider).getEnfants();
});

class EnfantsListScreen extends ConsumerWidget {
  const EnfantsListScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final async = ref.watch(enfantsListProvider);

    return async.when(
      data: (list) {
        if (list.isEmpty) {
          return const EmptyView(message: 'Aucun enfant', icon: Icons.child_care);
        }
        return RefreshIndicator(
          onRefresh: () async => ref.invalidate(enfantsListProvider),
          child: ListView.builder(
            padding: const EdgeInsets.all(16),
            itemCount: list.length,
            itemBuilder: (_, i) {
              final e = list[i];
              return Card(
                margin: const EdgeInsets.only(bottom: 12),
                child: ListTile(
                  leading: CircleAvatar(
                    backgroundColor: AppColors.primary,
                    foregroundColor: Colors.white,
                    child: Text(e.nomComplet[0]),
                  ),
                  title: Text(e.nomComplet, style: const TextStyle(fontWeight: FontWeight.bold)),
                  subtitle: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(e.classe),
                      Text(e.ecoleNom, style: const TextStyle(fontSize: 12)),
                      Text('Matricule : ${e.matricule}',
                          style: const TextStyle(fontSize: 12)),
                    ],
                  ),
                  isThreeLine: true,
                  trailing: const Icon(Icons.arrow_forward_ios, size: 16),
                  onTap: () => context.go('/home/enfants/${e.id}'),
                ),
              );
            },
          ),
        );
      },
      loading: () => const LoadingView(),
      error: (e, _) => ErrorView(
        message: e.toString(),
        onRetry: () => ref.invalidate(enfantsListProvider),
      ),
    );
  }
}
