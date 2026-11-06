package mr.ecoli.classe.services;

import java.util.List;
import mr.ecoli.common.exception.BadRequestException;
import mr.ecoli.classe.dtos.ClasseDto;
import mr.ecoli.classe.dtos.ClasseRequest;
import mr.ecoli.common.mapper.EntityMapper;
import mr.ecoli.common.EntityReferenceService;
import mr.ecoli.dao.entities.Classe;
import mr.ecoli.dao.entities.Enseignement;
import mr.ecoli.dao.repositories.ClasseRepository;
import mr.ecoli.dao.repositories.EnseignementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClasseService {

    private final ClasseRepository classeRepository;
    private final EnseignementRepository enseignementRepository;
    private final EntityMapper mapper;
    private final EntityReferenceService refs;

    public ClasseService(
            ClasseRepository classeRepository,
            EnseignementRepository enseignementRepository,
            EntityMapper mapper,
            EntityReferenceService refs) {
        this.classeRepository = classeRepository;
        this.enseignementRepository = enseignementRepository;
        this.mapper = mapper;
        this.refs = refs;
    }
    @Transactional(readOnly = true)
    public List<ClasseDto> listClasses(String search, Long niveauId) {
        return classeRepository.search(normalize(search), niveauId).stream()
                .map(this::toDto)
                .toList();
    }
    @Transactional(readOnly = true)
    public ClasseDto getById(Long id) {
        return toDto(refs.requireClasse(id));
    }
    public ClasseDto create(ClasseRequest request) {
        Classe classe = new Classe();
        apply(classe, request);
        return toDto(classeRepository.save(classe));
    }
    public ClasseDto update(Long id, ClasseRequest request) {
        Classe classe = refs.requireClasse(id);
        apply(classe, request);
        return toDto(classeRepository.save(classe));
    }
    public void delete(Long id) {
        if (classeRepository.countActiveStudents(id) > 0) {
            throw new BadRequestException("Impossible de supprimer : des élèves sont inscrits dans cette classe.");
        }
        classeRepository.delete(refs.requireClasse(id));
    }

    private void apply(Classe classe, ClasseRequest request) {
        classe.setNiveau(refs.requireNiveau(request.niveauId()));
        classe.setNom(request.nom());
        classe.setSalle(request.salle());
        classe.setCapacite(request.capacite());
    }

    private ClasseDto toDto(Classe classe) {
        long effectif = classeRepository.countActiveStudents(classe.getId());
        String referent = enseignementRepository.findByClasseId(classe.getId()).stream()
                .findFirst()
                .map(Enseignement::getEnseignant)
                .map(e -> e.getUtilisateur().getPrenom() + " " + e.getUtilisateur().getNom())
                .orElse(null);
        return mapper.toClasseDto(classe, effectif, referent);
    }

    private String normalize(String search) {
        return search == null ? "" : search.trim();
    }
}
