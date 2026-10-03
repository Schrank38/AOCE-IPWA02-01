package com.sheasepherd.ghostnet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "GEISTERNETZ")
public class Geisternetz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Breitengrad darf nicht leer sein.")
    @DecimalMin(value = "-90.0", message = "Breitengrad muss zwischen -90 und 90 liegen.")
    @DecimalMax(value = "90.0", message = "Breitengrad muss zwischen -90 und 90 liegen.")
    @Column(nullable = false)
    private Double latitude;

    @NotNull(message = "Längengrad darf nicht leer sein.")
    @DecimalMin(value = "-180.0", message = "Längengrad muss zwischen -180 und 180 liegen.")
    @DecimalMax(value = "180.0", message = "Längengrad muss zwischen -180 und 180 liegen.")
    @Column(nullable = false)
    private Double longitude;

    @NotNull(message = "Größenangabe ist erforderlich.")
    @Positive(message = "Größe muss einen positiven Wert haben.")
    @Column(nullable = false)
    private Double groesse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NetzStatus status;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "meldende_person_id", nullable = true)
    private Person meldendePerson;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "bergende_person_id", nullable = true)
    private Person bergendePerson;

    public Geisternetz() {}

    // Getter und Setter...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getGroesse() { return groesse; }
    public void setGroesse(Double groesse) { this.groesse = groesse; }

    public NetzStatus getStatus() { return status; }
    public void setStatus(NetzStatus status) { this.status = status; }

    public Person getMeldendePerson() { return meldendePerson; }
    public void setMeldendePerson(Person meldendePerson) { this.meldendePerson = meldendePerson; }

    public Person getBergendePerson() { return bergendePerson; }
    public void setBergendePerson(Person bergendePerson) { this.bergendePerson = bergendePerson; }
}