import { AsyncPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { BehaviorSubject, combineLatest, map, startWith, switchMap } from 'rxjs';
import { FactureListDto } from '../../../../core/api/api-dto';
import { AuthService } from '../../../../core/auth/auth.service';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { FilterCardComponent } from '../../../../shared/components/filter-card/filter-card.component';
import { CurrencyMruPipe } from '../../../../shared/pipes/currency-mru.pipe';
import { ComptabiliteApiService } from '../../services/comptabilite-api.service';
import { ComptabiliteRefreshService } from '../../services/comptabilite-refresh.service';
import { ComptabiliteEleveFiltersComponent } from '../../components/comptabilite-eleve-filters/comptabilite-eleve-filters.component';
import {
  ComptabiliteEleveFilterState,
  emptyEleveFilters,
} from '../../components/comptabilite-eleve-filters/comptabilite-eleve-filters.model';

@Component({
  selector: 'app-paiements-page',
  standalone: true,
  imports: [
    AsyncPipe,
    DatePipe,
    FormsModule,
    PageHeaderComponent,
    FilterCardComponent,
    CurrencyMruPipe,
    ComptabiliteEleveFiltersComponent,
  ],
  templateUrl: './paiements-page.component.html',
  styleUrl: './paiements-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PaiementsPageComponent implements OnInit {
  private readonly api = inject(ComptabiliteApiService);
  private readonly refreshBus = inject(ComptabiliteRefreshService);
  private readonly auth = inject(AuthService);
  private readonly mode$ = new BehaviorSubject('all');
  private readonly listFilters$ = new BehaviorSubject<ComptabiliteEleveFilterState>(emptyEleveFilters());
  private readonly refresh$ = new BehaviorSubject(0);

  readonly currentUser = toSignal(this.auth.currentUser$, { initialValue: null });
  readonly showForm = signal(false);
  readonly saving = signal(false);
  readonly formError = signal<string | null>(null);
  readonly facturesOuvertes = signal<readonly FactureListDto[]>([]);
  readonly selectedFacture = signal<FactureListDto | null>(null);
  readonly listFilters = signal<ComptabiliteEleveFilterState>(emptyEleveFilters());
  readonly formFilters = signal<ComptabiliteEleveFilterState>(emptyEleveFilters());

  readonly paymentForm = signal({
    datePaiement: new Date().toISOString().slice(0, 10),
    factureId: '',
    montant: '',
    modePaiement: 'ESPECES',
    reference: '',
  });

  readonly paiements$ = combineLatest([
    this.mode$,
    this.listFilters$,
    this.refresh$,
    this.refreshBus.changes$.pipe(startWith(0)),
  ]).pipe(
    switchMap(([mode, filters, _refresh, _bus]) =>
      this.api.listPaiements(
        undefined,
        undefined,
        mode === 'all' ? undefined : mode,
        filters.niveauId ? Number(filters.niveauId) : undefined,
        filters.classeId ? Number(filters.classeId) : undefined,
        filters.eleveId ? Number(filters.eleveId) : undefined,
        filters.eleveSearch || undefined
      )
    )
  );

  readonly viewState$ = combineLatest([this.paiements$, this.mode$, this.listFilters$]).pipe(
    map(([paiements, mode, listFilters]) => ({ paiements, mode, listFilters, count: paiements.length }))
  );

  ngOnInit(): void {
    this.loadFacturesOuvertes();
  }

  agentNom(): string {
    const u = this.currentUser();
    if (!u) return '—';
    return `${u.prenom} ${u.nom}`.trim();
  }

  onModeChange(mode: string): void {
    this.mode$.next(mode);
  }

  onListFiltersChange(filters: ComptabiliteEleveFilterState): void {
    this.listFilters.set(filters);
    this.listFilters$.next(filters);
  }

  onFormFiltersChange(filters: ComptabiliteEleveFilterState): void {
    this.formFilters.set(filters);
    this.selectedFacture.set(null);
    this.paymentForm.update((f) => ({ ...f, factureId: '', montant: '' }));
    this.loadFacturesOuvertes(filters);
  }

  openForm(): void {
    this.formError.set(null);
    this.resetForm();
    this.loadFacturesOuvertes();
    this.showForm.set(true);
  }

  cancelForm(): void {
    this.showForm.set(false);
    this.formError.set(null);
    this.selectedFacture.set(null);
    this.formFilters.set(emptyEleveFilters());
  }

  onFactureChange(factureId: string): void {
    const id = Number(factureId);
    const facture = this.facturesOuvertes().find((f) => f.id === id) ?? null;
    this.selectedFacture.set(facture);
    this.paymentForm.update((f) => ({
      ...f,
      factureId,
      montant: facture ? String(facture.resteAPayer) : '',
    }));
  }

  patchForm(field: string, value: string): void {
    this.paymentForm.update((f) => ({ ...f, [field]: value }));
  }

  submitPayment(): void {
    const f = this.paymentForm();
    const factureId = Number(f.factureId);
    const montant = Number(f.montant);
    if (!factureId || !montant || montant <= 0) {
      this.formError.set('Sélectionnez une facture et un montant valide.');
      return;
    }
    this.saving.set(true);
    this.formError.set(null);
    this.api
      .enregistrerPaiement({
        factureId,
        montant,
        datePaiement: f.datePaiement || null,
        modePaiement: f.modePaiement,
        reference: f.reference.trim() || null,
      })
      .subscribe({
        next: (created) => {
          this.saving.set(false);
          this.cancelForm();
          this.refresh$.next(this.refresh$.value + 1);
          this.loadFacturesOuvertes();
          if (created?.id) {
            this.downloadRecu(created.id);
          }
        },
        error: (err: { error?: { message?: string } }) => {
          this.saving.set(false);
          this.formError.set(err?.error?.message ?? 'Enregistrement impossible.');
        },
      });
  }

  downloadRecu(paiementId: number): void {
    this.api.downloadRecuPdf(paiementId).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `recu-${paiementId}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
      },
    });
  }

  private loadFacturesOuvertes(filters?: ComptabiliteEleveFilterState): void {
    this.api
      .listFactures(
        undefined,
        undefined,
        filters?.niveauId ? Number(filters.niveauId) : undefined,
        filters?.classeId ? Number(filters.classeId) : undefined,
        filters?.eleveId ? Number(filters.eleveId) : undefined
      )
      .subscribe({
        next: (list) =>
          this.facturesOuvertes.set(
            list.filter((f) => f.statut === 'IMPAYEE' || f.statut === 'PARTIELLE')
          ),
      });
  }

  private resetForm(): void {
    this.paymentForm.set({
      datePaiement: new Date().toISOString().slice(0, 10),
      factureId: '',
      montant: '',
      modePaiement: 'ESPECES',
      reference: '',
    });
    this.formFilters.set(emptyEleveFilters());
    this.selectedFacture.set(null);
  }
}
