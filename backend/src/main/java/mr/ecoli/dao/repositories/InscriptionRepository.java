package mr.ecoli.dao.repositories;

import mr.ecoli.dao.entities.Inscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

public interface InscriptionRepository extends JpaRepository<Inscription, Long> {

    @Query("SELECT COUNT(i) FROM Inscription i WHERE i.statut = 'ACTIF'")
    long countActiveInscriptions();

    @Query("""
            SELECT COUNT(i) FROM Inscription i
            WHERE i.statut = 'ACTIF'
            AND (:niveauId IS NULL OR i.classe.niveau.id = :niveauId)
            """)
    long countActiveInscriptions(@Param("niveauId") Long niveauId);

    @Query("""
            SELECT i FROM Inscription i
            JOIN FETCH i.eleve e
            JOIN FETCH e.tuteur
            JOIN FETCH i.classe c
            JOIN FETCH c.niveau
            ORDER BY i.dateInscription DESC
            """)
    java.util.List<Inscription> findRecentInscriptions(org.springframework.data.domain.Pageable pageable);

    @Query("""
            SELECT i FROM Inscription i
            JOIN FETCH i.classe c
            JOIN FETCH c.niveau
            JOIN FETCH i.anneeScolaire a
            JOIN FETCH a.ecole
            WHERE i.eleve.id = :eleveId AND i.statut = 'ACTIF' AND a.active = true
            """)
    java.util.Optional<Inscription> findActiveForEleve(@org.springframework.data.repository.query.Param("eleveId") Long eleveId);

    @Query("""
            SELECT i FROM Inscription i
            JOIN FETCH i.eleve e
            JOIN FETCH i.anneeScolaire a
            WHERE i.classe.id = :classeId
              AND i.statut = 'ACTIF'
              AND a.active = true
            ORDER BY e.nom, e.prenom
            """)
    java.util.List<Inscription> findActiveByClasseId(@Param("classeId") Long classeId);

    java.util.List<Inscription> findByEleveId(Long eleveId);

    void deleteByEleveId(Long eleveId);

    boolean existsByAnneeScolaire_Id(Long anneeScolaireId);
}
