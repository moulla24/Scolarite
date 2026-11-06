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
import java.math.BigDecimal;

@Entity
@Table(name = "regles_penalite")
public class ReglePenalite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ecole_id", nullable = false)
    private Ecole ecole;

    @Column(name = "jours_retard_min", nullable = false)
    private int joursRetardMin = 30;

    @Column(name = "type_penalite", nullable = false, length = 20)
    private String typePenalite = "FIXE";

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valeur;

    @Column(nullable = false)
    private boolean actif = true;

    public Long getId() { return id; }
    public Ecole getEcole() { return ecole; }
    public void setEcole(Ecole ecole) { this.ecole = ecole; }
    public int getJoursRetardMin() { return joursRetardMin; }
    public void setJoursRetardMin(int joursRetardMin) { this.joursRetardMin = joursRetardMin; }
    public String getTypePenalite() { return typePenalite; }
    public void setTypePenalite(String typePenalite) { this.typePenalite = typePenalite; }
    public BigDecimal getValeur() { return valeur; }
    public void setValeur(BigDecimal valeur) { this.valeur = valeur; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
}
