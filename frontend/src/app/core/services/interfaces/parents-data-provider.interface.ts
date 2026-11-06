import { Observable } from 'rxjs';
import { FilterOption } from '../../models/filter-option.model';
import { ParentsListQuery } from '../../models/list-query.model';
import { Parent } from '../../models/parent.model';

export interface ParentsDataProvider {
  getParents(query?: ParentsListQuery): Observable<readonly Parent[]>;
  getSchoolLevelFilters(): Observable<readonly FilterOption[]>;
}
