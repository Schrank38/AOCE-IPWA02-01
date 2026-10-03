package com.sheasepherd.ghostnet.controller;

import com.sheasepherd.ghostnet.model.Geisternetz;
import com.sheasepherd.ghostnet.model.Person;
import com.sheasepherd.ghostnet.service.GeisternetzService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/netze")
public class GeisternetzController {

    private final GeisternetzService service;

    public GeisternetzController(GeisternetzService service) {
        this.service = service;
    }

    /**
     * Übersicht der offenen Geisternetze (Status GEMELDET und BERGUNG_BEVORSTEHEND).
     */
    @GetMapping
    public String listeZeigen(Model model) {
        model.addAttribute("netze", service.getOffeneNetze());
        return "netze-liste";
    }

    /**
     * Stellt das Formular zur Erfassung eines neuen Geisternetzes bereit.
     */
    @GetMapping("/neu")
    public String formularZeigen(Model model) {
        model.addAttribute("netz", new Geisternetz());
        model.addAttribute("person", new Person());
        return "netz-erfassen";
    }

    /**
     * Speichert ein neues Geisternetz (anonym oder mit Personendaten).
     */
    @PostMapping("/speichern")
    public String meldungSpeichern(@Valid @ModelAttribute("netz") Geisternetz netz,
                                   BindingResult result,
                                   @ModelAttribute("person") Person person,
                                   Model model) {
        if (result.hasErrors()) {
            return "netz-erfassen";
        }
        service.erfasseNetz(netz, person);
        return "redirect:/netze";
    }

    /**
     * Trägt eine bergende Person für ein Geisternetz ein (US-2).
     */
    @PostMapping("/bergung/{id}")
    public String bergungUebernehmen(@PathVariable Long id,
                                     @ModelAttribute Person berger,
                                     RedirectAttributes redirect) {
        try {
            service.bergungAnkuendigen(id, berger);
            redirect.addFlashAttribute("erfolg", "Bergung erfolgreich angekündigt.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("fehler", e.getMessage());
        }
        return "redirect:/netze";
    }

    /**
     * Markiert ein Geisternetz als geborgen (US-4).
     */
    @PostMapping("/geborgen/{id}")
    public String geborgenMelden(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            service.alsGeborgenMelden(id);
            redirect.addFlashAttribute("erfolg", "Geisternetz erfolgreich als geborgen markiert.");
        } catch (Exception e) {
            redirect.addFlashAttribute("fehler", e.getMessage());
        }
        return "redirect:/netze";
    }

    /**
     * Markiert ein Geisternetz als verschollen (US-7, erfordert Namensangabe des Hinweisgebers).
     */
    @PostMapping("/verschollen/{id}")
    public String verschollenMelden(@PathVariable Long id,
                                     @ModelAttribute Person hinweisgeber,
                                     RedirectAttributes redirect) {
        try {
            service.alsVerschollenMelden(id, hinweisgeber);
            redirect.addFlashAttribute("erfolg", "Netz erfolgreich als verschollen markiert.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("fehler", e.getMessage());
        }
        return "redirect:/netze";
    }
}