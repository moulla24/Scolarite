import { AsyncPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { ExcelPageActionsComponent } from '../../../../shared/components/excel-page-actions/excel-page-actions.component';
import { FilterCardComponent } from '../../../../shared/components/filter-card/filter-card.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { MaquetteAddMatierePanelComponent } from '../../components/maquette-add-matiere-panel/maquette-add-matiere-panel.component';
import { MaquetteMatiereDetailComponent } from '../../components/maquette-matiere-detail/maquette-matiere-detail.component';
import { MaquetteMatieresGridComponent } from '../../components/maquette-matieres-grid/maquette-matieres-grid.component';
import { MaquetteMatiereCardDto } from '../../../../core/api/api-dto';
import { MaquettesApiService } from '../../services/maquettes-api.service';
import { MaquettesFacadeService } from '../../services/maquettes-facade.service';

@Component({
  selector: 'app-maquettes-page',
  standalone: true,
  imports: [
    AsyncPipe,
    FormsModule,
    PageHeaderComponent,
    FilterCardComponent,
    ExcelPageActionsComponent,
    MaquetteAddMatierePanelComponent,
    MaquetteMatieresGridComponent,
    MaquetteMatiereDetailComponent,
  ],
  providers: [MaquettesFacadeService],
  templateUrl: './maquettes-page.component.html',
  styleUrl: './maquettes-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MaquettesPageComponent {
  private readonly facade = inject(MaquettesFacadeService);
  private readonly api = inject(MaquettesApiService);

  readonly viewState$ = this.facade.viewState$;
  readonly selectedMatiereId = signal<number | null>(null);
  readonly successMessage = signal<string | null>(null);
  readonly addError = signal<string | null>(null);
  readonly importError = signal<string | null>(null);
  readonly importing = signal(false);
  readonly showAddPanel = signal(false);

  /** Arrow function : conserve `this` quand passée à excel-page-actions */
  readonly downloadExcelTemplate = async (): Promise<void> => {
    this.importError.set(null);
    try {
      const blob = await firstValueFrom(this.api.downloadGrilleTemplate());
      const url = URL.createObjectURL(blob);
      const anchor = document.createElement('a');
      anchor.href = url;
      anchor.download = 'modele-maquette.xlsx';
      anchor.click();
      URL.revokeObjectURL(url);
    } catch (err: unknown) {
      this.importError.set(
        await this.extractApiError(err, 'Téléchargement du modèle impossible.')
      );
    }
  };

  onLevelChange(levelId: string): void {
    this.facade.setLevel(levelId);
    this.selectedMatiereId.set(null);
    this.showAddPanel.set(false);
    this.successMessage.set(null);
  }

  onClassChange(classId: string): void {
    this.facade.setClass(classId);
    this.selectedMatiereId.set(null);
    this.showAddPanel.set(false);
    this.successMessage.set(null);
  }

  openAddPanel(): void {
    this.addError.set(null);
    this.showAddPanel.set(true);
  }

  closeAddPanel(): void {
    this.showAddPanel.set(false);
    this.addError.set(null);
  }

  onSubjectChange(subjectId: string, matieres: readonly MaquetteMatiereCardDto[]): void {
    this.facade.setSubject(subjectId);
    if (subjectId === 'all') {
      this.selectedMatiereId.set(null);
      return;
    }
    const row = matieres.find((m) => String(m.matiereId) === subjectId);
    this.selectedMatiereId.set(row?.id ?? null);
  }

  onSelectMatiere(id: number, matieres: readonly MaquetteMatiereCardDto[]): void {
    this.selectedMatiereId.set(id);
    const row = matieres.find((m) => m.id === id);
    if (row) {
      this.facade.setSubject(String(row.matiereId));
    }
  }

  onCloseDetail(): void {
    this.selectedMatiereId.set(null);
  }

  onDetailSaved(): void {
    this.successMessage.set('Maquette enregistrée avec succès.');
    this.facade.refresh();
  }

  onMatiereAdded(): void {
    this.addError.set(null);
    this.showAddPanel.set(false);
    this.successMessage.set('Matière ajoutée à la maquette.');
    this.facade.refresh();
  }

  onMatiereAddFailed(message: string): void {
    this.successMessage.set(null);
    this.addError.set(message);
  }

  async onRemoveMatiere(id: number): Promise<void> {
    if (!confirm('Retirer cette matière de la maquette ?')) return;
    try {
      await firstValueFrom(this.api.removeMatiere(id));
      if (this.selectedMatiereId() === id) {
        this.selectedMatiereId.set(null);
      }
      this.successMessage.set('Matière retirée de la maquette.');
      this.facade.refresh();
    } catch {
      alert('Suppression impossible.');
    }
  }

  async onExport(classeId: string): Promise<void> {
    try {
      const blob = await firstValueFrom(this.api.exportGrille(classeId));
      const url = URL.createObjectURL(blob);
      const anchor = document.createElement('a');
      anchor.href = url;
      anchor.download = `maquette-${classeId}.xlsx`;
      anchor.click();
      URL.revokeObjectURL(url);
    } catch {
      alert('Export impossible.');
    }
  }

  async onImportFile(classeId: string, file: File): Promise<void> {
    if (!classeId || classeId === 'all') {
      this.importError.set('Sélectionnez une classe avant d’importer.');
      return;
    }
    const name = file.name.toLowerCase();
    if (!name.endsWith('.xlsx')) {
      this.importError.set('Format non supporté. Utilisez un fichier .xlsx (téléchargez le modèle).');
      return;
    }

    this.importError.set(null);
    this.successMessage.set(null);
    this.importing.set(true);
    try {
      await firstValueFrom(this.api.importGrille(classeId, file));
      this.successMessage.set('Maquette importée avec succès.');
      this.facade.refresh();
    } catch (err: unknown) {
      this.importError.set(await this.extractApiError(err, 'Import impossible. Utilisez le modèle Excel fourni.'));
    } finally {
      this.importing.set(false);
    }
  }

  private async extractApiError(err: unknown, fallback: string): Promise<string> {
    if (err instanceof HttpErrorResponse) {
      if (err.status === 0) {
        return 'Serveur inaccessible. Vérifiez que le backend tourne sur le port 8081.';
      }
      const body = err.error;
      if (body && typeof body === 'object' && 'message' in body) {
        return String((body as { message: string }).message);
      }
      if (body instanceof Blob) {
        try {
          const text = await body.text();
          const json = JSON.parse(text) as { message?: string };
          if (json.message) return json.message;
        } catch {
          /* ignore */
        }
      }
      if (typeof body === 'string' && body.trim()) return body;
    }
    return fallback;
  }
}
