package controller;

import java.io.IOException;
import java.util.List;

import model.ItemCarrinho;
import model.Venda;

// VendaDAO: manda a venda pra API salvar.
// So mando os itens; quem calcula o total eh o PHP (pega os precos no banco).
public class VendaDAO {

    public int salvar(Venda venda) throws IOException {
        // monta o JSON com a lista de itens
        StringBuilder json = new StringBuilder();
        json.append("{\"itens\":[");

        List<ItemCarrinho> itens = venda.getItens();
        for (int i = 0; i < itens.size(); i++) {
            ItemCarrinho item = itens.get(i);
            if (i > 0) {
                json.append(",");
            }
            json.append("{\"produto_id\":").append(item.getProduto().getId())
                .append(",\"quantidade\":").append(item.getQuantidade())
                .append("}");
        }
        json.append("]}");
        // System.out.println(json); // usei isso pra ver o que ia pro servidor

        String resposta = ApiPDV.post("?acao=venda", json.toString());

        if (!resposta.contains("\"sucesso\":true")) {
            String erro = JsonUtil.valor(resposta, "erro");
            throw new IOException(erro.isEmpty() ? "Erro ao salvar a venda." : erro);
        }

        int vendaId = Integer.parseInt(JsonUtil.valor(resposta, "venda_id"));
        venda.setId(vendaId);
        return vendaId;
    }
}
