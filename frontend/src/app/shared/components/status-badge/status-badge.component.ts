import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { TransactionStatus } from '../../../core/models/transaction.model';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  template: `
    <span class="badge" [class]="status()">{{ label() }}</span>
  `,
  styles: `
    .badge {
      display: inline-flex;
      align-items: center;
      padding: 0.25rem 0.75rem;
      border-radius: 999px;
      font-size: 0.75rem;
      font-weight: 600;
      letter-spacing: 0.02em;
    }
    .paid {
      background: var(--color-success-bg);
      color: var(--color-success);
    }
    .pending {
      background: var(--color-warning-bg);
      color: var(--color-warning);
    }
    .overdue {
      background: var(--color-danger-bg);
      color: var(--color-danger);
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StatusBadgeComponent {
  readonly status = input.required<TransactionStatus>();

  label(): string {
    const labels: Record<TransactionStatus, string> = {
      paid: 'Payé',
      pending: 'En attente',
      overdue: 'En retard',
    };
    return labels[this.status()];
  }
}
