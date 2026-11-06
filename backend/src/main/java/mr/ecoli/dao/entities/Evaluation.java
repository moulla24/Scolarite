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
import mr.ecoli.dao.entities.Enseignement;

@Entity
@Table(name = "evaluations")
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enseignement_id", nullable = false)
    private Enseignement enseignement;

    @Column(nullable = false, length = 40)
    private String type;

    @Column(name = "date_evaluation", nullable = false)
    private LocalDate dateEvaluation;

    @Column(name = "note_max", nullable = false)
    private BigDecimal noteMax = BigDecimal.valueOf(20);

    @Column(nullable = false)
    private short trimestre = 1;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Enseignement getEnseignement() { return enseignement; }
    public void setEnseignement(Enseignement enseignement) { this.enseignement = enseignement; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public LocalDate getDateEvaluation() { return dateEvaluation; }
    public void setDateEvaluation(LocalDate dateEvaluation) { this.dateEvaluation = dateEvaluation; }
    public BigDecimal getNoteMax() { return noteMax; }
    public void setNoteMax(BigDecimal noteMax) { this.noteMax = noteMax; }
    public short getTrimestre() { return trimestre; }
    public void setTrimestre(short trimestre) { this.trimestre = trimestre; }
}
