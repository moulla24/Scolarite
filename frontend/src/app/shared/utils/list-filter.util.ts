const SUBJECT_IDS: Record<string, string> = {
  fr: 'français',
  ar: 'arabe',
  math: 'mathématiques',
  sci: 'sciences',
  hist: 'histoire-géographie',
};

export function matchesSearch(query: string, ...fields: string[]): boolean {
  const normalized = query.trim().toLowerCase();
  if (!normalized) {
    return true;
  }
  return fields.some((field) => field.toLowerCase().includes(normalized));
}

export function matchesFilter(selectedId: string, value: string): boolean {
  return selectedId === 'all' || value.toLowerCase() === selectedId.toLowerCase();
}

export function classSlug(className: string): string {
  return className.toLowerCase().replace(/\s+/g, '-');
}

export function matchesSubjectFilter(subjectId: string, subjectLabel: string): boolean {
  if (subjectId === 'all') {
    return true;
  }
  const key = SUBJECT_IDS[subjectId];
  return key ? subjectLabel.toLowerCase().includes(key) : true;
}

export function teacherMatchesSubject(subjectId: string, subjects: readonly string[]): boolean {
  if (subjectId === 'all') {
    return true;
  }
  const key = SUBJECT_IDS[subjectId];
  if (!key) {
    return true;
  }
  return subjects.some((s) => s.toLowerCase().includes(key));
}
