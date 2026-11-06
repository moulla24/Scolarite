import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SchoolLevel } from '../../../../core/models/school-level.model';

@Component({
  selector: 'app-level-filter',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './level-filter.component.html',
  styleUrl: './level-filter.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LevelFilterComponent {
  readonly levels = input.required<readonly SchoolLevel[]>();
  readonly selectedLevelId = input<string>('all');

  readonly levelChange = output<string>();

  onLevelChange(levelId: string): void {
    this.levelChange.emit(levelId);
  }
}
