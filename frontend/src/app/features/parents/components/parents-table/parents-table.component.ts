import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { Parent } from '../../../../core/models/parent.model';
import { BalanceAmountComponent } from '../../../../shared/components/balance-amount/balance-amount.component';
import { RowActionsMenuComponent } from '../../../../shared/components/row-actions-menu/row-actions-menu.component';
import { UserAvatarComponent } from '../../../../shared/components/user-avatar/user-avatar.component';

@Component({
  selector: 'app-parents-table',
  standalone: true,
  imports: [UserAvatarComponent, BalanceAmountComponent, RowActionsMenuComponent],
  templateUrl: './parents-table.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ParentsTableComponent {
  readonly parents = input.required<readonly Parent[]>();

  readonly edit = output<string>();
  readonly delete = output<string>();
}
