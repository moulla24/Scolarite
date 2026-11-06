import { Observable } from 'rxjs';
import { FilterOption } from '../../models/filter-option.model';
import { ScheduleListQuery } from '../../models/list-query.model';
import { ScheduleDay, ScheduleSlot } from '../../models/schedule.model';

export interface ScheduleDataProvider {
  getSlots(query?: ScheduleListQuery): Observable<readonly ScheduleSlot[]>;
  getDays(): Observable<readonly ScheduleDay[]>;
  getClassFilters(): Observable<readonly FilterOption[]>;
}
