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
@Table(name = "trimestre_types_controles")
public class TrimestreTypeControle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trimestre_id", nullable = false)
    private Trimestre trimestre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "type_controle_id", nullable = false)
    private TypeControle typeControle;

    @Column(name = "nombre_prevu", nullable = false)
    private Short nombrePrevu = 1;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Trimestre getTrimestre() { return trimestre; }
    public void setTrimestre(Trimestre trimestre) { this.trimestre = trimestre; }
    public TypeControle getTypeControle() { return typeControle; }
    public void setTypeControle(TypeControle typeControle) { this.typeControle = typeControle; }
    public Short getNombrePrevu() { return nombrePrevu; }
    public void setNombrePrevu(Short nombrePrevu) { this.nombrePrevu = nombrePrevu; }
}
