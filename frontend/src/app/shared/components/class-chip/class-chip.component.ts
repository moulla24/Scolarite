import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'app-class-chip',
  standalone: true,
  template: `<span class="chip">{{ label() }}</span>`,
  styles: `
    .chip {
      display: inline-block;
      padding: 0.25rem 0.65rem;
      border-radius: 6px;
      background: var(--color-accent-blue-bg);
      color: var(--color-accent-blue-dark);
      font-size: 0.75rem;
      font-weight: 600;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ClassChipComponent {
  readonly label = input.required<string>();
}
