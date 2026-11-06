import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { FeuillePresenceDto, FeuillePresenceSaveRequest } from '../../../core/api/api-dto';

@Injectable({ providedIn: 'root' })
export class AbsencesApiService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  getFeuille(classeId: string, date: string): Observable<FeuillePresenceDto> {
    return this.http.get<FeuillePresenceDto>(`${this.base}/absences/feuille`, {
      params: { classeId, date },
    });
  }

  saveFeuille(request: FeuillePresenceSaveRequest): Observable<FeuillePresenceDto> {
    return this.http.post<FeuillePresenceDto>(`${this.base}/absences/feuille`, request);
  }
}
