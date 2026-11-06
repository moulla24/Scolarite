package mr.ecoli.dao.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "maquette_matieres")
public class MaquetteMatiere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "maquette_classe_id", nullable = false)
    private MaquetteClasse maquetteClasse;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "matiere_id", nullable = false)
    private Matiere matiere;

    @Column(name = "volume_horaire", nullable = false)
    private BigDecimal volumeHoraire = BigDecimal.valueOf(3);

    @OneToMany(mappedBy = "maquetteMatiere", fetch = FetchType.LAZY)
    private List<MaquetteChapitre> chapitres = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public MaquetteClasse getMaquetteClasse() { return maquetteClasse; }
    public void setMaquetteClasse(MaquetteClasse maquetteClasse) { this.maquetteClasse = maquetteClasse; }
    public Matiere getMatiere() { return matiere; }
    public void setMatiere(Matiere matiere) { this.matiere = matiere; }
    public BigDecimal getVolumeHoraire() { return volumeHoraire; }
    public void setVolumeHoraire(BigDecimal volumeHoraire) { this.volumeHoraire = volumeHoraire; }
    public List<MaquetteChapitre> getChapitres() { return chapitres; }
    public void setChapitres(List<MaquetteChapitre> chapitres) { this.chapitres = chapitres; }
}
