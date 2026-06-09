package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import model.ItemRelatorio;

// RelatorioDAO: pega na API a lista de itens vendidos (pro relatorio).
public class RelatorioDAO {

    public List<ItemRelatorio> listar() throws IOException {
        String json = ApiPDV.get("?acao=relatorio");
        List<ItemRelatorio> itens = new ArrayList<>();

        // a API manda uma lista plana, um {...} por item vendido
        for (String obj : JsonUtil.objetos(json)) {
            int vendaId = Integer.parseInt(JsonUtil.valor(obj, "venda_id"));
            String data = JsonUtil.valor(obj, "data");
            String produto = JsonUtil.valor(obj, "produto");
            int quantidade = Integer.parseInt(JsonUtil.valor(obj, "quantidade"));
            double subtotal = Double.parseDouble(JsonUtil.valor(obj, "subtotal"));
            itens.add(new ItemRelatorio(vendaId, data, produto, quantidade, subtotal));
        }
        return itens;
    }
}
