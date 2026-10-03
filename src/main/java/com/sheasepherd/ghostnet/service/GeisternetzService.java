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
@Transactional
public class GeisternetzService {

    private final GeisternetzRepository geisternetzRepository;
    private final PersonRepository personRepository;

    public GeisternetzService(GeisternetzRepository geisternetzRepository, PersonRepository personRepository) {
        this.geisternetzRepository = geisternetzRepository;
        this.personRepository = personRepository;
    }

    /**
     * Erfasst ein neues Geisternetz. Falls Name angegeben ist, wird die Person verknüpft, sonst anonym (null).
     */
    public Geisternetz erfasseNetz(Geisternetz netz, Person melder) {
        if (melder != null && melder.getName() != null && !melder.getName().isBlank()) {
            netz.setMeldendePerson(verarbeitePerson(melder));
        } else {
            netz.setMeldendePerson(null);
        }
        netz.setStatus(NetzStatus.GEMELDET);
        return geisternetzRepository.save(netz);
    }

    /**
     * Liefert alle noch zu bergenden oder angekündigten Netze für die Übersicht.
     */
    @Transactional(readOnly = true)
    public List<Geisternetz> getOffeneNetze() {
        return geisternetzRepository.findByStatusIn(List.of(NetzStatus.GEMELDET, NetzStatus.BERGUNG_BEVORSTEHEND));
    }

    /**
     * Kündigt eine Bergung an und weist eine bergende Person zu (US-2).
     */
    public void bergungAnkuendigen(Long netzId, Person berger) {
        if (berger == null || berger.getName() == null || berger.getName().isBlank()) {
            throw new IllegalArgumentException("Für eine Bergungsankündigung müssen Kontaktdaten (Name) angegeben werden.");
        }
        Geisternetz netz = geisternetzRepository.findById(netzId)
                .orElseThrow(() -> new IllegalArgumentException("Geisternetz mit ID " + netzId + " nicht gefunden."));

        if (netz.getStatus() != NetzStatus.GEMELDET) {
            throw new IllegalStateException("Nur Netze im Status 'GEMELDET' können zur Bergung übernommen werden.");
        }

        netz.setBergendePerson(verarbeitePerson(berger));
        netz.setStatus(NetzStatus.BERGUNG_BEVORSTEHEND);
        geisternetzRepository.save(netz);
    }

    /**
     * Markiert ein Netz als geborgen (US-4).
     */
    public void alsGeborgenMelden(Long netzId) {
        Geisternetz netz = geisternetzRepository.findById(netzId)
                .orElseThrow(() -> new IllegalArgumentException("Geisternetz mit ID " + netzId + " nicht gefunden."));

        if (netz.getStatus() != NetzStatus.BERGUNG_BEVORSTEHEND) {
            throw new IllegalStateException("Nur Netze im Status 'BERGUNG_BEVORSTEHEND' können als geborgen gemeldet werden.");
        }

        netz.setStatus(NetzStatus.GEBORGEN);
        geisternetzRepository.save(netz);
    }

    /**
     * Markiert ein Netz als verschollen (US-7). Die meldende Person muss zwingend Kontaktdaten angeben.
     */
    public void alsVerschollenMelden(Long netzId, Person hinweisgeber) {
        if (hinweisgeber == null || hinweisgeber.getName() == null || hinweisgeber.getName().isBlank()) {
            throw new IllegalArgumentException("Eine Verschollen-Meldung darf nicht anonym erfolgen. Bitte geben Sie Ihren Namen an.");
        }
        Geisternetz netz = geisternetzRepository.findById(netzId)
                .orElseThrow(() -> new IllegalArgumentException("Geisternetz mit ID " + netzId + " nicht gefunden."));

        if (netz.getStatus() == NetzStatus.GEBORGEN || netz.getStatus() == NetzStatus.VERSCHOLLEN) {
            throw new IllegalStateException("Das Netz ist bereits abgeschlossen (geborgen oder verschollen).");
        }

        // Anonymitätsregel erfüllt: Der Hinweisgeber hat seinen Namen angegeben
        netz.setStatus(NetzStatus.VERSCHOLLEN);
        geisternetzRepository.save(netz);
    }

    /**
     * Hilfsmethode zur Wiederverwendung bestehender Personen-Datensätze (Deduplizierung).
     */
    private Person verarbeitePerson(Person person) {
        if (person == null || person.getName() == null || person.getName().isBlank()) {
            return null;
        }
        return personRepository.findByNameAndTelefonnummer(person.getName(), person.getTelefonnummer())
                .orElseGet(() -> personRepository.save(person));
    }
}