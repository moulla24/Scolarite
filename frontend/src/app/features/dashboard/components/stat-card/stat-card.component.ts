import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { DashboardStat } from '../../../../core/models/dashboard-stats.model';

@Component({
  selector: 'app-stat-card',
  standalone: true,
  templateUrl: './stat-card.component.html',
  styleUrl: './stat-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StatCardComponent {
  readonly stat = input.required<DashboardStat>();
}
