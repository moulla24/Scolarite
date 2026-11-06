import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { EnseignementDto } from '../../../../core/api/api-dto';
import { RowActionsMenuComponent } from '../../../../shared/components/row-actions-menu/row-actions-menu.component';

@Component({
  selector: 'app-assignments-table',
  standalone: true,
  imports: [RowActionsMenuComponent],
  templateUrl: './assignments-table.component.html',
  styleUrl: './assignments-table.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AssignmentsTableComponent {
  readonly assignments = input.required<readonly EnseignementDto[]>();

  readonly edit = output<number>();
  readonly deleteItem = output<number>();
}
