import {

  ChangeDetectionStrategy,

  Component,

  inject,

  OnInit,

  signal,

} from '@angular/core';

import { FormsModule } from '@angular/forms';

import { ExcelPageActionsComponent } from '../../../../shared/components/excel-page-actions/excel-page-actions.component';

import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';

import {

  AnneeScolaireDto,

  EcoleDto,

  NiveauDto,

} from '../../../../core/api/api-dto';

import {

  downloadSettingsAnneesTemplate,

  downloadSettingsNiveauxTemplate,

  exportSettingsToExcel,

  formatImportResult,

  parseAnneesImportExcel,

  parseNiveauxImportExcel,

  runImportBatch,

} from '../../../../shared/utils/excel.util';

import { SettingsApiService } from '../../services/settings-api.service';

import { StudentsApiService } from '../../../students/services/students-api.service';



type SettingsTab = 'niveaux' | 'annees' | 'ecoles';



@Component({

  selector: 'app-settings-page',

  standalone: true,

  imports: [FormsModule, PageHeaderComponent, ExcelPageActionsComponent],

  templateUrl: './settings-page.component.html',

  styleUrl: './settings-page.component.scss',

  changeDetection: ChangeDetectionStrategy.OnPush,

})

export class SettingsPageComponent implements OnInit {

  private readonly api = inject(SettingsApiService);

  private readonly ecoleDefault = inject(StudentsApiService).defaultEcoleId;



  readonly activeTab = signal<SettingsTab>('niveaux');

  readonly niveaux = signal<readonly NiveauDto[]>([]);

  readonly annees = signal<readonly AnneeScolaireDto[]>([]);

  readonly ecoles = signal<readonly EcoleDto[]>([]);

  readonly saving = signal(false);

  readonly errorMessage = signal<string | null>(null);

  readonly successMessage = signal<string | null>(null);



  niveauNom = '';

  anneeNom = '';

  anneeDebut = '';

  anneeFin = '';

  anneeActive = true;

  ecoleNom = '';

  ecoleCode = '';

  ecoleAdresse = '';

  ecoleTel = '';

  ecoleEmail = '';



  ngOnInit(): void {

    this.reloadAll();

  }



  setTab(tab: SettingsTab): void {

    this.activeTab.set(tab);

    this.errorMessage.set(null);

    this.successMessage.set(null);

  }



  reloadAll(): void {

    this.api.listNiveaux().subscribe({ next: (n) => this.niveaux.set(n) });

    this.api.listAnnees().subscribe({ next: (a) => this.annees.set(a) });

    this.api.listEcoles().subscribe({ next: (e) => this.ecoles.set(e) });

  }



  downloadExcelTemplate(): void {

    if (this.activeTab() === 'niveaux') {

      downloadSettingsNiveauxTemplate(this.ecoleDefault);

    } else if (this.activeTab() === 'annees') {

      downloadSettingsAnneesTemplate(this.ecoleDefault);

    } else {

      alert('Import Excel disponible pour les onglets Niveaux et Années scolaires.');

    }

  }



  onExport(): void {

    exportSettingsToExcel(this.niveaux(), this.annees(), this.ecoles());

  }



  async onImportFile(file: File): Promise<void> {

    const tab = this.activeTab();

    try {

      if (tab === 'niveaux') {

        const rows = await parseNiveauxImportExcel(file);

        if (rows.length === 0) {

          alert('Fichier vide ou colonnes incorrectes.');

          return;

        }

        const result = await runImportBatch(rows, (row) =>

          this.api.createNiveau({ ecoleId: row.ecoleId, nom: row.nom })

        );

        alert(formatImportResult(result));

        this.api.listNiveaux().subscribe({ next: (n) => this.niveaux.set(n) });

      } else if (tab === 'annees') {

        const rows = await parseAnneesImportExcel(file);

        if (rows.length === 0) {

          alert('Fichier vide ou colonnes incorrectes.');

          return;

        }

        const result = await runImportBatch(rows, (row) =>

          this.api.createAnnee({

            ecoleId: row.ecoleId,

            nom: row.nom,

            dateDebut: row.dateDebut,

            dateFin: row.dateFin,

            active: row.active,

          })

        );

        alert(formatImportResult(result));

        this.api.listAnnees().subscribe({ next: (a) => this.annees.set(a) });

      } else {

        alert('Import Excel disponible pour les onglets Niveaux et Années scolaires.');

      }

    } catch {

      alert('Import impossible. Vérifiez le fichier Excel.');

    }

  }



  onCreateNiveau(): void {

    if (!this.niveauNom.trim()) {

      this.errorMessage.set('Le nom du niveau est obligatoire.');

      return;

    }

    this.saving.set(true);

    this.api

      .createNiveau({ ecoleId: this.ecoleDefault, nom: this.niveauNom.trim() })

      .subscribe({

        next: () => {

          this.saving.set(false);

          this.niveauNom = '';

          this.successMessage.set('Niveau créé.');

          this.api.listNiveaux().subscribe({ next: (n) => this.niveaux.set(n) });

        },

        error: () => {

          this.saving.set(false);

          this.errorMessage.set('Création du niveau impossible.');

        },

      });

  }



  onCreateAnnee(): void {

    if (!this.anneeNom.trim() || !this.anneeDebut || !this.anneeFin) {

      this.errorMessage.set('Renseignez le nom et les dates de l’année scolaire.');

      return;

    }

    this.saving.set(true);

    this.api

      .createAnnee({

        ecoleId: this.ecoleDefault,

        nom: this.anneeNom.trim(),

        dateDebut: this.anneeDebut,

        dateFin: this.anneeFin,

        active: this.anneeActive,

      })

      .subscribe({

        next: () => {

          this.saving.set(false);

          this.anneeNom = '';

          this.successMessage.set('Année scolaire créée.');

          this.api.listAnnees().subscribe({ next: (a) => this.annees.set(a) });

        },

        error: () => {

          this.saving.set(false);

          this.errorMessage.set('Création de l’année impossible.');

        },

      });

  }



  onCreateEcole(): void {

    if (!this.ecoleNom.trim() || !this.ecoleCode.trim()) {

      this.errorMessage.set('Nom et code de l’école sont obligatoires.');

      return;

    }

    this.saving.set(true);

    this.api

      .createEcole({

        nom: this.ecoleNom.trim(),

        code: this.ecoleCode.trim(),

        adresse: this.ecoleAdresse.trim() || undefined,

        telephone: this.ecoleTel.trim() || undefined,

        email: this.ecoleEmail.trim() || undefined,

      })

      .subscribe({

        next: () => {

          this.saving.set(false);

          this.ecoleNom = '';

          this.ecoleCode = '';

          this.successMessage.set('École créée.');

          this.api.listEcoles().subscribe({ next: (e) => this.ecoles.set(e) });

        },

        error: () => {

          this.saving.set(false);

          this.errorMessage.set('Création de l’école impossible.');

        },

      });

  }

}


