import { SemaineDto, TrimestreDto } from '../../../core/api/api-dto';
import { ScheduleDay } from '../../../core/models/schedule.model';
import { SCHEDULE_DAYS } from '../../../core/api/api-mappers';

export interface ScheduleTimeSlot {
  readonly start: string;
  readonly end: string;
  readonly label: string;
}

/** Créneaux fixes de 2 h affichés dans la grille. */
export const SCHEDULE_TIME_SLOTS: readonly ScheduleTimeSlot[] = [
  { start: '08:00', end: '10:00', label: '08:00 - 10:00' },
  { start: '10:00', end: '12:00', label: '10:00 - 12:00' },
  { start: '12:00', end: '14:00', label: '12:00 - 14:00' },
  { start: '14:00', end: '16:00', label: '14:00 - 16:00' },
  { start: '16:00', end: '18:00', label: '16:00 - 18:00' },
];

export interface ScheduleWeek {
  readonly id: number;
  readonly index: number;
  readonly startDate: Date;
  readonly endDate: Date;
  readonly label: string;
}

export function mapSemaineDto(dto: SemaineDto): ScheduleWeek {
  return {
    id: dto.id,
    index: dto.numero,
    startDate: parseLocalDate(dto.dateDebut),
    endDate: parseLocalDate(dto.dateFin),
    label: dto.label,
  };
}

export function parseLocalDate(iso: string): Date {
  const [y, m, d] = iso.split('-').map(Number);
  return new Date(y, m - 1, d);
}

export function formatIsoDate(date: Date): string {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

export function startOfWeekMonday(date: Date): Date {
  const copy = new Date(date);
  const day = copy.getDay();
  const diff = day === 0 ? -6 : 1 - day;
  copy.setDate(copy.getDate() + diff);
  copy.setHours(0, 0, 0, 0);
  return copy;
}

export function addDays(date: Date, days: number): Date {
  const copy = new Date(date);
  copy.setDate(copy.getDate() + days);
  return copy;
}

export function findCurrentTrimester(
  trimestres: readonly TrimestreDto[],
  today = new Date()
): TrimestreDto | null {
  if (!trimestres.length) return null;
  const todayIso = formatIsoDate(today);
  const byDate = trimestres.find((t) => t.dateDebut <= todayIso && todayIso <= t.dateFin);
  if (byDate) return byDate;
  const actif = trimestres.find((t) => t.actif);
  if (actif) return actif;
  return [...trimestres].sort((a, b) => b.numero - a.numero)[0] ?? null;
}

export function findCurrentWeekIndex(weeks: readonly ScheduleWeek[], today = new Date()): number {
  if (!weeks.length) return 0;
  const t = new Date(today);
  t.setHours(0, 0, 0, 0);
  const idx = weeks.findIndex((w) => t >= w.startDate && t <= w.endDate);
  if (idx >= 0) return idx;
  if (t < weeks[0].startDate) return 0;
  return weeks.length - 1;
}

export function buildDaysForWeek(week: ScheduleWeek): ScheduleDay[] {
  return SCHEDULE_DAYS.map((day, offset) => {
    const date = addDays(week.startDate, offset);
    return {
      label: day.label,
      index: day.index,
      dateLabel: date.toLocaleDateString('fr-FR', { day: 'numeric', month: 'short' }),
    };
  });
}

export function resolveTimeSlotStart(startTime: string): string | null {
  for (const slot of SCHEDULE_TIME_SLOTS) {
    if (startTime === slot.start) return slot.start;
    if (startTime > slot.start && startTime < slot.end) return slot.start;
  }
  return null;
}

export function isCurrentWeek(week: ScheduleWeek, today = new Date()): boolean {
  const t = new Date(today);
  t.setHours(0, 0, 0, 0);
  return t >= week.startDate && t <= week.endDate;
}
