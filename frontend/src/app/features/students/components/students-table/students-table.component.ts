import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { Student } from '../../../../core/models/student.model';
import { AccountStatusBadgeComponent } from '../../../../shared/components/account-status-badge/account-status-badge.component';
import { BalanceAmountComponent } from '../../../../shared/components/balance-amount/balance-amount.component';
import { ClassChipComponent } from '../../../../shared/components/class-chip/class-chip.component';
import { RowActionsMenuComponent } from '../../../../shared/components/row-actions-menu/row-actions-menu.component';
import { UserAvatarComponent } from '../../../../shared/components/user-avatar/user-avatar.component';

@Component({
  selector: 'app-students-table',
  standalone: true,
  imports: [
    UserAvatarComponent,
    ClassChipComponent,
    BalanceAmountComponent,
    AccountStatusBadgeComponent,
    RowActionsMenuComponent,
  ],
  templateUrl: './students-table.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StudentsTableComponent {
  readonly students = input.required<readonly Student[]>();

  readonly edit = output<string>();
  readonly delete = output<string>();
}
