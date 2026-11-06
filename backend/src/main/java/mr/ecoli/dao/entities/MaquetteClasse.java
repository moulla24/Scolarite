package mr.ecoli.dao.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "maquettes_classe")
public class MaquetteClasse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "classe_id", nullable = false, unique = true)
    private Classe classe;

    @OneToMany(mappedBy = "maquetteClasse", fetch = FetchType.LAZY)
    private List<MaquetteMatiere> matieres = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Classe getClasse() { return classe; }
    public void setClasse(Classe classe) { this.classe = classe; }
    public List<MaquetteMatiere> getMatieres() { return matieres; }
    public void setMatieres(List<MaquetteMatiere> matieres) { this.matieres = matieres; }
}
