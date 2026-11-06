import { Observable } from 'rxjs';
import { Classroom } from '../../models/classroom.model';
import { FilterOption } from '../../models/filter-option.model';
import { ClassroomsListQuery } from '../../models/list-query.model';

export interface ClassroomsDataProvider {
  getClassrooms(query?: ClassroomsListQuery): Observable<readonly Classroom[]>;
  getLevelFilters(): Observable<readonly FilterOption[]>;
}
