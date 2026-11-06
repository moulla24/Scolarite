import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { TeacherStatus } from '../../../core/models/teacher.model';

@Component({
  selector: 'app-teacher-status-badge',
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
    .active {
      background: var(--color-success-bg);
      color: var(--color-success);
    }
    .on_leave {
      background: var(--color-warning-bg);
      color: var(--color-warning);
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TeacherStatusBadgeComponent {
  readonly status = input.required<TeacherStatus>();

  label(): string {
    return this.status() === 'active' ? 'Actif' : 'En congé';
  }
}
