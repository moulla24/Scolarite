import { AsyncPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { take } from 'rxjs';
import { ExcelPageActionsComponent } from '../../../../shared/components/excel-page-actions/excel-page-actions.component';
import { exportDashboardToExcel } from '../../../../shared/utils/excel.util';
import { LevelFilterComponent } from '../../components/level-filter/level-filter.component';
import { StatCardComponent } from '../../components/stat-card/stat-card.component';
import { TransactionsTableComponent } from '../../components/transactions-table/transactions-table.component';
import { DashboardFacadeService } from '../../services/dashboard-facade.service';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [
    AsyncPipe,
    ExcelPageActionsComponent,
    StatCardComponent,
    LevelFilterComponent,
    TransactionsTableComponent,
  ],
  providers: [DashboardFacadeService],
  templateUrl: './dashboard-page.component.html',
  styleUrl: './dashboard-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardPageComponent {
  private readonly facade = inject(DashboardFacadeService);

  readonly viewState$ = this.facade.viewState$;

  onLevelChange(levelId: string): void {
    this.facade.setLevel(levelId);
  }

  onExport(): void {
    this.viewState$.pipe(take(1)).subscribe((state) => {
      const levelLabel =
        state.levels.find((l) => l.id === state.selectedLevelId)?.label ?? 'Tous les niveaux';
      exportDashboardToExcel(state.stats, state.transactions, levelLabel);
    });
  }
}
