package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import model.Produto;

// ProdutoDAO: fala com a API pra buscar, listar e cadastrar produto.
public class ProdutoDAO {

    // busca um produto pelo id. devolve o Produto ou null se nao achar.
    public Produto buscarPorId(int id) throws IOException {
        String json = ApiPDV.get("?acao=produto&id=" + id);

        if (!json.contains("\"sucesso\":true")) {
            return null;
        }
        return montar(json);
    }

    // lista todos os produtos cadastrados.
    public List<Produto> listar() throws IOException {
        String json = ApiPDV.get("?acao=produtos");
        List<Produto> produtos = new ArrayList<>();

        // cada {...} dentro do array vira um produto
        for (String obj : JsonUtil.objetos(json)) {
            produtos.add(montar(obj));
        }
        return produtos;
    }

    // cadastra um produto novo. devolve true se deu certo.
    // TODO: um dia fazer editar e excluir tambem
    public boolean cadastrar(Produto p) throws IOException {
        String json = "{"
                + "\"nome\":\"" + p.getNome() + "\","
                + "\"preco\":" + p.getPreco() + ","
                + "\"estoque\":" + p.getEstoque()
                + "}";
        String resposta = ApiPDV.post("?acao=produto", json);
        return resposta.contains("\"sucesso\":true");
    }

    // monta um Produto a partir de um pedaco de JSON
    private Produto montar(String json) {
        int id = Integer.parseInt(JsonUtil.valor(json, "id"));
        String nome = JsonUtil.valor(json, "nome");
        double preco = Double.parseDouble(JsonUtil.valor(json, "preco"));
        int estoque = Integer.parseInt(JsonUtil.valor(json, "estoque"));
        return new Produto(id, nome, preco, estoque);
    }
}
