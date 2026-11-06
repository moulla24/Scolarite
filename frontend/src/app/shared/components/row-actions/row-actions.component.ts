import { ChangeDetectionStrategy, Component, output } from '@angular/core';

@Component({
  selector: 'app-row-actions',
  standalone: true,
  template: `
    <button type="button" class="row-actions" (click)="actionClick.emit()" aria-label="Actions">
      <span class="material-symbols-outlined">more_vert</span>
    </button>
  `,
  styles: `
    .row-actions {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 2rem;
      height: 2rem;
      border: none;
      border-radius: var(--radius-sm);
      background: transparent;
      color: var(--text-muted);
      cursor: pointer;
      transition: background 0.15s ease, color 0.15s ease;

      &:hover {
        background: var(--surface-hover);
        color: var(--text-primary);
      }
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RowActionsComponent {
  readonly actionClick = output<void>();
}
