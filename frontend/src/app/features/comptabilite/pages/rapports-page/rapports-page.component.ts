import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RapportFinancierDto } from '../../../../core/api/api-dto';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { CurrencyMruPipe } from '../../../../shared/pipes/currency-mru.pipe';
import { ComptabiliteApiService } from '../../services/comptabilite-api.service';

@Component({
  selector: 'app-rapports-page',
  standalone: true,
  imports: [DatePipe, FormsModule, PageHeaderComponent, CurrencyMruPipe],
  templateUrl: './rapports-page.component.html',
  styleUrl: './rapports-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RapportsPageComponent {
  private readonly api = inject(ComptabiliteApiService);

  readonly type = signal<'JOURNALIER' | 'MENSUEL' | 'ANNUEL'>('MENSUEL');
  readonly rapport = signal<RapportFinancierDto | null>(null);
  readonly loading = signal(false);

  load(): void {
    this.loading.set(true);
    this.api.getRapport(this.type()).subscribe({
      next: (r) => {
        this.rapport.set(r);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
