export type TeacherStatus = 'active' | 'on_leave';

export interface Teacher {
  readonly id: string;
  readonly fullName: string;
  readonly initials: string;
  readonly email: string;
  readonly phone: string;
  readonly subjects: readonly string[];
  readonly classes: readonly string[];
  readonly status: TeacherStatus;
}
