package model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Venda: junta os itens do carrinho, o total e a data.
public class Venda {

    private int id;
    private List<ItemCarrinho> itens;
    private Date dataVenda;

    public Venda() {
        // ja inicio a lista pra nao dar NullPointerException
        this.itens = new ArrayList<>();
        this.dataVenda = new Date();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<ItemCarrinho> getItens() {
        return itens;
    }

    public void setItens(List<ItemCarrinho> itens) {
        this.itens = itens;
    }

    public Date getDataVenda() {
        return dataVenda;
    }

    public void setDataVenda(Date dataVenda) {
        this.dataVenda = dataVenda;
    }

    // adiciona item; se o produto ja estiver no carrinho, so soma a quantidade
    public void adicionarItem(Produto produto, int quantidade) {
        for (ItemCarrinho item : itens) {
            if (item.getProduto().getId() == produto.getId()) {
                item.setQuantidade(item.getQuantidade() + quantidade);
                return;
            }
        }
        itens.add(new ItemCarrinho(produto, quantidade));
    }

    // total = soma dos subtotais
    public double getTotal() {
        double total = 0.0;
        for (ItemCarrinho item : itens) {
            total += item.getSubtotal();
        }
        return total;
    }

    public void limpar() {
        itens.clear();
        this.dataVenda = new Date();
    }
}
