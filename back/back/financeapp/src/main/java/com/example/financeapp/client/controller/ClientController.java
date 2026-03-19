package com.example.financeapp.client.controller;

import com.example.financeapp.auth.model.Banker;
import com.example.financeapp.client.dto.ClientDto;
import com.example.financeapp.client.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ClientDto.Response>> findAll(
            @AuthenticationPrincipal Banker banker) {
        return ResponseEntity.ok(service.findAll(banker.getId()));
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClientDto.Response> findById(
            @PathVariable String id,
            @AuthenticationPrincipal Banker banker) {
        return ResponseEntity.ok(service.findById(id, banker.getId()));
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ClientDto.Response> create(
            @RequestBody @Valid ClientDto.Request request,
            @AuthenticationPrincipal Banker banker) {
        return ResponseEntity.ok(service.create(request, banker.getId()));
    }

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ClientDto.Response> update(
            @PathVariable String id,
            @RequestBody @Valid ClientDto.UpdateRequest request,
            @AuthenticationPrincipal Banker banker) {
        return ResponseEntity.ok(service.update(id, request, banker.getId()));
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id,
            @AuthenticationPrincipal Banker banker) {
        service.delete(id, banker.getId());
        return ResponseEntity.noContent().build();
    }
}