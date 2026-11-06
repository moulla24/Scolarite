import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'app-facture-status-badge',
  standalone: true,
  template: `
    <span class="badge" [class]="'badge--' + status().toLowerCase()">{{ label() }}</span>
  `,
  styles: `
    .badge {
      display: inline-flex;
      align-items: center;
      padding: 0.2rem 0.55rem;
      border-radius: 999px;
      font-size: 0.75rem;
      font-weight: 600;
      letter-spacing: 0.02em;
    }
    .badge--payee {
      background: var(--color-success-bg);
      color: var(--color-success);
    }
    .badge--partielle {
      background: var(--color-warning-bg);
      color: var(--color-warning);
    }
    .badge--impayee {
      background: var(--color-danger-bg);
      color: var(--color-danger);
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FactureStatusBadgeComponent {
  readonly status = input.required<string>();

  label(): string {
    switch (this.status()?.toUpperCase()) {
      case 'PAYEE':
        return 'Payée';
      case 'PARTIELLE':
        return 'Partielle';
      case 'IMPAYEE':
        return 'Impayée';
      default:
        return this.status();
    }
  }
}
