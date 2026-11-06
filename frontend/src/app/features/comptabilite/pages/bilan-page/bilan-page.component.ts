import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BilanDto, EcritureComptableDto, EcritureComptableRequest } from '../../../../core/api/api-dto';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { CurrencyMruPipe } from '../../../../shared/pipes/currency-mru.pipe';
import { ComptabiliteApiService } from '../../services/comptabilite-api.service';

@Component({
  selector: 'app-bilan-page',
  standalone: true,
  imports: [DatePipe, FormsModule, PageHeaderComponent, CurrencyMruPipe],
  templateUrl: './bilan-page.component.html',
  styleUrl: './bilan-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BilanPageComponent implements OnInit {
  private readonly api = inject(ComptabiliteApiService);

  readonly bilan = signal<BilanDto | null>(null);
  readonly ajustements = signal<readonly EcritureComptableDto[]>([]);
  readonly loading = signal(true);
  readonly from = signal(this.monthStart());
  readonly to = signal(this.today());
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
    this.loading.set(true);
    const from = this.from();
    const to = this.to();
    this.api.getBilan(from || undefined, to || undefined).subscribe({
      next: (b) => {
        this.bilan.set(b);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
    this.api.listJournal(from, to, 'MANUEL').subscribe({
      next: (list) => this.ajustements.set(list),
    });
  }

  openCreate(): void {
    this.editId.set(null);
    this.form = { ...this.emptyForm(), dateEcriture: this.to() };
    this.showForm.set(true);
  }

  openEdit(e: EcritureComptableDto): void {
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

  deleteAjustement(e: EcritureComptableDto): void {
    if (!confirm('Supprimer cet ajustement ?')) return;
    this.api.deleteEcriture(e.id).subscribe(() => this.load());
  }

  resetPeriod(): void {
    this.from.set(this.monthStart());
    this.to.set(this.today());
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
