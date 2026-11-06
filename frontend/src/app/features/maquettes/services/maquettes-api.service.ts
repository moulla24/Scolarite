import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  MaquetteAddMatiereRequest,
  MaquetteDto,
  MaquetteGrilleDto,
  MaquetteMatiereDetailDto,
  MaquetteMatiereSaveRequest,
} from '../../../core/api/api-dto';
import { mapMaquette, toLevelFilters, toSubjectFilters } from '../../../core/api/api-mappers';
import { ReferentielApiService } from '../../../core/api/referentiel-api.service';
import { FilterOption } from '../../../core/models/filter-option.model';
import { MaquettesListQuery } from '../../../core/models/list-query.model';
import { PedagogicalTemplate } from '../../../core/models/pedagogical-template.model';
import { MaquettesDataProvider } from '../../../core/services/interfaces/maquettes-data-provider.interface';
import { MaquetteCreatePayload } from '../models/maquette-create.model';

@Injectable({ providedIn: 'root' })
export class MaquettesApiService implements MaquettesDataProvider {
  private readonly http = inject(HttpClient);
  private readonly referentiel = inject(ReferentielApiService);
  private readonly base = environment.apiUrl;

  getTemplates(query?: MaquettesListQuery): Observable<readonly PedagogicalTemplate[]> {
    const params: Record<string, string> = {};
    if (query?.search?.trim()) params['search'] = query.search.trim();
    if (query?.matiereId && query.matiereId !== 'all') params['matiereId'] = query.matiereId;
    if (query?.niveauId && query.niveauId !== 'all') params['niveauId'] = query.niveauId;
    return this.http.get<MaquetteDto[]>(`${this.base}/maquettes`, { params }).pipe(
      map((maquettes) => maquettes.map((dto) => mapMaquette(dto)))
    );
  }

  getSubjectFilters(): Observable<readonly FilterOption[]> {
    return this.referentiel.getMatieres().pipe(map((m) => toSubjectFilters(m)));
  }

  getLevelFilters(): Observable<readonly FilterOption[]> {
    return this.referentiel.getNiveaux().pipe(map((n) => toLevelFilters(n)));
  }

  getById(id: string): Observable<PedagogicalTemplate> {
    return this.http
      .get<MaquetteDto>(`${this.base}/maquettes/${id}`)
      .pipe(map((dto) => mapMaquette(dto)));
  }

  create(payload: MaquetteCreatePayload): Observable<PedagogicalTemplate> {
    return this.http
      .post<MaquetteDto>(`${this.base}/maquettes`, {
        titre: payload.titre,
        matiereId: payload.matiereId,
        niveauId: payload.niveauId,
        description: payload.description,
        format: payload.format,
        fichierUrl: payload.fichierUrl,
        nbPages: payload.nbPages,
        dateMaj: payload.dateMaj || null,
      })
      .pipe(map((dto) => mapMaquette(dto)));
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/maquettes/${id}`);
  }

  uploadFile(file: File): Observable<{ url: string }> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<{ url: string }>(`${this.base}/maquettes/upload`, formData);
  }

  getGrille(classeId: string): Observable<MaquetteGrilleDto> {
    return this.http.get<MaquetteGrilleDto>(`${this.base}/maquettes/grille`, {
      params: { classeId },
    });
  }

  getMatiereDetail(id: number): Observable<MaquetteMatiereDetailDto> {
    return this.http.get<MaquetteMatiereDetailDto>(`${this.base}/maquettes/grille/matieres/${id}`);
  }

  saveMatiere(id: number, request: MaquetteMatiereSaveRequest): Observable<MaquetteMatiereDetailDto> {
    return this.http.put<MaquetteMatiereDetailDto>(
      `${this.base}/maquettes/grille/matieres/${id}`,
      request
    );
  }

  addMatiere(classeId: string, request: MaquetteAddMatiereRequest): Observable<MaquetteGrilleDto> {
    return this.http.post<MaquetteGrilleDto>(`${this.base}/maquettes/grille/matieres`, request, {
      params: { classeId },
    });
  }

  removeMatiere(id: number): Observable<MaquetteGrilleDto> {
    return this.http.delete<MaquetteGrilleDto>(`${this.base}/maquettes/grille/matieres/${id}`);
  }

  downloadGrilleTemplate(): Observable<Blob> {
    return this.http.get(`${this.base}/maquettes/grille/template`, {
      responseType: 'blob',
    });
  }

  exportGrille(classeId: string): Observable<Blob> {
    return this.http.get(`${this.base}/maquettes/grille/export`, {
      params: { classeId },
      responseType: 'blob',
    });
  }

  importGrille(classeId: string, file: File): Observable<MaquetteGrilleDto> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<MaquetteGrilleDto>(`${this.base}/maquettes/grille/import`, formData, {
      params: { classeId },
    });
  }
}
