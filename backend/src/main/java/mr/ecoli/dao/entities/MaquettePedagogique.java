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
import java.time.LocalDate;
import mr.ecoli.dao.entities.Matiere;
import mr.ecoli.dao.entities.Niveau;

@Entity
@Table(name = "maquettes_pedagogiques")
public class MaquettePedagogique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "matiere_id", nullable = false)
    private Matiere matiere;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "niveau_id", nullable = false)
    private Niveau niveau;

    private String description;

    @Column(nullable = false, length = 10)
    private String format = "PDF";

    @Column(name = "fichier_url", length = 500)
    private String fichierUrl;

    @Column(name = "nb_pages", nullable = false)
    private Integer nbPages = 0;

    @Column(name = "date_maj", nullable = false)
    private LocalDate dateMaj = LocalDate.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public Matiere getMatiere() { return matiere; }
    public void setMatiere(Matiere matiere) { this.matiere = matiere; }
    public Niveau getNiveau() { return niveau; }
    public void setNiveau(Niveau niveau) { this.niveau = niveau; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public String getFichierUrl() { return fichierUrl; }
    public void setFichierUrl(String fichierUrl) { this.fichierUrl = fichierUrl; }
    public Integer getNbPages() { return nbPages; }
    public void setNbPages(Integer nbPages) { this.nbPages = nbPages; }
    public LocalDate getDateMaj() { return dateMaj; }
    public void setDateMaj(LocalDate dateMaj) { this.dateMaj = dateMaj; }
}
