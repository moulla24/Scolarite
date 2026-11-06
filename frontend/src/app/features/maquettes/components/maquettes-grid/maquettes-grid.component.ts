import { UpperCasePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { PedagogicalTemplate } from '../../../../core/models/pedagogical-template.model';
import { ClassChipComponent } from '../../../../shared/components/class-chip/class-chip.component';

@Component({
  selector: 'app-maquettes-grid',
  standalone: true,
  imports: [ClassChipComponent, UpperCasePipe],
  templateUrl: './maquettes-grid.component.html',
  styleUrl: './maquettes-grid.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MaquettesGridComponent {
  readonly templates = input.required<readonly PedagogicalTemplate[]>();

  readonly download = output<string>();
  readonly preview = output<string>();
  readonly deleteItem = output<string>();

  formatIcon(format: string): string {
    const icons: Record<string, string> = { pdf: 'picture_as_pdf', docx: 'description', pptx: 'slideshow' };
    return icons[format] ?? 'insert_drive_file';
  }

  levelLabel(level: string): string {
    return level.toUpperCase();
  }
}
