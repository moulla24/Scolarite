import { Observable } from 'rxjs';
import { FilterOption } from '../../models/filter-option.model';
import { MaquettesListQuery } from '../../models/list-query.model';
import { PedagogicalTemplate } from '../../models/pedagogical-template.model';

export interface MaquettesDataProvider {
  getTemplates(query?: MaquettesListQuery): Observable<readonly PedagogicalTemplate[]>;
  getSubjectFilters(): Observable<readonly FilterOption[]>;
  getLevelFilters(): Observable<readonly FilterOption[]>;
}
