import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { MaquetteChapitreRequest } from '../../../../core/api/api-dto';
import { MaquettesApiService } from '../../services/maquettes-api.service';

interface EditableChapitre {
  ordre: number;
  titre: string;
  description: string;
}

@Component({
  selector: 'app-maquette-add-matiere-panel',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './maquette-add-matiere-panel.component.html',
  styleUrl: './maquette-add-matiere-panel.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MaquetteAddMatierePanelComponent {
  private readonly api = inject(MaquettesApiService);

  readonly classeId = input.required<string>();

  readonly added = output<void>();
  readonly failed = output<string>();
  readonly closed = output<void>();

  readonly nom = signal('');
  readonly volumeHoraire = signal('3');
  readonly chapitres = signal<readonly EditableChapitre[]>([
    { ordre: 1, titre: 'Chapitre 1', description: '' },
  ]);
  readonly saving = signal(false);

  addChapitre(): void {
    const next = this.chapitres().length + 1;
    this.chapitres.update((list) => [
      ...list,
      { ordre: next, titre: `Chapitre ${next}`, description: '' },
    ]);
  }

  removeChapitre(index: number): void {
    this.chapitres.update((list) => list.filter((_, i) => i !== index));
  }

  patchChapitre(index: number, field: keyof EditableChapitre, value: string): void {
    this.chapitres.update((list) =>
      list.map((ch, i) => (i === index ? { ...ch, [field]: value } : ch))
    );
  }

  async onSubmit(): Promise<void> {
    const nom = this.nom().trim();
    if (!nom) {
      this.failed.emit('Indiquez le nom de la matière.');
      return;
    }

    const vol = Number(this.volumeHoraire().replace(',', '.'));
    if (!Number.isFinite(vol) || vol <= 0) {
      this.failed.emit('Le volume horaire doit être un nombre positif.');
      return;
    }

    const payload: MaquetteChapitreRequest[] = this.chapitres()
      .filter((ch) => ch.titre.trim())
      .map((ch, i) => ({
        ordre: ch.ordre || i + 1,
        titre: ch.titre.trim(),
        description: ch.description.trim() || null,
      }));

    this.saving.set(true);
    try {
      await firstValueFrom(
        this.api.addMatiere(this.classeId(), { nom, volumeHoraire: vol, chapitres: payload })
      );
      this.saving.set(false);
      this.resetForm();
      this.added.emit();
    } catch {
      this.saving.set(false);
      this.failed.emit('Impossible d’ajouter cette matière.');
    }
  }

  private resetForm(): void {
    this.nom.set('');
    this.volumeHoraire.set('3');
    this.chapitres.set([{ ordre: 1, titre: 'Chapitre 1', description: '' }]);
  }
}
