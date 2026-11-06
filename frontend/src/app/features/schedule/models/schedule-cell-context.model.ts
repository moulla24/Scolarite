import { ScheduleSlot } from '../../../core/models/schedule.model';

export interface ScheduleCellContext {
  readonly semaineId: number;
  readonly dayIndex: number;
  readonly dayLabel: string;
  readonly dateLabel?: string;
  readonly timeLabel: string;
  readonly startTime: string;
  readonly endTime: string;
  readonly classId: string;
  readonly classLabel: string;
  readonly slot: ScheduleSlot | null;
}
