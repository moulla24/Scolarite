package mr.ecoli.dao.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "maquette_chapitres")
public class MaquetteChapitre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "maquette_matiere_id", nullable = false)
    private MaquetteMatiere maquetteMatiere;

    @Column(nullable = false)
    private short ordre = 1;

    @Column(nullable = false, length = 200)
    private String titre;

    private String description;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public MaquetteMatiere getMaquetteMatiere() { return maquetteMatiere; }
    public void setMaquetteMatiere(MaquetteMatiere maquetteMatiere) { this.maquetteMatiere = maquetteMatiere; }
    public short getOrdre() { return ordre; }
    public void setOrdre(short ordre) { this.ordre = ordre; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
