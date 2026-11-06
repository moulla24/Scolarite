import {
  BulletinEleveDto,
  ClasseDto,
  EleveDto,
  EnseignantDto,
  MaquetteDto,
  MatiereDto,
  NiveauDto,
  SeanceEmploiDto,
  TuteurDto,
} from './api-dto';
import { DashboardStat } from '../models/dashboard-stats.model';
import { FilterOption } from '../models/filter-option.model';
import { GradeRecord } from '../models/grade-record.model';
import { Parent, ParentStudentSummary } from '../models/parent.model';
import { PedagogicalTemplate, TemplateFormat } from '../models/pedagogical-template.model';
import { Classroom } from '../models/classroom.model';
import { ScheduleDay, ScheduleSlot } from '../models/schedule.model';
import { SchoolLevel } from '../models/school-level.model';
import { Student, StudentAccountStatus } from '../models/student.model';
import { Teacher, TeacherStatus } from '../models/teacher.model';
import { DashboardStatsDto } from './api-dto';

export function niveauSlug(nom: string): string {
  return nom.trim().toLowerCase();
}

export function toLevelFilters(niveaux: readonly NiveauDto[]): readonly FilterOption[] {
  return [
    { id: 'all', label: 'Tous les niveaux' },
    ...niveaux.map((n) => ({ id: String(n.id), label: n.nom })),
  ];
}

export function toSchoolLevels(niveaux: readonly NiveauDto[]): readonly SchoolLevel[] {
  return [
    { id: 'all', label: 'Tous les niveaux' },
    ...niveaux.map((n) => ({ id: String(n.id), label: n.nom })),
  ];
}

export function classFilterLabel(classe: ClasseDto): string {
  const nom = classe.nom.trim();
  const niveau = classe.niveau.trim();
  if (nom.toLowerCase().startsWith(niveau.toLowerCase())) {
    return nom;
  }
  return `${niveau} ${nom}`;
}

export function toClassFilters(classes: readonly ClasseDto[]): readonly FilterOption[] {
  return [
    { id: 'all', label: 'Toutes les classes' },
    ...classes.map((c) => ({ id: String(c.id), label: classFilterLabel(c) })),
  ];
}

export function toSubjectFilters(matieres: readonly MatiereDto[]): readonly FilterOption[] {
  return [
    { id: 'all', label: 'Toutes les matières' },
    ...matieres.map((m) => ({ id: String(m.id), label: m.nom })),
  ];
}

export function mapDashboardStats(dto: DashboardStatsDto): readonly DashboardStat[] {
  return [
    {
      id: 'active-students',
      title: 'Élèves inscrits',
      value: String(dto.elevesInscrits),
      detail: `${dto.elevesActifs} actifs`,
      icon: 'groups',
      accent: 'blue',
    },
    {
      id: 'teachers',
      title: 'Enseignants',
      value: String(dto.enseignantsActifs),
      detail: 'Corps enseignant',
      icon: 'school',
      accent: 'teal',
    },
    {
      id: 'classes',
      title: 'Classes',
      value: String(dto.classesTotal),
      detail: 'Sections ouvertes',
      icon: 'class',
      accent: 'violet',
    },
    {
      id: 'sessions',
      title: 'Créneaux emploi du temps',
      value: String(dto.seancesSemaine),
      detail: 'Séances planifiées',
      icon: 'calendar_month',
      accent: 'warning',
    },
  ];
}

export function mapParent(dto: TuteurDto, niveauIdForFilter = ''): Parent {
  const students: ParentStudentSummary[] = dto.nomsEleves.map((name) => ({ name }));
  return {
    id: String(dto.id),
    fullName: dto.nomComplet,
    initials: initialsFromName(dto.nomComplet),
    phone: dto.telephone ?? '—',
    email: dto.email ?? '—',
    students,
    balance: Number(dto.solde ?? 0),
    status: dto.statutCompte === 'A_JOUR' ? 'up_to_date' : 'debt',
    schoolLevel: niveauIdForFilter,
  };
}

export function mapStudent(dto: EleveDto, niveauId = ''): Student {
  return {
    id: String(dto.id),
    fullName: dto.nomComplet,
    initials: dto.initials,
    matricule: dto.matricule,
    className: dto.classe,
    classId: '',
    level: niveauSlug(dto.niveau),
    levelId: niveauId,
    balance: Number(dto.solde ?? 0),
    status: mapStudentStatus(dto.statut),
  };
}

export function mapGradeRecord(dto: BulletinEleveDto, niveauId = ''): GradeRecord {
  return {
    id: String(dto.eleveId),
    fullName: dto.nomComplet,
    className: dto.classe,
    classId: '',
    level: '',
    levelId: niveauId,
    subjectCount: dto.nombreMatieres,
  };
}

export function mapTeacher(dto: EnseignantDto): Teacher {
  return {
    id: String(dto.id),
    fullName: dto.nomComplet,
    initials: dto.initials,
    email: dto.email ?? '—',
    phone: dto.telephone ?? '—',
    subjects: dto.matieres ?? [],
    classes: dto.classes ?? [],
    status: dto.statut === 'ACTIF' ? 'active' : 'on_leave',
  };
}

export function mapClassroom(dto: ClasseDto, niveauId = ''): Classroom {
  return {
    id: String(dto.id),
    name: dto.nom,
    level: niveauSlug(dto.niveau),
    levelId: niveauId,
    teacherName: dto.enseignantReferent,
    studentCount: dto.effectif,
    capacity: dto.capacite,
    room: dto.salle ?? '—',
  };
}

export function mapMaquette(dto: MaquetteDto, niveauId = ''): PedagogicalTemplate {
  return {
    id: String(dto.id),
    title: dto.titre,
    subject: dto.matiere,
    subjectId: '',
    level: dto.niveau,
    levelId: niveauId,
    description: dto.description ?? '',
    format: normalizeFormat(dto.format),
    updatedAt: formatDate(dto.dateMaj),
    pageCount: dto.nbPages,
    fichierUrl: dto.fichierUrl,
  };
}

export function mapSeance(dto: SeanceEmploiDto): ScheduleSlot {
  const [startTime = '', endTime = ''] = dto.horaire.split(' - ').map((s) => s.trim());
  return {
    id: String(dto.id),
    semaineId: String(dto.semaineId),
    day: dto.jour,
    dayIndex: dto.jourIndex,
    startTime,
    endTime,
    className: dto.classe,
    classId: String(dto.classeId),
    matiereId: String(dto.matiereId),
    enseignantId: String(dto.enseignantId),
    subject: dto.matiere,
    teacherName: dto.enseignant,
    room: dto.salle ?? '—',
  };
}

export const SCHEDULE_DAYS: readonly ScheduleDay[] = [
  { label: 'Lundi', index: 1 },
  { label: 'Mardi', index: 2 },
  { label: 'Mercredi', index: 3 },
  { label: 'Jeudi', index: 4 },
  { label: 'Vendredi', index: 5 },
];

function mapStudentStatus(statut: string): StudentAccountStatus {
  return statut === 'A_JOUR' ? 'up_to_date' : 'debt';
}

function initialsFromName(name: string): string {
  const parts = name.split(/\s+/).filter(Boolean);
  if (parts.length === 0) return '?';
  if (parts.length === 1) return parts[0].substring(0, 2).toUpperCase();
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
}

function normalizeFormat(format: string): TemplateFormat {
  const f = format?.toLowerCase() ?? 'pdf';
  if (f === 'docx' || f === 'pptx' || f === 'pdf') return f;
  return 'pdf';
}

function formatDate(iso: string): string {
  if (!iso) return '—';
  const [y, m, d] = iso.split('-');
  return d && m && y ? `${d}/${m}/${y}` : iso;
}
