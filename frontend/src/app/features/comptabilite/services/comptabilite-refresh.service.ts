import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ComptabiliteRefreshService {
  private readonly tick$ = new BehaviorSubject(0);

  readonly changes$ = this.tick$.asObservable();

  notify(): void {
    this.tick$.next(this.tick$.value + 1);
  }
}
