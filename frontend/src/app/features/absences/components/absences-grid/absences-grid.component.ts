import {
  ChangeDetectionStrategy,
  Component,
  input,
  output,
  signal,
} from '@angular/core';
import { FeuillePresenceDto, PresenceStatut } from '../../../../core/api/api-dto';

@Component({
  selector: 'app-absences-grid',
  standalone: true,
  templateUrl: './absences-grid.component.html',
  styleUrl: './absences-grid.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AbsencesGridComponent {
  readonly feuille = input.required<FeuillePresenceDto>();
  readonly statutChange = output<{
    plage: string;
    eleveId: number;
    statut: PresenceStatut;
  }>();

  readonly selectedPlageIndex = signal(0);

  selectPlage(index: number): void {
    this.selectedPlageIndex.set(index);
  }

  onStatutChange(plage: string, eleveId: number, statut: PresenceStatut): void {
    this.statutChange.emit({ plage, eleveId, statut });
  }
}
