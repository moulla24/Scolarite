import { inject, Injectable } from '@angular/core';
import {
  BehaviorSubject,
  combineLatest,
  distinctUntilChanged,
  map,
  Observable,
  shareReplay,
  switchMap,
  tap,
} from 'rxjs';
import { DASHBOARD_DATA_PROVIDER } from '../../../core/constants/app.constants';
import { DashboardStat } from '../../../core/models/dashboard-stats.model';
import { SchoolLevel } from '../../../core/models/school-level.model';
import { Transaction } from '../../../core/models/transaction.model';
import { DashboardDataProvider } from '../../../core/services/interfaces/dashboard-data-provider.interface';

export interface DashboardViewState {
  readonly stats: readonly DashboardStat[];
  readonly transactions: readonly Transaction[];
  readonly levels: readonly SchoolLevel[];
  readonly selectedLevelId: string;
  readonly selectedLevelLabel: string;
  readonly loading: boolean;
}

@Injectable()
export class DashboardFacadeService {
  private readonly dataProvider = inject<DashboardDataProvider>(DASHBOARD_DATA_PROVIDER);
  private readonly selectedLevel$ = new BehaviorSubject<string>('all');
  private readonly loading$ = new BehaviorSubject(false);

  private readonly levels$ = this.dataProvider.getSchoolLevels().pipe(shareReplay(1));

  readonly viewState$: Observable<DashboardViewState> = this.selectedLevel$.pipe(
    distinctUntilChanged(),
    tap(() => this.loading$.next(true)),
    switchMap((levelId) => {
      const niveauParam = levelId === 'all' ? null : levelId;
      return combineLatest([
        this.levels$,
        this.dataProvider.getStats(niveauParam),
        this.dataProvider.getTransactions(niveauParam),
      ]).pipe(
        map(([levels, stats, transactions]) => ({
          stats,
          transactions,
          levels,
          selectedLevelId: levelId,
          selectedLevelLabel:
            levels.find((l) => l.id === levelId)?.label ?? 'Tous les niveaux',
          loading: false,
        }))
      );
    }),
    tap(() => this.loading$.next(false)),
    shareReplay({ bufferSize: 1, refCount: true })
  );

  setLevel(levelId: string): void {
    this.selectedLevel$.next(levelId);
  }
}
