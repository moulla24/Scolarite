import { AsyncPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom, map, take } from 'rxjs';
import { ScheduleDay, ScheduleSlot } from '../../../../core/models/schedule.model';
import { ExcelPageActionsComponent } from '../../../../shared/components/excel-page-actions/excel-page-actions.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import {
  downloadScheduleTemplate,
  exportScheduleSlotsToExcel,
  formatImportResult,
  parseScheduleImportExcel,
  runImportBatch,
} from '../../../../shared/utils/excel.util';
import { ScheduleCellEditorComponent } from '../../components/schedule-cell-editor/schedule-cell-editor.component';
import { ScheduleCellContext } from '../../models/schedule-cell-context.model';
import { ScheduleApiService } from '../../services/schedule-api.service';
import { ScheduleFacadeService, ScheduleViewState } from '../../services/schedule-facade.service';

@Component({
  selector: 'app-schedule-page',
  standalone: true,
  imports: [
    AsyncPipe,
    FormsModule,
    PageHeaderComponent,
    ExcelPageActionsComponent,
    ScheduleCellEditorComponent,
  ],
  providers: [ScheduleFacadeService],
  templateUrl: './schedule-page.component.html',
  styleUrl: './schedule-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SchedulePageComponent {
  private readonly facade = inject(ScheduleFacadeService);
  private readonly api = inject(ScheduleApiService);

  readonly viewState$ = this.facade.viewState$;
  readonly pageState$ = this.viewState$.pipe(map((view) => ({ view })));
  readonly cellEditor = signal<ScheduleCellContext | null>(null);

  onLevelChange(id: string): void {
    this.facade.setLevel(id);
    this.cellEditor.set(null);
  }

  onClassChange(id: string): void {
    this.facade.setClass(id);
    this.cellEditor.set(null);
  }

  onWeekChange(index: number): void {
    this.facade.setWeek(Number(index));
  }

  onPrevWeek(view: { selectedWeekIndex: number }): void {
    if (view.selectedWeekIndex > 0) {
      this.facade.setWeek(view.selectedWeekIndex - 1);
    }
  }

  onNextWeek(view: { selectedWeekIndex: number; weeks: readonly unknown[] }): void {
    if (view.selectedWeekIndex < view.weeks.length - 1) {
      this.facade.setWeek(view.selectedWeekIndex + 1);
    }
  }

  onCurrentWeek(): void {
    this.facade.goToCurrentWeek();
  }

  downloadExcelTemplate(): void {
    downloadScheduleTemplate();
  }

  onExport(): void {
    this.viewState$.pipe(take(1)).subscribe((state) => exportScheduleSlotsToExcel(state.slots));
  }

  async onImportFile(file: File): Promise<void> {
    try {
      const rows = await parseScheduleImportExcel(file);
      if (rows.length === 0) {
        alert('Fichier vide ou colonnes incorrectes. Téléchargez le modèle Excel.');
        return;
      }
      const view = await firstValueFrom(this.viewState$);
      const defaultSemaineId = view.selectedWeek?.id;
      const result = await runImportBatch(rows, (row) =>
        this.api.create({
          ...row,
          semaineId: row.semaineId || defaultSemaineId!,
        })
      );
      alert(formatImportResult(result));
      this.facade.refresh();
    } catch {
      alert('Import impossible. Vérifiez le fichier Excel.');
    }
  }

  onPrint(): void {
    window.print();
  }

  onCellClick(
    view: ScheduleViewState,
    day: ScheduleDay,
    timeLabel: string,
    slot: ScheduleSlot | undefined
  ): void {
    const [startTime = '', endTime = ''] = timeLabel.split(' - ').map((s) => s.trim());
    if (!view.filtersReady) {
      return;
    }
    const classId = slot?.classId ?? view.selectedClassId;
    const classLabel =
      slot?.className ??
      view.classFilters.find((f) => f.id === classId)?.label ??
      'Classe';

    if (!view.selectedWeek) {
      alert('Aucune semaine disponible pour ce trimestre.');
      return;
    }

    this.cellEditor.set({
      semaineId: view.selectedWeek.id,
      dayIndex: day.index,
      dayLabel: day.label,
      dateLabel: day.dateLabel,
      timeLabel,
      startTime,
      endTime,
      classId,
      classLabel,
      slot: slot ?? null,
    });
  }

  onCellEditorSaved(): void {
    this.cellEditor.set(null);
    this.facade.refresh();
  }

  onCellEditorCancelled(): void {
    this.cellEditor.set(null);
  }

  getSlot(
    grid: ReadonlyMap<string, ScheduleSlot | undefined>,
    dayIndex: number,
    timeLabel: string
  ): ScheduleSlot | undefined {
    return this.facade.getSlot(grid, dayIndex, timeLabel);
  }
}
