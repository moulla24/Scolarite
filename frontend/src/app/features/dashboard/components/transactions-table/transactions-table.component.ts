import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { Transaction } from '../../../../core/models/transaction.model';
import { StatusBadgeComponent } from '../../../../shared/components/status-badge/status-badge.component';
import { CurrencyMruPipe } from '../../../../shared/pipes/currency-mru.pipe';

@Component({
  selector: 'app-transactions-table',
  standalone: true,
  imports: [StatusBadgeComponent, CurrencyMruPipe],
  templateUrl: './transactions-table.component.html',
  styleUrl: './transactions-table.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TransactionsTableComponent {
  readonly transactions = input.required<readonly Transaction[]>();
  readonly loading = input(false);
}
