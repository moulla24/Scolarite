import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  HostListener,
  inject,
  output,
  signal,
} from '@angular/core';

@Component({
  selector: 'app-row-actions-menu',
  standalone: true,
  template: `
    <div class="row-actions-menu">
      <button
        type="button"
        class="row-actions-menu__trigger"
        (click)="toggle($event)"
        [attr.aria-expanded]="open()"
        aria-haspopup="menu"
        aria-label="Actions"
      >
        <span class="material-symbols-outlined">more_vert</span>
      </button>
      @if (open()) {
        <div class="row-actions-menu__dropdown" role="menu">
          <button type="button" class="row-actions-menu__item" role="menuitem" (click)="onEdit()">
            <span class="material-symbols-outlined" aria-hidden="true">edit</span>
            Modifier
          </button>
          <button
            type="button"
            class="row-actions-menu__item row-actions-menu__item--danger"
            role="menuitem"
            (click)="onDelete()"
          >
            <span class="material-symbols-outlined" aria-hidden="true">delete</span>
            Supprimer
          </button>
        </div>
      }
    </div>
  `,
  styles: `
    .row-actions-menu {
      position: relative;
      display: inline-flex;
    }

    .row-actions-menu__trigger {
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

    .row-actions-menu__dropdown {
      position: absolute;
      top: 100%;
      right: 0;
      z-index: 20;
      min-width: 9rem;
      margin-top: 0.25rem;
      padding: 0.25rem;
      border: 1px solid var(--border-subtle);
      border-radius: var(--radius-md);
      background: var(--surface-card);
      box-shadow: var(--shadow-md);
    }

    .row-actions-menu__item {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      width: 100%;
      padding: 0.5rem 0.625rem;
      border: none;
      border-radius: var(--radius-sm);
      background: transparent;
      color: var(--text-primary);
      font-size: 0.875rem;
      text-align: left;
      cursor: pointer;
      transition: background 0.15s ease;

      .material-symbols-outlined {
        font-size: 1.125rem;
        color: var(--text-muted);
      }

      &:hover {
        background: var(--surface-hover);
      }

      &--danger {
        color: var(--color-danger, #c62828);

        .material-symbols-outlined {
          color: inherit;
        }
      }
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RowActionsMenuComponent {
  private readonly host = inject(ElementRef<HTMLElement>);

  readonly edit = output<void>();
  readonly delete = output<void>();

  readonly open = signal(false);

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.host.nativeElement.contains(event.target as Node)) {
      this.open.set(false);
    }
  }

  toggle(event: MouseEvent): void {
    event.stopPropagation();
    this.open.update((v) => !v);
  }

  onEdit(): void {
    this.open.set(false);
    this.edit.emit();
  }

  onDelete(): void {
    this.open.set(false);
    this.delete.emit();
  }
}
