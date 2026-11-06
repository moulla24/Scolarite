import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, shareReplay } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ClasseDto, MatiereDto, NiveauDto } from './api-dto';

@Injectable({ providedIn: 'root' })
export class ReferentielApiService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  readonly niveaux$ = this.http
    .get<NiveauDto[]>(`${this.base}/niveaux`)
    .pipe(shareReplay(1));

  readonly classes$ = this.http
    .get<ClasseDto[]>(`${this.base}/classes`)
    .pipe(shareReplay(1));

  getNiveaux(): Observable<readonly NiveauDto[]> {
    return this.niveaux$;
  }

  getClasses(niveauId?: string, search?: string): Observable<readonly ClasseDto[]> {
    const params: Record<string, string> = {};
    if (niveauId && niveauId !== 'all') params['niveauId'] = niveauId;
    if (search?.trim()) params['search'] = search.trim();
    return this.http.get<ClasseDto[]>(`${this.base}/classes`, { params });
  }

  getMatieres(): Observable<readonly MatiereDto[]> {
    return this.http.get<MatiereDto[]>(`${this.base}/matieres`);
  }
}
