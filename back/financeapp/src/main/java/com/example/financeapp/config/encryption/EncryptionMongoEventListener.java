package com.example.financeapp.config.encryption;

import com.example.financeapp.auth.model.Banker;
import com.example.financeapp.client.model.Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterLoadEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;
import org.springframework.stereotype.Component;

/**
 * Listener de ciclo de vida do MongoDB para criptografia transparente
 * dos campos sensíveis em Client e Banker.
 *
 * CAMPOS CRIPTOGRAFADOS:
 *   Client:  cpf, email, akropoliLinkId
 *   Banker:  email
 *
 * CAMPOS NÃO CRIPTOGRAFADOS (hashes para busca):
 *   Client:  cpfHash, emailHash  ← armazenados em plaintext para indexação
 *   Banker:  emailHash           ← armazenado em plaintext para indexação
 *
 * NOTAS:
 *   - onBeforeSave: cifra os campos no Document Bson antes de gravar.
 *     O objeto Java não é modificado — apenas o documento que vai ao banco.
 *   - onAfterLoad: decifra os campos no Document Bson após carregar.
 *     O objeto Java recebe os valores em plaintext.
 *   - Campos *Hash nunca são criptografados — são hashes HMAC-SHA256
 *     usados como índices para buscas determinísticas.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EncryptionMongoEventListener {

    private final FieldEncryptionService encryptionService;

    // ── Client ────────────────────────────────────────────────────────────────

    @org.springframework.context.event.EventListener
    public void onClientBeforeSave(BeforeSaveEvent<Client> event) {
        var doc = event.getDocument();
        if (doc == null) return;
        Client client = event.getSource();

        encryptField(doc, "email",          client.getEmail());
        encryptField(doc, "cpf",            client.getCpf());
        encryptField(doc, "akropoliLinkId", client.getAkropoliLinkId());
        // emailHash e cpfHash NÃO são criptografados — são índices de busca
    }

    @org.springframework.context.event.EventListener
    public void onClientAfterLoad(AfterLoadEvent<Client> event) {
        var doc = event.getDocument();
        if (doc == null) return;

        decryptField(doc, "email");
        decryptField(doc, "cpf");
        decryptField(doc, "akropoliLinkId");
    }

    // ── Banker ────────────────────────────────────────────────────────────────

    @org.springframework.context.event.EventListener
    public void onBankerBeforeSave(BeforeSaveEvent<Banker> event) {
        var doc = event.getDocument();
        if (doc == null) return;
        Banker banker = event.getSource();

        encryptField(doc, "email", banker.getEmail());
        // emailHash NÃO é criptografado
    }

    @org.springframework.context.event.EventListener
    public void onBankerAfterLoad(AfterLoadEvent<Banker> event) {
        var doc = event.getDocument();
        if (doc == null) return;

        decryptField(doc, "email");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void encryptField(org.bson.Document doc, String key, String value) {
        if (value != null && !value.isBlank()) {
            doc.put(key, encryptionService.encrypt(value));
        }
    }

    private void decryptField(org.bson.Document doc, String key) {
        String raw = doc.getString(key);
        if (raw == null || raw.isBlank()) return;
        try {
            doc.put(key, encryptionService.decrypt(raw));
        } catch (FieldEncryptionService.EncryptionException e) {
            log.warn("Could not decrypt field '{}' for document '{}' — " +
                            "may be legacy plaintext. Keeping original value.",
                    key, doc.get("_id"));
        }
    }
}