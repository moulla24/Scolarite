import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  AnneeScolaireDto,
  AnneeScolaireRequest,
  EcoleDto,
  EcoleRequest,
  NiveauDto,
  NiveauRequest,
} from '../../../core/api/api-dto';

@Injectable({ providedIn: 'root' })
export class SettingsApiService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  listNiveaux(): Observable<readonly NiveauDto[]> {
    return this.http.get<NiveauDto[]>(`${this.base}/niveaux`);
  }

  createNiveau(request: NiveauRequest): Observable<NiveauDto> {
    return this.http.post<NiveauDto>(`${this.base}/niveaux`, request);
  }

  listAnnees(): Observable<readonly AnneeScolaireDto[]> {
    return this.http.get<AnneeScolaireDto[]>(`${this.base}/annees-scolaires`);
  }

  createAnnee(request: AnneeScolaireRequest): Observable<AnneeScolaireDto> {
    return this.http.post<AnneeScolaireDto>(`${this.base}/annees-scolaires`, request);
  }

  listEcoles(): Observable<readonly EcoleDto[]> {
    return this.http.get<EcoleDto[]>(`${this.base}/ecoles`);
  }

  createEcole(request: EcoleRequest): Observable<EcoleDto> {
    return this.http.post<EcoleDto>(`${this.base}/ecoles`, request);
  }
}
