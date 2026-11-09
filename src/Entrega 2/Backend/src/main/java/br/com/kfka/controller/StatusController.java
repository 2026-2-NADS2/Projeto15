package br.com.kfka.controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@io.swagger.v3.oas.annotations.tags.Tag(name="Status")
@RestController
public class StatusController {
    @io.swagger.v3.oas.annotations.security.SecurityRequirements
    @GetMapping("/status") public ResponseEntity<Map<String, String>> status() { return ResponseEntity.ok(Map.of("status", "UP")); }
}
