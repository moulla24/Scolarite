import { DatePipe, KeyValuePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FraisScolaireDto, FraisScolaireRequest, NiveauDto } from '../../../../core/api/api-dto';
import { ReferentielApiService } from '../../../../core/api/referentiel-api.service';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { CurrencyMruPipe } from '../../../../shared/pipes/currency-mru.pipe';
import { ComptabiliteApiService } from '../../services/comptabilite-api.service';

@Component({
  selector: 'app-frais-page',
  standalone: true,
  imports: [KeyValuePipe, FormsModule, PageHeaderComponent, CurrencyMruPipe],
  templateUrl: './frais-page.component.html',
  styleUrl: './frais-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FraisPageComponent implements OnInit {
  private readonly api = inject(ComptabiliteApiService);
  private readonly referentiel = inject(ReferentielApiService);

  readonly frais = signal<readonly FraisScolaireDto[]>([]);
  readonly types = signal<Record<string, string>>({});
  readonly niveaux = signal<readonly NiveauDto[]>([]);
  readonly showForm = signal(false);
  readonly saving = signal(false);

  form: FraisScolaireRequest = {
    typeFrais: 'INSCRIPTION',
    libelle: '',
    montant: 0,
    niveauId: null,
    jourEcheance: 10,
    actif: true,
  };

  ngOnInit(): void {
    this.api.getFraisTypes().subscribe((t) => this.types.set(t));
    this.referentiel.getNiveaux().subscribe((n) => this.niveaux.set(n));
    this.load();
  }

  load(): void {
    this.api.listFrais().subscribe((list) => this.frais.set(list));
  }

  openForm(): void {
    this.showForm.set(true);
  }

  cancelForm(): void {
    this.showForm.set(false);
  }

  submit(): void {
    if (!this.form.libelle.trim() || this.form.montant <= 0) return;
    this.saving.set(true);
    this.api.createFrais(this.form).subscribe({
      next: () => {
        this.saving.set(false);
        this.showForm.set(false);
        this.form = { typeFrais: 'INSCRIPTION', libelle: '', montant: 0, niveauId: null, jourEcheance: 10, actif: true };
        this.load();
      },
      error: () => this.saving.set(false),
    });
  }
}
