package com.estacionnexo.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.core.env.Environment;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {
    private final Environment entorno;

    public AuthController(Environment entorno) {
        this.entorno = entorno;
    }

    @GetMapping("/")
    String inicio() {
        return "redirect:/login";
    }

    @GetMapping("/contacto")
    String contacto(Model model) {
        model.addAttribute("direccion", entorno.getProperty("nexo.contacto.direccion", ""));
        model.addAttribute("telefono", entorno.getProperty("nexo.contacto.telefono", ""));
        model.addAttribute("correo", entorno.getProperty("nexo.contacto.correo", ""));
        model.addAttribute("horario", entorno.getProperty("nexo.contacto.horario", ""));
        return "publico/contacto";
    }

    @GetMapping("/login")
    String login() {
        return "publico/login";
    }
}
