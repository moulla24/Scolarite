import { KeyValuePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PaiePersonnelLigneDto } from '../../../../core/api/api-dto';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { FilterCardComponent } from '../../../../shared/components/filter-card/filter-card.component';
import { CurrencyMruPipe } from '../../../../shared/pipes/currency-mru.pipe';
import { ComptabiliteApiService } from '../../services/comptabilite-api.service';

@Component({
  selector: 'app-paie-page',
  standalone: true,
  imports: [FormsModule, PageHeaderComponent, FilterCardComponent, CurrencyMruPipe],
  templateUrl: './paie-page.component.html',
  styleUrl: './paie-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PaiePageComponent implements OnInit {
  private readonly api = inject(ComptabiliteApiService);

  readonly lignes = signal<readonly PaiePersonnelLigneDto[]>([]);
  readonly mois = signal(this.currentMonth());
  readonly mode = signal('VIREMENT');
  readonly loading = signal(false);
  readonly paying = signal(false);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.api.listPaieMois(this.mois()).subscribe({
      next: (list) => {
        this.lignes.set(list);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  payer(l: PaiePersonnelLigneDto): void {
    if (l.paye) return;
    this.paying.set(true);
    this.api
      .payerPersonnel({
        personnelId: l.personnelId,
        mois: `${this.mois()}-01`,
        montant: l.salaireMensuel,
        modePaiement: this.mode(),
      })
      .subscribe({
        next: () => {
          this.paying.set(false);
          this.load();
        },
        error: () => this.paying.set(false),
      });
  }

  payerTous(): void {
    const impayes = this.lignes().filter((l) => !l.paye).length;
    if (impayes === 0) return;
    if (!confirm(`Verser ${impayes} salaire(s) pour ${this.mois()} ?`)) return;
    this.paying.set(true);
    this.api.payerTousPersonnel(this.mois(), this.mode()).subscribe({
      next: () => {
        this.paying.set(false);
        this.load();
      },
      error: () => this.paying.set(false),
    });
  }

  totalMasse(): number {
    return this.lignes().reduce((s, l) => s + l.salaireMensuel, 0);
  }

  totalPaye(): number {
    return this.lignes().filter((l) => l.paye).reduce((s, l) => s + (l.montantPaye ?? l.salaireMensuel), 0);
  }

  countImpayes(): number {
    return this.lignes().filter((l) => !l.paye).length;
  }

  private currentMonth(): string {
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
  }
}
