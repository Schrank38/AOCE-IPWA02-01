package com.sheasepherd.ghostnet.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "GEISTERNETZ")
public class Geisternetz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Breitengrad darf nicht leer sein.")
    @DecimalMin(value = "-90.0", message = "Der Breitengrad muss mindestens -90 betragen.")
    @DecimalMax(value = "90.0", message = "Der Breitengrad darf höchstens 90 betragen.")
    @Column(nullable = false)
    private Double latitude;

    @NotNull(message = "Längengrad darf nicht leer sein.")
    @DecimalMin(value = "-180.0", message = "Der Längengrad muss mindestens -180 betragen.")
    @DecimalMax(value = "180.0", message = "Der Längengrad darf höchstens 180 betragen.")
    @Column(nullable = false)
    private Double longitude;

    @NotNull(message = "Größenangabe ist erforderlich.")
    @Positive(message = "Die geschätzte Größe muss größer als 0 sein.")
    @Column(nullable = false)
    private Double groesse;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NetzStatus status;

    /*
     * JPA erhöht diese Versionsnummer bei Änderungen.
     * Gleichzeitige Änderungen am selben Netz führen so zu einem Konflikt
     * statt zu einem unbemerkten Überschreiben.
     */
    @Version
    private Long version;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "meldende_person_id", nullable = true)
    private Person meldendePerson;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "bergende_person_id", nullable = true)
    private Person bergendePerson;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "verschollen_melder_id", nullable = true)
    private Person verschollenMelder;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getGroesse() {
        return groesse;
    }

    public void setGroesse(Double groesse) {
        this.groesse = groesse;
    }

    public NetzStatus getStatus() {
        return status;
    }

    public void setStatus(NetzStatus status) {
        this.status = status;
    }

    public Long getVersion() {
        return version;
    }

    public Person getMeldendePerson() {
        return meldendePerson;
    }

    public void setMeldendePerson(Person meldendePerson) {
        this.meldendePerson = meldendePerson;
    }

    public Person getBergendePerson() {
        return bergendePerson;
    }

    public void setBergendePerson(Person bergendePerson) {
        this.bergendePerson = bergendePerson;
    }

    public Person getVerschollenMelder() {
        return verschollenMelder;
    }

    public void setVerschollenMelder(Person verschollenMelder) {
        this.verschollenMelder = verschollenMelder;
    }
}