package mr.ecoli.matiere.services;

import mr.ecoli.dao.repositories.MatiereRepository;
import mr.ecoli.matiere.dtos.MatiereDto;

import java.util.List;
import mr.ecoli.common.mapper.EntityMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MatiereService {

    private final MatiereRepository matiereRepository;
    private final EntityMapper mapper;

    public MatiereService(MatiereRepository matiereRepository, EntityMapper mapper) {
        this.matiereRepository = matiereRepository;
        this.mapper = mapper;
    }
    public List<MatiereDto> listMatieres() {
        return matiereRepository.findAll().stream()
                .map(mapper::toMatiereDto)
                .toList();
    }
}
