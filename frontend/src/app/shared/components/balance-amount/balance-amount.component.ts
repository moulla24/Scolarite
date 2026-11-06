import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { CurrencyMruPipe } from '../../pipes/currency-mru.pipe';

@Component({
  selector: 'app-balance-amount',
  standalone: true,
  imports: [CurrencyMruPipe],
  template: `
    <span class="balance" [class.balance--zero]="amount() === 0" [class.balance--debt]="amount() > 0">
      {{ amount() | currencyMru }}
    </span>
  `,
  styles: `
    .balance {
      font-weight: 700;
      font-size: 0.875rem;
    }
    .balance--zero {
      color: var(--color-success);
    }
    .balance--debt {
      color: var(--color-danger);
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BalanceAmountComponent {
  readonly amount = input.required<number>();
}
