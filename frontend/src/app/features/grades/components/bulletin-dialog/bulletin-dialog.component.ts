import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { BulletinDetailDto } from '../../../../core/api/api-dto';

@Component({
  selector: 'app-bulletin-dialog',
  standalone: true,
  templateUrl: './bulletin-dialog.component.html',
  styleUrl: './bulletin-dialog.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BulletinDialogComponent {
  readonly bulletin = input<BulletinDetailDto | null>(null);
  readonly visible = input(false);

  readonly closed = output<void>();
  readonly exportExcel = output<void>();

  onClose(): void {
    this.closed.emit();
  }

  onBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('bulletin-dialog__backdrop')) {
      this.onClose();
    }
  }
}
