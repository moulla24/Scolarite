import { ChangeDetectionStrategy, Component, ElementRef, input, output, ViewChild } from '@angular/core';
import { EXCEL_ACCEPT } from '../../utils/excel-import.util';

@Component({
  selector: 'app-excel-page-actions',
  standalone: true,
  template: `
    <input
      #importInput
      type="file"
      [accept]="accept"
      hidden
      (change)="onFileSelected($event)"
    />
    @if (showTemplate()) {
      <button type="button" class="btn btn--outline" (click)="onTemplateClick()">
        <span class="material-symbols-outlined" aria-hidden="true">download</span>
        Télécharger modèle
      </button>
    }
    <button type="button" class="btn btn--outline" (click)="exportClick.emit()">
      <span class="material-symbols-outlined" aria-hidden="true">table</span>
      Exporter Excel
    </button>
    @if (showImport()) {
      <button type="button" class="btn btn--outline" (click)="openImport()">
        <span class="material-symbols-outlined" aria-hidden="true">upload</span>
        Importer Excel
      </button>
    }
  `,
  styles: `
    :host {
      display: contents;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ExcelPageActionsComponent {
  /** Fonction appelée au clic sur « Télécharger modèle » — déclenche le .xlsx côté client. */
  readonly templateDownload = input<(() => void) | null>(null);
  readonly showTemplate = input(true);
  readonly showImport = input(true);
  readonly accept = EXCEL_ACCEPT;

  readonly templateClick = output<void>();
  readonly exportClick = output<void>();
  readonly importFile = output<File>();

  @ViewChild('importInput') importInput?: ElementRef<HTMLInputElement>;

  onTemplateClick(): void {
    const download = this.templateDownload();
    if (download) {
      download();
      return;
    }
    this.templateClick.emit();
  }

  openImport(): void {
    this.importInput?.nativeElement.click();
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';
    if (file) this.importFile.emit(file);
  }
}
