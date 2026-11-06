import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DemandePaiementDto } from '../../../../core/api/api-dto';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { FilterCardComponent } from '../../../../shared/components/filter-card/filter-card.component';
import { CurrencyMruPipe } from '../../../../shared/pipes/currency-mru.pipe';
import { ComptabiliteApiService } from '../../services/comptabilite-api.service';
import { ComptabiliteRefreshService } from '../../services/comptabilite-refresh.service';

@Component({
  selector: 'app-validations-page',
  standalone: true,
  imports: [DatePipe, FormsModule, PageHeaderComponent, FilterCardComponent, CurrencyMruPipe],
  templateUrl: './validations-page.component.html',
  styleUrl: './validations-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ValidationsPageComponent implements OnInit {
  private readonly api = inject(ComptabiliteApiService);
  private readonly refreshBus = inject(ComptabiliteRefreshService);

  readonly demandes = signal<readonly DemandePaiementDto[]>([]);
  readonly statut = signal('EN_ATTENTE');
  readonly loading = signal(true);
  readonly processingId = signal<number | null>(null);
  readonly refuseId = signal<number | null>(null);
  readonly motifRefus = signal('');
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.load();
  }

  onStatutChange(statut: string): void {
    this.statut.set(statut);
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.api.listDemandesPaiement(this.statut() || undefined).subscribe({
      next: (list) => {
        this.demandes.set(list);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  valider(d: DemandePaiementDto): void {
    this.processingId.set(d.id);
    this.error.set(null);
    this.api.validerDemandePaiement(d.id).subscribe({
      next: () => {
        this.processingId.set(null);
        this.refreshBus.notify();
        this.load();
      },
      error: (err: { error?: { message?: string } }) => {
        this.processingId.set(null);
        this.error.set(err?.error?.message ?? 'Validation impossible.');
      },
    });
  }

  openRefus(d: DemandePaiementDto): void {
    this.refuseId.set(d.id);
    this.motifRefus.set('');
    this.error.set(null);
  }

  cancelRefus(): void {
    this.refuseId.set(null);
    this.motifRefus.set('');
  }

  confirmerRefus(): void {
    const id = this.refuseId();
    const motif = this.motifRefus().trim();
    if (!id || !motif) {
      this.error.set('Indiquez un motif de refus.');
      return;
    }
    this.processingId.set(id);
    this.api.refuserDemandePaiement(id, motif).subscribe({
      next: () => {
        this.processingId.set(null);
        this.refuseId.set(null);
        this.refreshBus.notify();
        this.load();
      },
      error: (err: { error?: { message?: string } }) => {
        this.processingId.set(null);
        this.error.set(err?.error?.message ?? 'Refus impossible.');
      },
    });
  }

  statutLabel(statut: string): string {
    return (
      {
        EN_COURS: 'Traitement bancaire',
        EN_ATTENTE: 'À valider',
        VALIDE: 'Validée',
        REFUSE: 'Refusée',
      }[statut] ?? statut
    );
  }
}
