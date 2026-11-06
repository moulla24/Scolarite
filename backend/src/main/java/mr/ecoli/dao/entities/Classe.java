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
import mr.ecoli.dao.entities.Niveau;

@Entity
@Table(name = "classes")
public class Classe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "niveau_id", nullable = false)
    private Niveau niveau;

    @Column(nullable = false, length = 50)
    private String nom;

    @Column(length = 50)
    private String salle;

    @Column(nullable = false)
    private Integer capacite = 30;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Niveau getNiveau() { return niveau; }
    public void setNiveau(Niveau niveau) { this.niveau = niveau; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getSalle() { return salle; }
    public void setSalle(String salle) { this.salle = salle; }
    public Integer getCapacite() { return capacite; }
    public void setCapacite(Integer capacite) { this.capacite = capacite; }

    public String getLibelleComplet() {
        return niveau.getNom() + " " + nom;
    }
}
