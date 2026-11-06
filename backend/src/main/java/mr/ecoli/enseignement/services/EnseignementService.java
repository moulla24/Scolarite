package mr.ecoli.enseignement.services;



import java.util.List;

import mr.ecoli.common.EntityReferenceService;

import mr.ecoli.common.exception.BadRequestException;
import mr.ecoli.common.exception.ResourceNotFoundException;

import mr.ecoli.dao.entities.Enseignement;

import mr.ecoli.dao.repositories.EnseignementRepository;

import mr.ecoli.dao.repositories.EvaluationRepository;

import mr.ecoli.enseignement.dtos.EnseignementDto;

import mr.ecoli.enseignement.dtos.EnseignementRequest;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;



@Service

@Transactional

public class EnseignementService {



    private final EnseignementRepository enseignementRepository;

    private final EvaluationRepository evaluationRepository;

    private final EntityReferenceService refs;



    public EnseignementService(

            EnseignementRepository enseignementRepository,

            EvaluationRepository evaluationRepository,

            EntityReferenceService refs) {

        this.enseignementRepository = enseignementRepository;

        this.evaluationRepository = evaluationRepository;

        this.refs = refs;

    }



    @Transactional(readOnly = true)

    public List<EnseignementDto> list(Long classeId, Long matiereId) {

        return enseignementRepository.search(classeId, matiereId).stream()

                .map(this::toDto)

                .toList();

    }



    @Transactional(readOnly = true)

    public EnseignementDto getById(Long id) {

        return toDto(requireWithDetails(id));

    }



    public EnseignementDto create(EnseignementRequest request) {

        ensureUniqueCombo(request.classeId(), request.matiereId(), null);

        Enseignement en = new Enseignement();

        apply(en, request);

        en = enseignementRepository.save(en);

        return toDto(requireWithDetails(en.getId()));

    }



    public EnseignementDto update(Long id, EnseignementRequest request) {

        Enseignement en = requireWithDetails(id);

        ensureUniqueCombo(request.classeId(), request.matiereId(), id);

        apply(en, request);

        enseignementRepository.save(en);

        return toDto(requireWithDetails(id));

    }



    public void delete(Long id) {

        requireWithDetails(id);

        if (evaluationRepository.existsByEnseignement_Id(id)) {

            throw new BadRequestException(

                    "Impossible de supprimer : des évaluations sont liées à cette affectation.");

        }

        enseignementRepository.deleteById(id);

    }



    private void apply(Enseignement en, EnseignementRequest request) {

        en.setClasse(refs.requireClasse(request.classeId()));

        en.setMatiere(refs.requireMatiere(request.matiereId()));

        en.setEnseignant(refs.requireEnseignant(request.enseignantId()));

    }



    private void ensureUniqueCombo(Long classeId, Long matiereId, Long excludeId) {

        boolean exists = excludeId == null

                ? enseignementRepository.existsByClasseIdAndMatiereId(classeId, matiereId)

                : enseignementRepository.existsByClasseIdAndMatiereIdAndIdNot(classeId, matiereId, excludeId);

        if (exists) {

            throw new BadRequestException("Cette matière est déjà assignée à cette classe.");

        }

    }



    private Enseignement requireWithDetails(Long id) {

        return enseignementRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enseignement introuvable : " + id));

    }



    private EnseignementDto toDto(Enseignement en) {

        var u = en.getEnseignant().getUtilisateur();

        String enseignantNom = u.getPrenom() + " " + u.getNom();

        String classe = en.getClasse().getLibelleComplet();

        String matiere = en.getMatiere().getNom();

        return new EnseignementDto(

                en.getId(),

                en.getClasse().getId(),

                classe,

                en.getMatiere().getId(),

                matiere,

                en.getEnseignant().getId(),

                enseignantNom,

                matiere + " — " + classe + " (" + enseignantNom + ")");

    }

}

