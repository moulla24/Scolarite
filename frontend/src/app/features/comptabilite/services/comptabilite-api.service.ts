import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  BilanDto,
  ComptabiliteStatsDto,
  DashboardFinancierDto,
  DepenseScolaireDto,
  DepenseScolaireRequest,
  EcritureComptableDto,
  EcritureComptableRequest,
  FactureDetailDto,
  FactureListDto,
  FraisScolaireDto,
  FraisScolaireRequest,
  DemandePaiementDto,
  NotificationComptableDto,
  PaiePersonnelLigneDto,
  PaiementHistoriqueDto,
  PaiementPersonnelRequest,
  PaiementRequest,
  PersonnelDto,
  PersonnelRequest,
  RapportFinancierDto,
  RemiseEleveDto,
  RemiseEleveRequest,
  SoldeEleveDto,
} from '../../../core/api/api-dto';

@Injectable({ providedIn: 'root' })
export class ComptabiliteApiService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/comptabilite`;
  private readonly financeBase = `${this.base}/finance`;
  private readonly personnelBase = `${this.base}/personnel`;
  private readonly facturesBase = `${environment.apiUrl}/factures`;

  getStats(): Observable<ComptabiliteStatsDto> {
    return this.http.get<ComptabiliteStatsDto>(`${this.base}/stats`);
  }

  listFactures(
    statut?: string,
    search?: string,
    niveauId?: number,
    classeId?: number,
    eleveId?: number
  ): Observable<FactureListDto[]> {
    const params: Record<string, string> = {};
    if (statut?.trim()) params['statut'] = statut.trim();
    if (search?.trim()) params['search'] = search.trim();
    if (niveauId) params['niveauId'] = String(niveauId);
    if (classeId) params['classeId'] = String(classeId);
    if (eleveId) params['eleveId'] = String(eleveId);
    return this.http.get<FactureListDto[]>(`${this.base}/factures`, { params });
  }

  getFacture(id: number): Observable<FactureDetailDto> {
    return this.http.get<FactureDetailDto>(`${this.base}/factures/${id}`);
  }

  listPaiements(
    from?: string,
    to?: string,
    mode?: string,
    niveauId?: number,
    classeId?: number,
    eleveId?: number,
    search?: string
  ): Observable<PaiementHistoriqueDto[]> {
    const params: Record<string, string> = {};
    if (from) params['from'] = from;
    if (to) params['to'] = to;
    if (mode?.trim()) params['mode'] = mode.trim();
    if (niveauId) params['niveauId'] = String(niveauId);
    if (classeId) params['classeId'] = String(classeId);
    if (eleveId) params['eleveId'] = String(eleveId);
    if (search?.trim()) params['search'] = search.trim();
    return this.http.get<PaiementHistoriqueDto[]>(`${this.base}/paiements`, { params });
  }

  enregistrerPaiement(body: PaiementRequest): Observable<PaiementHistoriqueDto & { id: number }> {
    return this.http.post<PaiementHistoriqueDto & { id: number }>(`${this.facturesBase}/paiements`, body);
  }

  listJournal(
    from?: string,
    to?: string,
    referenceType?: string,
    compte?: string,
    search?: string
  ): Observable<EcritureComptableDto[]> {
    const params: Record<string, string> = {};
    if (from) params['from'] = from;
    if (to) params['to'] = to;
    if (referenceType?.trim()) params['referenceType'] = referenceType.trim();
    if (compte?.trim()) params['compte'] = compte.trim();
    if (search?.trim()) params['search'] = search.trim();
    return this.http.get<EcritureComptableDto[]>(`${this.base}/journal`, { params });
  }

  getBilan(from?: string, to?: string): Observable<BilanDto> {
    const params: Record<string, string> = {};
    if (from) params['from'] = from;
    if (to) params['to'] = to;
    return this.http.get<BilanDto>(`${this.base}/bilan`, { params });
  }

  createEcriture(body: EcritureComptableRequest): Observable<EcritureComptableDto> {
    return this.http.post<EcritureComptableDto>(`${this.base}/journal`, body);
  }

  updateEcriture(id: number, body: EcritureComptableRequest): Observable<EcritureComptableDto> {
    return this.http.put<EcritureComptableDto>(`${this.base}/journal/${id}`, body);
  }

  deleteEcriture(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/journal/${id}`);
  }

  listDemandesPaiement(statut?: string): Observable<DemandePaiementDto[]> {
    const params: Record<string, string> = {};
    if (statut?.trim()) params['statut'] = statut.trim();
    return this.http.get<DemandePaiementDto[]>(`${this.base}/demandes-paiement`, { params });
  }

  countDemandesEnAttente(): Observable<number> {
    return this.http.get<number>(`${this.base}/demandes-paiement/en-attente/count`);
  }

  validerDemandePaiement(id: number): Observable<DemandePaiementDto> {
    return this.http.post<DemandePaiementDto>(`${this.base}/demandes-paiement/${id}/valider`, {});
  }

  refuserDemandePaiement(id: number, motifRefus: string): Observable<DemandePaiementDto> {
    return this.http.post<DemandePaiementDto>(`${this.base}/demandes-paiement/${id}/refuser`, {
      motifRefus,
    });
  }

  listNotifications(): Observable<NotificationComptableDto[]> {
    return this.http.get<NotificationComptableDto[]>(`${this.base}/notifications`);
  }

  markNotificationRead(id: number): Observable<void> {
    return this.http.put<void>(`${this.base}/notifications/${id}/lu`, {});
  }

  markAllNotificationsRead(): Observable<void> {
    return this.http.put<void>(`${this.base}/notifications/lu-tout`, {});
  }

  listPersonnel(categorie?: string, statut?: string, search?: string): Observable<PersonnelDto[]> {
    const params: Record<string, string> = {};
    if (categorie?.trim()) params['categorie'] = categorie.trim();
    if (statut?.trim()) params['statut'] = statut.trim();
    if (search?.trim()) params['search'] = search.trim();
    return this.http.get<PersonnelDto[]>(this.personnelBase, { params });
  }

  getPersonnelCategories(): Observable<Record<string, string>> {
    return this.http.get<Record<string, string>>(`${this.personnelBase}/categories`);
  }

  createPersonnel(body: PersonnelRequest): Observable<PersonnelDto> {
    return this.http.post<PersonnelDto>(this.personnelBase, body);
  }

  updatePersonnel(id: number, body: PersonnelRequest): Observable<PersonnelDto> {
    return this.http.put<PersonnelDto>(`${this.personnelBase}/${id}`, body);
  }

  deletePersonnel(id: number): Observable<void> {
    return this.http.delete<void>(`${this.personnelBase}/${id}`);
  }

  listPaieMois(mois: string): Observable<PaiePersonnelLigneDto[]> {
    return this.http.get<PaiePersonnelLigneDto[]>(`${this.personnelBase}/paie`, { params: { mois } });
  }

  payerPersonnel(body: PaiementPersonnelRequest): Observable<unknown> {
    return this.http.post(`${this.personnelBase}/paie`, body);
  }

  payerTousPersonnel(mois: string, mode = 'VIREMENT'): Observable<unknown[]> {
    return this.http.post<unknown[]>(`${this.personnelBase}/paie/tous`, null, { params: { mois, mode } });
  }

  getDashboardFinance(): Observable<DashboardFinancierDto> {
    return this.http.get<DashboardFinancierDto>(`${this.financeBase}/dashboard`);
  }

  listFrais(): Observable<FraisScolaireDto[]> {
    return this.http.get<FraisScolaireDto[]>(`${this.financeBase}/frais`);
  }

  getFraisTypes(): Observable<Record<string, string>> {
    return this.http.get<Record<string, string>>(`${this.financeBase}/frais/types`);
  }

  createFrais(body: FraisScolaireRequest): Observable<FraisScolaireDto> {
    return this.http.post<FraisScolaireDto>(`${this.financeBase}/frais`, body);
  }

  updateFrais(id: number, body: FraisScolaireRequest): Observable<FraisScolaireDto> {
    return this.http.put<FraisScolaireDto>(`${this.financeBase}/frais/${id}`, body);
  }

  listRemises(eleveId?: number): Observable<RemiseEleveDto[]> {
    const params: Record<string, string> = {};
    if (eleveId) params['eleveId'] = String(eleveId);
    return this.http.get<RemiseEleveDto[]>(`${this.financeBase}/remises`, { params });
  }

  getRemiseTypes(): Observable<Record<string, string>> {
    return this.http.get<Record<string, string>>(`${this.financeBase}/remises/types`);
  }

  createRemise(body: RemiseEleveRequest): Observable<RemiseEleveDto> {
    return this.http.post<RemiseEleveDto>(`${this.financeBase}/remises`, body);
  }

  listDepenses(categorie?: string): Observable<DepenseScolaireDto[]> {
    const params: Record<string, string> = {};
    if (categorie?.trim()) params['categorie'] = categorie.trim();
    return this.http.get<DepenseScolaireDto[]>(`${this.financeBase}/depenses`, { params });
  }

  getDepenseCategories(): Observable<Record<string, string>> {
    return this.http.get<Record<string, string>>(`${this.financeBase}/depenses/categories`);
  }

  createDepense(body: DepenseScolaireRequest): Observable<DepenseScolaireDto> {
    return this.http.post<DepenseScolaireDto>(`${this.financeBase}/depenses`, body);
  }

  getSoldeEleve(eleveId: number): Observable<SoldeEleveDto> {
    return this.http.get<SoldeEleveDto>(`${this.financeBase}/soldes/${eleveId}`);
  }

  getRapport(type: 'JOURNALIER' | 'MENSUEL' | 'ANNUEL'): Observable<RapportFinancierDto> {
    return this.http.get<RapportFinancierDto>(`${this.financeBase}/rapports`, { params: { type } });
  }

  downloadRecuPdf(paiementId: number): Observable<Blob> {
    return this.http.get(`${this.financeBase}/paiements/${paiementId}/recu.pdf`, { responseType: 'blob' });
  }
}
