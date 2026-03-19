import { describe, expect, it } from "vitest";
import { normalizeAuthResponse } from "./api";

describe("normalizeAuthResponse", () => {
    it("normaliza o contrato flat retornado pelo backend atual", () => {
        const session = normalizeAuthResponse({
            token: "token-123",
            refreshToken: "refresh-123",
            id: "banker-1",
            name: "Maria",
            email: "maria@teste.com",
            role: "BANKER",
        });

        expect(session).toEqual({
            token: "token-123",
            refreshToken: "refresh-123",
            banker: {
                id: "banker-1",
                name: "Maria",
                email: "maria@teste.com",
                role: "BANKER",
            },
        });
    });

    it("mantém compatibilidade com o contrato legado com banker aninhado", () => {
        const session = normalizeAuthResponse({
            token: "token-123",
            banker: {
                id: "banker-1",
                name: "Maria",
                email: "maria@teste.com",
            },
        });

        expect(session.banker.name).toBe("Maria");
        expect(session.token).toBe("token-123");
    });
});