package mr.ecoli.niveau.services;

import mr.ecoli.dao.entities.Niveau;
import mr.ecoli.dao.repositories.NiveauRepository;
import mr.ecoli.niveau.dtos.NiveauDto;
import mr.ecoli.niveau.dtos.NiveauRequest;

import java.util.List;
import mr.ecoli.common.EntityReferenceService;
import mr.ecoli.common.mapper.EntityMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NiveauService {

    private final NiveauRepository niveauRepository;
    private final EntityMapper mapper;
    private final EntityReferenceService refs;

    public NiveauService(
            NiveauRepository niveauRepository, EntityMapper mapper, EntityReferenceService refs) {
        this.niveauRepository = niveauRepository;
        this.mapper = mapper;
        this.refs = refs;
    }
    @Transactional(readOnly = true)
    public List<NiveauDto> listNiveaux() {
        return niveauRepository.findAllByOrderByNomAsc().stream()
                .map(mapper::toNiveauDto)
                .toList();
    }
    @Transactional(readOnly = true)
    public NiveauDto getNiveau(Long id) {
        return mapper.toNiveauDto(refs.requireNiveau(id));
    }
    public NiveauDto createNiveau(NiveauRequest request) {
        Niveau n = new Niveau();
        n.setEcole(refs.requireEcole(request.ecoleId()));
        n.setNom(request.nom());
        return mapper.toNiveauDto(niveauRepository.save(n));
    }
    public NiveauDto updateNiveau(Long id, NiveauRequest request) {
        Niveau n = refs.requireNiveau(id);
        n.setEcole(refs.requireEcole(request.ecoleId()));
        n.setNom(request.nom());
        return mapper.toNiveauDto(niveauRepository.save(n));
    }
    public void deleteNiveau(Long id) {
        niveauRepository.delete(refs.requireNiveau(id));
    }
}
