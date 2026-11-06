import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-search-field',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './search-field.component.html',
  styleUrl: './search-field.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SearchFieldComponent {
  readonly placeholder = input('Rechercher…');
  readonly value = input('');

  readonly valueChange = output<string>();

  onInput(value: string): void {
    this.valueChange.emit(value);
  }
}
