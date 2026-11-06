import { AsyncPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BehaviorSubject, combineLatest, map, startWith, switchMap } from 'rxjs';
import { FactureDetailDto, FactureListDto } from '../../../../core/api/api-dto';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { SearchFieldComponent } from '../../../../shared/components/search-field/search-field.component';
import { FilterCardComponent } from '../../../../shared/components/filter-card/filter-card.component';
import { FactureStatusBadgeComponent } from '../../../../shared/components/facture-status-badge/facture-status-badge.component';
import { BalanceAmountComponent } from '../../../../shared/components/balance-amount/balance-amount.component';
import { CurrencyMruPipe } from '../../../../shared/pipes/currency-mru.pipe';
import { ComptabiliteApiService } from '../../services/comptabilite-api.service';
import { ComptabiliteRefreshService } from '../../services/comptabilite-refresh.service';
import { ComptabiliteEleveFiltersComponent } from '../../components/comptabilite-eleve-filters/comptabilite-eleve-filters.component';
import {
  ComptabiliteEleveFilterState,
  emptyEleveFilters,
} from '../../components/comptabilite-eleve-filters/comptabilite-eleve-filters.model';

@Component({
  selector: 'app-factures-page',
  standalone: true,
  imports: [
    AsyncPipe,
    DatePipe,
    FormsModule,
    PageHeaderComponent,
    SearchFieldComponent,
    FilterCardComponent,
    FactureStatusBadgeComponent,
    BalanceAmountComponent,
    CurrencyMruPipe,
    ComptabiliteEleveFiltersComponent,
  ],
  templateUrl: './factures-page.component.html',
  styleUrl: './factures-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FacturesPageComponent {
  private readonly api = inject(ComptabiliteApiService);
  private readonly refreshBus = inject(ComptabiliteRefreshService);
  private readonly search$ = new BehaviorSubject('');
  private readonly statut$ = new BehaviorSubject('all');
  private readonly eleveFilters$ = new BehaviorSubject<ComptabiliteEleveFilterState>(emptyEleveFilters());
  private readonly refresh$ = new BehaviorSubject(0);

  readonly eleveFilters = signal<ComptabiliteEleveFilterState>(emptyEleveFilters());
  readonly selectedFacture = signal<FactureDetailDto | null>(null);
  readonly loadingDetail = signal(false);

  readonly factures$ = combineLatest([
    this.search$,
    this.statut$,
    this.eleveFilters$,
    this.refresh$,
    this.refreshBus.changes$.pipe(startWith(0)),
  ]).pipe(
    switchMap(([search, statut, filters, _refresh, _bus]) =>
      this.api.listFactures(
        statut === 'all' ? undefined : statut,
        search.trim() || undefined,
        filters.niveauId ? Number(filters.niveauId) : undefined,
        filters.classeId ? Number(filters.classeId) : undefined,
        filters.eleveId ? Number(filters.eleveId) : undefined
      )
    )
  );

  readonly viewState$ = combineLatest([
    this.factures$,
    this.search$,
    this.statut$,
    this.eleveFilters$,
  ]).pipe(
    map(([factures, search, statut, eleveFilters]) => ({
      factures,
      search,
      statut,
      eleveFilters,
      count: factures.length,
    }))
  );

  onSearch(q: string): void {
    this.search$.next(q);
  }

  onStatutChange(statut: string): void {
    this.statut$.next(statut);
  }

  onEleveFiltersChange(filters: ComptabiliteEleveFilterState): void {
    this.eleveFilters.set(filters);
    this.eleveFilters$.next(filters);
  }

  openDetail(f: FactureListDto): void {
    this.loadingDetail.set(true);
    this.api.getFacture(f.id).subscribe({
      next: (detail) => {
        this.selectedFacture.set(detail);
        this.loadingDetail.set(false);
      },
      error: () => this.loadingDetail.set(false),
    });
  }

  closeDetail(): void {
    this.selectedFacture.set(null);
  }
}
