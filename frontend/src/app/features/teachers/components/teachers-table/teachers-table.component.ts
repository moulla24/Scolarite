import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { Teacher } from '../../../../core/models/teacher.model';
import { RowActionsMenuComponent } from '../../../../shared/components/row-actions-menu/row-actions-menu.component';
import { TeacherStatusBadgeComponent } from '../../../../shared/components/teacher-status-badge/teacher-status-badge.component';
import { UserAvatarComponent } from '../../../../shared/components/user-avatar/user-avatar.component';

@Component({
  selector: 'app-teachers-table',
  standalone: true,
  imports: [UserAvatarComponent, TeacherStatusBadgeComponent, RowActionsMenuComponent],
  templateUrl: './teachers-table.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TeachersTableComponent {
  readonly teachers = input.required<readonly Teacher[]>();

  readonly edit = output<string>();
  readonly delete = output<string>();
}
