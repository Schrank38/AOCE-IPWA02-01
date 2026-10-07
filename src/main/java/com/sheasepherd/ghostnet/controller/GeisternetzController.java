package com.sheasepherd.ghostnet.controller;

import com.sheasepherd.ghostnet.model.Geisternetz;
import com.sheasepherd.ghostnet.model.Person;
import com.sheasepherd.ghostnet.service.GeisternetzService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/netze")
public class GeisternetzController {

    private final GeisternetzService service;

    public GeisternetzController(GeisternetzService service) {
        this.service = service;
    }

    @GetMapping
    public String listeZeigen(Model model) {
        model.addAttribute("netze", service.getOffeneNetze());
        return "netze-liste";
    }

    @GetMapping("/neu")
    public String formularZeigen(Model model) {
        if (!model.containsAttribute("person")) {
            model.addAttribute("person", new Person());
        }
        return "netz-erfassen";
    }

    @PostMapping("/speichern")
    public String meldungSpeichern(@RequestParam("latitude") String latitudeStr,
                                   @RequestParam("longitude") String longitudeStr,
                                   @RequestParam("groesse") String groesseStr,
                                   @ModelAttribute("person") Person person,
                                   RedirectAttributes redirect) {
        try {
            service.erfasseNetzAlsString(latitudeStr, longitudeStr, groesseStr, person);
            redirect.addFlashAttribute("erfolg", "Geisternetz erfolgreich erfasst.");
            return "redirect:/netze";
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("fehler", e.getMessage());
            redirect.addFlashAttribute("person", person);
            return "redirect:/netze/neu";
        }
    }

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

    @PostMapping("/geborgen/{id}")
    public String geborgenMelden(@PathVariable Long id,
                                 @ModelAttribute Person berger,
                                 RedirectAttributes redirect) {
        try {
            service.alsGeborgenMelden(id, berger);
            redirect.addFlashAttribute("erfolg", "Geisternetz erfolgreich als geborgen markiert.");
        } catch (Exception e) {
            redirect.addFlashAttribute("fehler", e.getMessage());
        }
        return "redirect:/netze";
    }

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