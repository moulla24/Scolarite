import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { UserAvatarComponent } from '../../../../shared/components/user-avatar/user-avatar.component';
import { RowActionsMenuComponent } from '../../../../shared/components/row-actions-menu/row-actions-menu.component';
import { Comptable } from '../../services/comptables-api.service';

@Component({
  selector: 'app-comptables-table',
  standalone: true,
  imports: [UserAvatarComponent, RowActionsMenuComponent],
  templateUrl: './comptables-table.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ComptablesTableComponent {
  readonly comptables = input.required<readonly Comptable[]>();
  readonly edit = output<string>();
  readonly delete = output<string>();
}
