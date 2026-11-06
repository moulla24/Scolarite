import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router, RouterLink } from '@angular/router';
import { forkJoin, interval } from 'rxjs';
import { startWith, switchMap } from 'rxjs/operators';
import { ComptabiliteStatsDto, DashboardFinancierDto, NotificationComptableDto } from '../../../../core/api/api-dto';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { CurrencyMruPipe } from '../../../../shared/pipes/currency-mru.pipe';
import { ComptabiliteApiService } from '../../services/comptabilite-api.service';
import { ComptabiliteRefreshService } from '../../services/comptabilite-refresh.service';

@Component({
  selector: 'app-comptabilite-dashboard-page',
  standalone: true,
  imports: [DatePipe, RouterLink, PageHeaderComponent, CurrencyMruPipe],
  templateUrl: './comptabilite-dashboard-page.component.html',
  styleUrl: './comptabilite-dashboard-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ComptabiliteDashboardPageComponent implements OnInit {
  private readonly api = inject(ComptabiliteApiService);
  private readonly router = inject(Router);
  private readonly refreshBus = inject(ComptabiliteRefreshService);
  private readonly destroyRef = inject(DestroyRef);

  readonly stats = signal<ComptabiliteStatsDto | null>(null);
  readonly finance = signal<DashboardFinancierDto | null>(null);
  readonly notifications = signal<readonly NotificationComptableDto[]>([]);
  readonly loading = signal(true);

  ngOnInit(): void {
    interval(8000)
      .pipe(
        startWith(0),
        switchMap(() =>
          forkJoin({
            stats: this.api.getStats(),
            finance: this.api.getDashboardFinance(),
            notifications: this.api.listNotifications(),
          })
        ),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: ({ stats, finance, notifications }) => {
          this.stats.set(stats);
          this.finance.set(finance);
          this.notifications.set(notifications.slice(0, 8));
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });

    this.refreshBus.changes$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => this.loadNow());
  }

  private loadNow(): void {
    forkJoin({
      stats: this.api.getStats(),
      finance: this.api.getDashboardFinance(),
      notifications: this.api.listNotifications(),
    }).subscribe({
      next: ({ stats, finance, notifications }) => {
        this.stats.set(stats);
        this.finance.set(finance);
        this.notifications.set(notifications.slice(0, 8));
      },
    });
  }

  markAllRead(): void {
    this.api.markAllNotificationsRead().subscribe({
      next: () => {
        this.notifications.update((list) => list.map((n) => ({ ...n, lu: true })));
        this.stats.update((s) => (s ? { ...s, notificationsNonLues: 0 } : s));
      },
    });
  }

  markRead(id: number): void {
    this.api.markNotificationRead(id).subscribe({
      next: () => {
        this.notifications.update((list) =>
          list.map((n) => (n.id === id ? { ...n, lu: true } : n))
        );
      },
    });
  }

  onNotificationClick(n: NotificationComptableDto): void {
    if (!n.lu) {
      this.markRead(n.id);
    }
    if (n.type === 'PAIEMENT_A_VALIDER' || n.demandePaiementId) {
      void this.router.navigate(['/comptabilite/validations']);
    }
  }

  maxRevenu(): number {
    const list = this.finance()?.revenusMensuels ?? [];
    return Math.max(...list.map((r) => r.montant), 1);
  }
}
