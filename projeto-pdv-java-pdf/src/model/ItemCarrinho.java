package model;

// ItemCarrinho: uma linha do carrinho (produto + quantidade).
// O subtotal eu calculo na hora, nao guardo, pra nao ficar errado.
public class ItemCarrinho {

    private Produto produto;
    private int quantidade;

    public ItemCarrinho(Produto produto, int quantidade) {
        this.produto = produto;
        this.quantidade = quantidade;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    // preco * quantidade
    public double getSubtotal() {
        return produto.getPreco() * quantidade;
    }
}
