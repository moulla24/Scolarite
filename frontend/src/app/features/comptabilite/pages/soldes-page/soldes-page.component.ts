import { DatePipe, KeyValuePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  RemiseEleveDto,
  RemiseEleveRequest,
  SoldeEleveDto,
} from '../../../../core/api/api-dto';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { BalanceAmountComponent } from '../../../../shared/components/balance-amount/balance-amount.component';
import { CurrencyMruPipe } from '../../../../shared/pipes/currency-mru.pipe';
import { ComptabiliteApiService } from '../../services/comptabilite-api.service';
import { ComptabiliteEleveFiltersComponent } from '../../components/comptabilite-eleve-filters/comptabilite-eleve-filters.component';
import {
  ComptabiliteEleveFilterState,
  emptyEleveFilters,
} from '../../components/comptabilite-eleve-filters/comptabilite-eleve-filters.model';

@Component({
  selector: 'app-soldes-page',
  standalone: true,
  imports: [
    DatePipe,
    KeyValuePipe,
    FormsModule,
    PageHeaderComponent,
    BalanceAmountComponent,
    CurrencyMruPipe,
    ComptabiliteEleveFiltersComponent,
  ],
  templateUrl: './soldes-page.component.html',
  styleUrl: './soldes-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SoldesPageComponent implements OnInit {
  private readonly api = inject(ComptabiliteApiService);

  readonly remiseTypes = signal<Record<string, string>>({});
  readonly remises = signal<readonly RemiseEleveDto[]>([]);
  readonly solde = signal<SoldeEleveDto | null>(null);
  readonly listFilters = signal<ComptabiliteEleveFilterState>(emptyEleveFilters());
  readonly formFilters = signal<ComptabiliteEleveFilterState>(emptyEleveFilters());
  readonly loading = signal(false);
  readonly showRemiseForm = signal(false);
  readonly savingRemise = signal(false);
  readonly remiseError = signal<string | null>(null);

  remiseForm: RemiseEleveRequest = {
    eleveId: 0,
    typeRemise: 'FAMILIALE',
    libelle: '',
    pourcentage: 25,
    actif: true,
  };

  ngOnInit(): void {
    this.api.getRemiseTypes().subscribe((t) => this.remiseTypes.set(t));
    this.api.listRemises().subscribe((r) => this.remises.set(r));
  }

  onListFiltersChange(filters: ComptabiliteEleveFilterState): void {
    this.listFilters.set(filters);
    if (!filters.eleveId) {
      this.solde.set(null);
      return;
    }
    this.loadSolde(filters.eleveId);
  }

  onFormFiltersChange(filters: ComptabiliteEleveFilterState): void {
    this.formFilters.set(filters);
    this.remiseForm = {
      ...this.remiseForm,
      eleveId: filters.eleveId ? Number(filters.eleveId) : 0,
    };
  }

  openRemiseForm(): void {
    this.remiseError.set(null);
    const list = this.listFilters();
    this.formFilters.set({ ...list });
    this.remiseForm = {
      eleveId: list.eleveId ? Number(list.eleveId) : 0,
      typeRemise: 'FAMILIALE',
      libelle: '',
      pourcentage: 25,
      actif: true,
    };
    this.showRemiseForm.set(true);
  }

  cancelRemiseForm(): void {
    this.showRemiseForm.set(false);
    this.remiseError.set(null);
    this.formFilters.set(emptyEleveFilters());
  }

  submitRemise(): void {
    const eleveId = this.formFilters().eleveId
      ? Number(this.formFilters().eleveId)
      : this.remiseForm.eleveId;
    if (!eleveId) {
      this.remiseError.set('Sélectionnez un élève pour enregistrer la remise.');
      return;
    }
    if (!(this.remiseForm.libelle ?? '').trim()) {
      this.remiseError.set('Indiquez un libellé pour la remise.');
      return;
    }
    this.remiseForm.eleveId = eleveId;
    this.savingRemise.set(true);
    this.remiseError.set(null);
    this.api.createRemise(this.remiseForm).subscribe({
      next: () => {
        this.savingRemise.set(false);
        this.showRemiseForm.set(false);
        this.formFilters.set(emptyEleveFilters());
        this.api.listRemises().subscribe((r) => this.remises.set(r));
        const selected = this.listFilters().eleveId;
        if (selected) {
          this.loadSolde(selected);
        }
      },
      error: (err: { error?: { message?: string } }) => {
        this.savingRemise.set(false);
        this.remiseError.set(err?.error?.message ?? 'Enregistrement impossible.');
      },
    });
  }

  private loadSolde(eleveId: string): void {
    this.loading.set(true);
    this.api.getSoldeEleve(Number(eleveId)).subscribe({
      next: (s) => {
        this.solde.set(s);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
