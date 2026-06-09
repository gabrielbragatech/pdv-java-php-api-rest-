package model;

public class ItemRelatorio {

    private int vendaId;
    private String data;
    private String produto;
    private int quantidade;
    private double subtotal;

    public ItemRelatorio(int vendaId, String data, String produto, int quantidade, double subtotal) {
        this.vendaId = vendaId;
        this.data = data;
        this.produto = produto;
        this.quantidade = quantidade;
        this.subtotal = subtotal;
    }

    public int getVendaId() {
        return vendaId;
    }

    public String getData() {
        return data;
    }

    public String getProduto() {
        return produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public double getSubtotal() {
        return subtotal;
    }
}
