package model;

// Produto: espelha a tabela "produtos" do banco.
// Atributos privados (encapsulamento) e acesso pelos getters/setters.
public class Produto {

    private int id;
    private String nome;
    private double preco;
    private int estoque;

    // construtor vazio, quando vou preencher campo a campo
    public Produto() {
    }

    // construtor cheio, usado pelo DAO ao ler do banco
    public Produto(int id, String nome, double preco, int estoque) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.estoque = estoque;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public int getEstoque() {
        return estoque;
    }

    public void setEstoque(int estoque) {
        this.estoque = estoque;
    }
}
