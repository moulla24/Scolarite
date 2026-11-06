package mr.ecoli.maquette.services;

import java.time.LocalDate;
import java.util.List;
import mr.ecoli.maquette.dtos.MaquetteDto;
import mr.ecoli.maquette.dtos.MaquetteRequest;
import mr.ecoli.common.mapper.EntityMapper;
import mr.ecoli.common.EntityReferenceService;
import mr.ecoli.dao.entities.MaquettePedagogique;
import mr.ecoli.dao.repositories.MaquettePedagogiqueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MaquetteService {

    private final MaquettePedagogiqueRepository repository;
    private final EntityMapper mapper;
    private final EntityReferenceService refs;

    public MaquetteService(
            MaquettePedagogiqueRepository repository,
            EntityMapper mapper,
            EntityReferenceService refs) {
        this.repository = repository;
        this.mapper = mapper;
        this.refs = refs;
    }
    @Transactional(readOnly = true)
    public List<MaquetteDto> listMaquettes(String search, Long matiereId, Long niveauId) {
        return repository.search(normalize(search), matiereId, niveauId).stream()
                .map(mapper::toMaquetteDto)
                .toList();
    }
    @Transactional(readOnly = true)
    public MaquetteDto getById(Long id) {
        return mapper.toMaquetteDto(refs.requireMaquette(id));
    }
    public MaquetteDto create(MaquetteRequest request) {
        MaquettePedagogique m = new MaquettePedagogique();
        apply(m, request);
        return mapper.toMaquetteDto(repository.save(m));
    }
    public MaquetteDto update(Long id, MaquetteRequest request) {
        MaquettePedagogique m = refs.requireMaquette(id);
        apply(m, request);
        return mapper.toMaquetteDto(repository.save(m));
    }
    public void delete(Long id) {
        repository.delete(refs.requireMaquette(id));
    }

    private void apply(MaquettePedagogique m, MaquetteRequest request) {
        m.setTitre(request.titre());
        m.setMatiere(refs.requireMatiere(request.matiereId()));
        m.setNiveau(refs.requireNiveau(request.niveauId()));
        m.setDescription(request.description());
        m.setFormat(request.format());
        m.setFichierUrl(request.fichierUrl());
        m.setNbPages(request.nbPages());
        m.setDateMaj(request.dateMaj() != null ? request.dateMaj() : LocalDate.now());
    }

    private String normalize(String search) {
        return search == null ? "" : search.trim();
    }
}
