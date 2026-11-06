import { ChangeDetectionStrategy, Component, input } from '@angular/core';

export type AccountStatusType = 'up_to_date' | 'debt';

@Component({
  selector: 'app-account-status-badge',
  standalone: true,
  template: `<span class="badge" [class]="status()">{{ label() }}</span>`,
  styles: `
    .badge {
      display: inline-flex;
      padding: 0.25rem 0.75rem;
      border-radius: 999px;
      font-size: 0.75rem;
      font-weight: 600;
    }
    .up_to_date {
      background: #dcfce7;
      color: #15803d;
      border: 1px solid #bbf7d0;
    }
    .debt {
      background: var(--color-danger-bg);
      color: var(--color-danger);
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccountStatusBadgeComponent {
  readonly status = input.required<AccountStatusType>();

  label(): string {
    return this.status() === 'up_to_date' ? 'À jour' : 'Dette';
  }
}
