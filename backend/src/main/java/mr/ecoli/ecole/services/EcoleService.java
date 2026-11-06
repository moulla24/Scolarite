package mr.ecoli.ecole.services;

import java.util.List;
import mr.ecoli.common.exception.BadRequestException;
import mr.ecoli.ecole.dtos.EcoleDto;
import mr.ecoli.ecole.dtos.EcoleRequest;
import mr.ecoli.common.mapper.EntityMapper;
import mr.ecoli.common.EntityReferenceService;
import mr.ecoli.dao.entities.Ecole;
import mr.ecoli.dao.repositories.AnneeScolaireRepository;
import mr.ecoli.dao.repositories.EcoleRepository;
import mr.ecoli.dao.repositories.EleveRepository;
import mr.ecoli.dao.repositories.NiveauRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EcoleService {

    private final EcoleRepository ecoleRepository;
    private final NiveauRepository niveauRepository;
    private final EleveRepository eleveRepository;
    private final AnneeScolaireRepository anneeScolaireRepository;
    private final EntityMapper mapper;
    private final EntityReferenceService refs;

    public EcoleService(
            EcoleRepository ecoleRepository,
            NiveauRepository niveauRepository,
            EleveRepository eleveRepository,
            AnneeScolaireRepository anneeScolaireRepository,
            EntityMapper mapper,
            EntityReferenceService refs) {
        this.ecoleRepository = ecoleRepository;
        this.niveauRepository = niveauRepository;
        this.eleveRepository = eleveRepository;
        this.anneeScolaireRepository = anneeScolaireRepository;
        this.mapper = mapper;
        this.refs = refs;
    }
    @Transactional(readOnly = true)
    public List<EcoleDto> listEcoles() {
        return ecoleRepository.findAllByOrderByNomAsc().stream()
                .map(mapper::toEcoleDto)
                .toList();
    }
    @Transactional(readOnly = true)
    public EcoleDto getById(Long id) {
        return mapper.toEcoleDto(refs.requireEcole(id));
    }
    public EcoleDto create(EcoleRequest request) {
        assertCodeUnique(request.code(), null);
        Ecole ecole = new Ecole();
        applyRequest(ecole, request);
        return mapper.toEcoleDto(ecoleRepository.save(ecole));
    }
    public EcoleDto update(Long id, EcoleRequest request) {
        assertCodeUnique(request.code(), id);
        Ecole ecole = refs.requireEcole(id);
        applyRequest(ecole, request);
        return mapper.toEcoleDto(ecoleRepository.save(ecole));
    }
    public void delete(Long id) {
        refs.requireEcole(id);
        if (niveauRepository.existsByEcole_Id(id)
                || eleveRepository.existsByEcole_Id(id)
                || anneeScolaireRepository.existsByEcole_Id(id)) {
            throw new BadRequestException(
                    "Impossible de supprimer cette école : des niveaux, élèves ou années scolaires y sont rattachés.");
        }
        ecoleRepository.deleteById(id);
    }

    private void applyRequest(Ecole ecole, EcoleRequest request) {
        ecole.setNom(request.nom().trim());
        ecole.setCode(request.code().trim().toUpperCase());
        ecole.setAdresse(request.adresse());
        ecole.setTelephone(request.telephone());
        ecole.setEmail(request.email());
        ecole.setLogoUrl(request.logoUrl());
    }

    private void assertCodeUnique(String code, Long excludeId) {
        String normalized = code.trim().toUpperCase();
        boolean exists = excludeId == null
                ? ecoleRepository.existsByCode(normalized)
                : ecoleRepository.existsByCodeAndIdNot(normalized, excludeId);
        if (exists) {
            throw new BadRequestException("Le code école « " + normalized + " » est déjà utilisé.");
        }
    }
}
