package mr.ecoli.dao.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "compteur_recu")
public class CompteurRecu {

    @Id
    private Integer annee;

    @Column(name = "dernier_numero", nullable = false)
    private int dernierNumero;

    public Integer getAnnee() { return annee; }
    public void setAnnee(Integer annee) { this.annee = annee; }
    public int getDernierNumero() { return dernierNumero; }
    public void setDernierNumero(int dernierNumero) { this.dernierNumero = dernierNumero; }
}
