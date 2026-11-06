import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'currencyMru', standalone: true })
export class CurrencyMruPipe implements PipeTransform {
  transform(value: number): string {
    return `${value.toLocaleString('fr-FR')} MRU`;
  }
}
