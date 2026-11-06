export interface ScheduleSlot {
  readonly id: string;
  readonly semaineId: string;
  readonly day: string;
  readonly dayIndex: number;
  readonly startTime: string;
  readonly endTime: string;
  readonly className: string;
  readonly classId: string;
  readonly matiereId: string;
  readonly enseignantId: string;
  readonly subject: string;
  readonly teacherName: string;
  readonly room: string;
}

export interface ScheduleDay {
  readonly label: string;
  readonly index: number;
  readonly dateLabel?: string;
}
