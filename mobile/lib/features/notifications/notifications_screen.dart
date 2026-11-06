import 'dart:async';

import 'package:ecoli_parent/core/theme/app_colors.dart';
import 'package:ecoli_parent/models/parent_models.dart';
import 'package:ecoli_parent/providers/parent_data_providers.dart';
import 'package:ecoli_parent/providers/parent_polling.dart';
import 'package:ecoli_parent/providers/providers.dart';
import 'package:ecoli_parent/widgets/loading_error.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

class NotificationsScreen extends ConsumerStatefulWidget {
  const NotificationsScreen({super.key});

  @override
  ConsumerState<NotificationsScreen> createState() =>
      _NotificationsScreenState();
}

class _NotificationsScreenState extends ConsumerState<NotificationsScreen>
    with WidgetsBindingObserver {
  Timer? _fastPoll;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _startFastPoll();
    WidgetsBinding.instance.addPostFrameCallback((_) => bumpParentDataPoll(ref));
  }

  @override
  void dispose() {
    _fastPoll?.cancel();
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      bumpParentDataPoll(ref);
    }
  }

  void _startFastPoll() {
    _fastPoll?.cancel();
    _fastPoll = Timer.periodic(const Duration(seconds: 4), (_) {
      if (mounted) bumpParentDataPoll(ref);
    });
  }

  IconData _icon(String type) => switch (type) {
        'NOTE' => Icons.grade,
        'ABSENCE' => Icons.event_busy,
        'FACTURE' => Icons.receipt,
        'PAIEMENT_EN_COURS' => Icons.hourglass_top,
        'PAIEMENT_VALIDE' => Icons.check_circle,
        'PAIEMENT_REFUSE' => Icons.cancel,
        'DEMANDE_PAIEMENT' => Icons.send,
        'INFO_PAIEMENT' => Icons.info,
        _ => Icons.notifications,
      };

  Color _color(String type) => switch (type) {
        'NOTE' => Colors.blue,
        'ABSENCE' => Colors.red,
        'FACTURE' => Colors.orange,
        'PAIEMENT_VALIDE' => Colors.green,
        'PAIEMENT_REFUSE' => Colors.red,
        'PAIEMENT_EN_COURS' => Colors.blue,
        'DEMANDE_PAIEMENT' => AppColors.primary,
        _ => AppColors.primary,
      };

  @override
  Widget build(BuildContext context) {
    ref.watch(parentLiveSyncProvider);
    final async = ref.watch(notificationsProvider);
    final service = ref.read(parentAppServiceProvider);

    return async.when(
      skipLoadingOnReload: true,
      data: (list) {
        return Column(
          children: [
            if (list.any((n) => !n.lu))
              Padding(
                padding: const EdgeInsets.all(8),
                child: Align(
                  alignment: Alignment.centerRight,
                  child: TextButton(
                    onPressed: () async {
                      await service.markAllNotificationsRead();
                      bumpParentDataPoll(ref);
                    },
                    child: const Text('Tout marquer lu'),
                  ),
                ),
              ),
            Expanded(
              child: list.isEmpty
                  ? const EmptyView(
                      message: 'Aucune notification',
                      icon: Icons.notifications_none,
                    )
                  : RefreshIndicator(
                      onRefresh: () async => bumpParentDataPoll(ref),
                      child: ListView.builder(
                        physics: const AlwaysScrollableScrollPhysics(),
                        padding: const EdgeInsets.all(16),
                        itemCount: list.length,
                        itemBuilder: (_, i) {
                          final n = list[i];
                          return Card(
                            color: n.lu ? null : AppColors.primaryBg,
                            margin: const EdgeInsets.only(bottom: 8),
                            child: ListTile(
                              leading: CircleAvatar(
                                backgroundColor:
                                    _color(n.type).withValues(alpha: 0.2),
                                child: Icon(_icon(n.type), color: _color(n.type)),
                              ),
                              title: Text(
                                n.titre,
                                style: TextStyle(
                                  fontWeight: n.lu
                                      ? FontWeight.normal
                                      : FontWeight.bold,
                                ),
                              ),
                              subtitle: Text(
                                n.message,
                                maxLines: 3,
                                overflow: TextOverflow.ellipsis,
                              ),
                              onTap: () async {
                                if (!n.lu) {
                                  await service.markNotificationRead(n.id);
                                  bumpParentDataPoll(ref);
                                }
                                if (n.eleveId != null && context.mounted) {
                                  context.go('/home/enfants/${n.eleveId}');
                                }
                              },
                            ),
                          );
                        },
                      ),
                    ),
            ),
          ],
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
