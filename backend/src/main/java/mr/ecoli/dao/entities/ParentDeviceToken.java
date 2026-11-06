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
import java.time.LocalDateTime;

@Entity
@Table(name = "parent_device_tokens")
public class ParentDeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tuteur_id", nullable = false)
    private Tuteur tuteur;

    @Column(nullable = false, length = 500)
    private String token;

    @Column(length = 20)
    private String plateforme;

    @Column(name = "date_enregistrement", nullable = false)
    private LocalDateTime dateEnregistrement = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Tuteur getTuteur() { return tuteur; }
    public void setTuteur(Tuteur tuteur) { this.tuteur = tuteur; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getPlateforme() { return plateforme; }
    public void setPlateforme(String plateforme) { this.plateforme = plateforme; }
    public LocalDateTime getDateEnregistrement() { return dateEnregistrement; }
    public void setDateEnregistrement(LocalDateTime dateEnregistrement) {
        this.dateEnregistrement = dateEnregistrement;
    }
}
