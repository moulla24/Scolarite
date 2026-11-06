import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { EcritureComptableDto, EcritureComptableRequest } from '../../../../core/api/api-dto';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { FilterCardComponent } from '../../../../shared/components/filter-card/filter-card.component';
import { CurrencyMruPipe } from '../../../../shared/pipes/currency-mru.pipe';
import { ComptabiliteApiService } from '../../services/comptabilite-api.service';

@Component({
  selector: 'app-journal-page',
  standalone: true,
  imports: [DatePipe, FormsModule, PageHeaderComponent, FilterCardComponent, CurrencyMruPipe],
  templateUrl: './journal-page.component.html',
  styleUrl: './journal-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class JournalPageComponent implements OnInit {
  private readonly api = inject(ComptabiliteApiService);

  readonly ecritures = signal<readonly EcritureComptableDto[]>([]);
  readonly from = signal(this.monthStart());
  readonly to = signal(this.today());
  readonly referenceType = signal('');
  readonly compte = signal('');
  readonly search = signal('');
  readonly totalDebit = signal(0);
  readonly showForm = signal(false);
  readonly editId = signal<number | null>(null);
  readonly saving = signal(false);

  form: EcritureComptableRequest = this.emptyForm();

  readonly comptes = [
    { code: '512', label: '512 — Banque / Caisse' },
    { code: '641', label: '641 — Charges personnel' },
    { code: '706', label: '706 — Produits scolaires' },
    { code: '411', label: '411 — Clients' },
    { code: '421', label: '421 — Dettes salariales' },
  ];

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.api
      .listJournal(
        this.from() || undefined,
        this.to() || undefined,
        this.referenceType() || undefined,
        this.compte() || undefined,
        this.search() || undefined
      )
      .subscribe({
        next: (list) => {
          this.ecritures.set(list);
          this.totalDebit.set(list.reduce((s, e) => s + e.montant, 0));
        },
      });
  }

  openCreate(): void {
    this.editId.set(null);
    this.form = this.emptyForm();
    this.showForm.set(true);
  }

  openEdit(e: EcritureComptableDto): void {
    if (!e.modifiable) return;
    this.editId.set(e.id);
    this.form = {
      dateEcriture: e.dateEcriture,
      libelle: e.libelle,
      compteDebit: e.compteDebit,
      compteCredit: e.compteCredit,
      montant: e.montant,
      numeroPiece: e.numeroPiece,
    };
    this.showForm.set(true);
  }

  cancelForm(): void {
    this.showForm.set(false);
    this.editId.set(null);
  }

  save(): void {
    this.saving.set(true);
    const id = this.editId();
    const req = id ? this.api.updateEcriture(id, this.form) : this.api.createEcriture(this.form);
    req.subscribe({
      next: () => {
        this.saving.set(false);
        this.showForm.set(false);
        this.load();
      },
      error: () => this.saving.set(false),
    });
  }

  deleteEcriture(e: EcritureComptableDto): void {
    if (!e.modifiable || !confirm('Supprimer cette écriture ?')) return;
    this.api.deleteEcriture(e.id).subscribe(() => this.load());
  }

  resetFilters(): void {
    this.from.set(this.monthStart());
    this.to.set(this.today());
    this.referenceType.set('');
    this.compte.set('');
    this.search.set('');
    this.load();
  }

  private emptyForm(): EcritureComptableRequest {
    return {
      dateEcriture: this.today(),
      libelle: '',
      compteDebit: '512',
      compteCredit: '706',
      montant: 0,
      numeroPiece: '',
    };
  }

  private monthStart(): string {
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-01`;
  }

  private today(): string {
    return new Date().toISOString().slice(0, 10);
  }
}
