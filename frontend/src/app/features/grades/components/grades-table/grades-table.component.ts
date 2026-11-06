import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { GrilleNotesDto, NotesSummaryLigneDto } from '../../../../core/api/api-dto';
import { ClassChipComponent } from '../../../../shared/components/class-chip/class-chip.component';

@Component({
  selector: 'app-grades-table',
  standalone: true,
  imports: [ClassChipComponent],
  templateUrl: './grades-table.component.html',
  styleUrl: './grades-table.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GradesTableComponent {
  readonly grille = input.required<GrilleNotesDto>();
  readonly summaryRows = input<readonly NotesSummaryLigneDto[]>([]);
  readonly summaryMode = input(false);
  readonly needsClassSelection = input(false);
  readonly loadError = input(false);

  readonly viewBulletin = output<number>();
}
