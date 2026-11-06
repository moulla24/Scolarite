import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { Classroom } from '../../../../core/models/classroom.model';
import { ClassChipComponent } from '../../../../shared/components/class-chip/class-chip.component';

@Component({
  selector: 'app-classrooms-grid',
  standalone: true,
  imports: [ClassChipComponent],
  templateUrl: './classrooms-grid.component.html',
  styleUrl: './classrooms-grid.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ClassroomsGridComponent {
  readonly classrooms = input.required<readonly Classroom[]>();

  readonly viewDetail = output<string>();

  fillPercent(c: Classroom): number {
    return Math.round((c.studentCount / c.capacity) * 100);
  }
}
