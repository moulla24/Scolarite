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
import java.time.LocalDate;

@Entity
@Table(name = "historique_retards")
public class HistoriqueRetard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facture_id")
    private Facture facture;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "eleve_id", nullable = false)
    private Eleve eleve;

    @Column(name = "libelle_echeance", nullable = false, length = 120)
    private String libelleEcheance;

    @Column(name = "date_echeance", nullable = false)
    private LocalDate dateEcheance;

    @Column(name = "jours_retard", nullable = false)
    private int joursRetard;

    @Column(name = "montant_penalite", nullable = false, precision = 12, scale = 2)
    private BigDecimal montantPenalite = BigDecimal.ZERO;

    @Column(name = "date_constat", nullable = false)
    private LocalDate dateConstat = LocalDate.now();

    public Long getId() { return id; }
    public Facture getFacture() { return facture; }
    public void setFacture(Facture facture) { this.facture = facture; }
    public Eleve getEleve() { return eleve; }
    public void setEleve(Eleve eleve) { this.eleve = eleve; }
    public String getLibelleEcheance() { return libelleEcheance; }
    public void setLibelleEcheance(String libelleEcheance) { this.libelleEcheance = libelleEcheance; }
    public LocalDate getDateEcheance() { return dateEcheance; }
    public void setDateEcheance(LocalDate dateEcheance) { this.dateEcheance = dateEcheance; }
    public int getJoursRetard() { return joursRetard; }
    public void setJoursRetard(int joursRetard) { this.joursRetard = joursRetard; }
    public BigDecimal getMontantPenalite() { return montantPenalite; }
    public void setMontantPenalite(BigDecimal montantPenalite) { this.montantPenalite = montantPenalite; }
    public LocalDate getDateConstat() { return dateConstat; }
    public void setDateConstat(LocalDate dateConstat) { this.dateConstat = dateConstat; }
}
