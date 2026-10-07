package com.sheasepherd.ghostnet.service;

import com.sheasepherd.ghostnet.model.Geisternetz;
import com.sheasepherd.ghostnet.model.NetzStatus;
import com.sheasepherd.ghostnet.model.Person;
import com.sheasepherd.ghostnet.repository.GeisternetzRepository;
import com.sheasepherd.ghostnet.repository.PersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GeisternetzService {

    private final GeisternetzRepository geisternetzRepository;
    private final PersonRepository personRepository;

    public GeisternetzService(GeisternetzRepository geisternetzRepository, PersonRepository personRepository) {
        this.geisternetzRepository = geisternetzRepository;
        this.personRepository = personRepository;
    }

    @Transactional(readOnly = true)
    public List<Geisternetz> getOffeneNetze() {
        return geisternetzRepository.findByStatusIn(List.of(NetzStatus.GEMELDET, NetzStatus.BERGUNG_BEVORSTEHEND));
    }

    // Die neue Methode, die Strings entgegennimmt, Kommas in Punkte umwandelt und abspeichert
    @Transactional
    public void erfasseNetzAlsString(String latitudeStr, String longitudeStr, String groesseStr, Person person) {
        try {
            Double lat = Double.valueOf(latitudeStr.trim().replace(',', '.'));
            Double lon = Double.valueOf(longitudeStr.trim().replace(',', '.'));
            Double groesse = Double.valueOf(groesseStr.trim().replace(',', '.'));

            Geisternetz netz = new Geisternetz();
            netz.setLatitude(lat);
            netz.setLongitude(lon);
            netz.setGroesse(groesse);
            netz.setStatus(NetzStatus.GEMELDET);

            // Anonymitäts-Check für die meldende Person
            if (person != null && person.getName() != null && !person.getName().isBlank()) {
                if (person.getTelefonnummer() == null || person.getTelefonnummer().isBlank()) {
                    throw new IllegalArgumentException("Bei Angabe eines Namens ist die Telefonnummer zwingend erforderlich.");
                }
                Person persistiertePerson = personRepository.findByNameAndTelefonnummer(person.getName(), person.getTelefonnummer())
                        .orElse(person);
                netz.setMeldendePerson(persistiertePerson);
            } else {
                netz.setMeldendePerson(null);
            }

            geisternetzRepository.save(netz);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Bitte geben Sie gültige Zahlen für Koordinaten und Größe ein.");
        }
    }

    @Transactional
    public void bergungAnkuendigen(Long netzId, Person berger) {
        if (berger == null || berger.getName() == null || berger.getName().isBlank() || 
            berger.getTelefonnummer() == null || berger.getTelefonnummer().isBlank()) {
            throw new IllegalArgumentException("Für eine Bergung müssen Name und Telefonnummer zwingend angegeben werden.");
        }
        Geisternetz netz = geisternetzRepository.findById(netzId)
                .orElseThrow(() -> new IllegalArgumentException("Geisternetz mit ID " + netzId + " nicht gefunden."));
        if (netz.getStatus() != NetzStatus.GEMELDET) {
            throw new IllegalStateException("Nur Netze im Status 'GEMELDET' können übernommen werden.");
        }
        if (netz.getBergendePerson() != null) {
            throw new IllegalStateException("Dieses Netz ist bereits einer bergenden Person zugeordnet.");
        }
        Person persistierterBerger = personRepository.findByNameAndTelefonnummer(berger.getName(), berger.getTelefonnummer())
                .orElse(berger);
        netz.setBergendePerson(persistierterBerger);
        netz.setStatus(NetzStatus.BERGUNG_BEVORSTEHEND);
        geisternetzRepository.save(netz);
    }

    @Transactional
    public void alsGeborgenMelden(Long netzId, Person berger) {
        Geisternetz netz = geisternetzRepository.findById(netzId)
                .orElseThrow(() -> new IllegalArgumentException("Geisternetz mit ID " + netzId + " nicht gefunden."));
        if (netz.getStatus() != NetzStatus.BERGUNG_BEVORSTEHEND) {
            throw new IllegalStateException("Nur Netze im Status 'BERGUNG_BEVORSTEHEND' können als geborgen gemeldet werden.");
        }
        netz.setStatus(NetzStatus.GEBORGEN);
        geisternetzRepository.save(netz);
    }

    @Transactional
    public void alsVerschollenMelden(Long netzId, Person hinweisgeber) {
        if (hinweisgeber == null || hinweisgeber.getName() == null || hinweisgeber.getName().isBlank() ||
            hinweisgeber.getTelefonnummer() == null || hinweisgeber.getTelefonnummer().isBlank()) {
            throw new IllegalArgumentException("Eine Verschollen-Meldung darf nicht anonym erfolgen. Bitte geben Sie Ihren Namen und eine Telefonnummer an.");
        }
        Geisternetz netz = geisternetzRepository.findById(netzId)
                .orElseThrow(() -> new IllegalArgumentException("Geisternetz mit ID " + netzId + " nicht gefunden."));
        if (netz.getStatus() == NetzStatus.GEBORGEN || netz.getStatus() == NetzStatus.VERSCHOLLEN) {
            throw new IllegalStateException("Das Netz ist bereits abgeschlossen (geborgen oder verschollen).");
        }
        Person persistierterMelder = personRepository.findByNameAndTelefonnummer(hinweisgeber.getName(), hinweisgeber.getTelefonnummer())
                .orElse(hinweisgeber);
        netz.setVerschollenMelder(persistierterMelder);
        netz.setStatus(NetzStatus.VERSCHOLLEN);
        geisternetzRepository.save(netz);
    }
}