import {
  ChangeDetectionStrategy,
  Component,
  effect,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import {
  MaquetteChapitreRequest,
  MaquetteMatiereDetailDto,
} from '../../../../core/api/api-dto';
import { MaquettesApiService } from '../../services/maquettes-api.service';

interface EditableChapitre {
  ordre: number;
  titre: string;
  description: string;
}

@Component({
  selector: 'app-maquette-matiere-detail',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './maquette-matiere-detail.component.html',
  styleUrl: './maquette-matiere-detail.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MaquetteMatiereDetailComponent {
  private readonly api = inject(MaquettesApiService);

  readonly maquetteMatiereId = input.required<number>();

  readonly saved = output<void>();
  readonly closed = output<void>();

  readonly detail = signal<MaquetteMatiereDetailDto | null>(null);
  readonly volumeHoraire = signal('3');
  readonly chapitres = signal<readonly EditableChapitre[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);

  constructor() {
    effect(() => {
      const id = this.maquetteMatiereId();
      if (id) this.load(id);
    });
  }

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

  async onSave(): Promise<void> {
    const id = this.maquetteMatiereId();
    const vol = Number(this.volumeHoraire().replace(',', '.'));
    if (!Number.isFinite(vol) || vol <= 0) return;

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
        this.api.saveMatiere(id, { volumeHoraire: vol, chapitres: payload })
      );
      this.saving.set(false);
      this.saved.emit();
      await this.load(id);
    } catch {
      this.saving.set(false);
      alert('Enregistrement impossible.');
    }
  }

  private async load(id: number): Promise<void> {
    this.loading.set(true);
    try {
      const data = await firstValueFrom(this.api.getMatiereDetail(id));
      this.detail.set(data);
      this.volumeHoraire.set(String(data.volumeHoraire));
      this.chapitres.set(
        data.chapitres.map((ch) => ({
          ordre: ch.ordre,
          titre: ch.titre,
          description: ch.description ?? '',
        }))
      );
      this.loading.set(false);
    } catch {
      this.loading.set(false);
      this.detail.set(null);
    }
  }
}
