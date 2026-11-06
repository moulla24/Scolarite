import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  BulletinDetailDto,
  BulletinEleveDto,
  BulletinEleveStatutDto,
  BulletinTrimestreDto,
  EnseignementDto,
  EvaluationDto,
  EvaluationRequest,
  GrilleNotesDto,
  NoteDto,
  NoteRequest,
  NotesImportLigneDto,
  NotesImportResultDto,
  NotesSummaryLigneDto,
  SaisieNotesDto,
  SaisieSaveRequest,
  TrimestreInfoDto,
} from '../../../core/api/api-dto';

@Injectable({ providedIn: 'root' })
export class GradesApiService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  listEvaluations(classeId?: string): Observable<readonly EvaluationDto[]> {
    const params: Record<string, string> = {};
    if (classeId && classeId !== 'all') params['classeId'] = classeId;
    return this.http.get<EvaluationDto[]>(`${this.base}/notes/evaluations`, { params });
  }

  getEvaluation(id: number): Observable<EvaluationDto> {
    return this.http.get<EvaluationDto>(`${this.base}/notes/evaluations/${id}`);
  }

  createEvaluation(request: EvaluationRequest): Observable<EvaluationDto> {
    return this.http.post<EvaluationDto>(`${this.base}/notes/evaluations`, request);
  }

  updateEvaluation(id: number, request: EvaluationRequest): Observable<EvaluationDto> {
    return this.http.put<EvaluationDto>(`${this.base}/notes/evaluations/${id}`, request);
  }

  deleteEvaluation(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/notes/evaluations/${id}`);
  }

  getNotesByEvaluation(evaluationId: number): Observable<readonly NoteDto[]> {
    return this.http.get<NoteDto[]>(`${this.base}/notes/evaluations/${evaluationId}/notes`);
  }

  getNote(id: number): Observable<NoteDto> {
    return this.http.get<NoteDto>(`${this.base}/notes/notes/${id}`);
  }

  createNote(request: NoteRequest): Observable<NoteDto> {
    return this.http.post<NoteDto>(`${this.base}/notes/notes`, request);
  }

  updateNote(id: number, request: NoteRequest): Observable<NoteDto> {
    return this.http.put<NoteDto>(`${this.base}/notes/notes/${id}`, request);
  }

  deleteNote(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/notes/notes/${id}`);
  }

  listBulletins(classeId?: string, niveauId?: string): Observable<readonly BulletinEleveDto[]> {
    const params: Record<string, string> = {};
    if (classeId && classeId !== 'all') params['classeId'] = classeId;
    if (niveauId && niveauId !== 'all') params['niveauId'] = niveauId;
    return this.http.get<BulletinEleveDto[]>(`${this.base}/notes/bulletins`, { params });
  }

  getBulletinDetail(eleveId: number): Observable<BulletinDetailDto> {
    return this.http.get<BulletinDetailDto>(`${this.base}/notes/bulletins/${eleveId}`);
  }

  getSummary(classeId?: string, niveauId?: string): Observable<readonly NotesSummaryLigneDto[]> {
    const params: Record<string, string> = {};
    if (classeId && classeId !== 'all') params['classeId'] = classeId;
    if (niveauId && niveauId !== 'all') params['niveauId'] = niveauId;
    return this.http.get<NotesSummaryLigneDto[]>(`${this.base}/notes/summary`, { params });
  }

  getGrille(classeId?: string, niveauId?: string, matiereId?: string): Observable<GrilleNotesDto> {
    const params: Record<string, string> = {};
    if (classeId && classeId !== 'all') params['classeId'] = classeId;
    if (niveauId && niveauId !== 'all') params['niveauId'] = niveauId;
    if (matiereId && matiereId !== 'all') params['matiereId'] = matiereId;
    return this.http.get<GrilleNotesDto>(`${this.base}/notes/grille`, { params });
  }

  importNotes(lignes: readonly NotesImportLigneDto[]): Observable<NotesImportResultDto> {
    return this.http.post<NotesImportResultDto>(`${this.base}/notes/import`, lignes);
  }

  listEnseignements(classeId?: string, matiereId?: string): Observable<readonly EnseignementDto[]> {
    const params: Record<string, string> = {};
    if (classeId && classeId !== 'all') params['classeId'] = classeId;
    if (matiereId && matiereId !== 'all') params['matiereId'] = matiereId;
    return this.http.get<EnseignementDto[]>(`${this.base}/enseignements`, { params });
  }

  getTrimestreCourant(): Observable<TrimestreInfoDto> {
    return this.http.get<TrimestreInfoDto>(`${this.base}/notes/trimestre-courant`);
  }

  getSaisie(classeId: string, matiereId: string): Observable<SaisieNotesDto> {
    return this.http.get<SaisieNotesDto>(`${this.base}/notes/saisie`, {
      params: { classeId, matiereId },
    });
  }

  saveSaisie(request: SaisieSaveRequest): Observable<void> {
    return this.http.post<void>(`${this.base}/notes/saisie`, request);
  }

  listBulletinStatuts(classeId?: string, niveauId?: string): Observable<readonly BulletinEleveStatutDto[]> {
    const params: Record<string, string> = {};
    if (classeId && classeId !== 'all') params['classeId'] = classeId;
    if (niveauId && niveauId !== 'all') params['niveauId'] = niveauId;
    return this.http.get<BulletinEleveStatutDto[]>(`${this.base}/notes/bulletins/statuts`, { params });
  }

  getBulletinTrimestre(eleveId: number, trimestre: number): Observable<BulletinTrimestreDto> {
    return this.http.get<BulletinTrimestreDto>(
      `${this.base}/notes/bulletins/${eleveId}/trimestre/${trimestre}`
    );
  }

  downloadBulletinPdf(eleveId: number, trimestre: number): Observable<Blob> {
    return this.http.get(`${this.base}/notes/bulletins/${eleveId}/trimestre/${trimestre}/pdf`, {
      responseType: 'blob',
    });
  }

  downloadBulletinExcel(eleveId: number, trimestre: number): Observable<Blob> {
    return this.http.get(`${this.base}/notes/bulletins/${eleveId}/trimestre/${trimestre}/excel`, {
      responseType: 'blob',
    });
  }
}
