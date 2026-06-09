package view;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import controller.ProdutoDAO;
import model.Produto;

// Tela pra cadastrar um produto novo.
public class TelaCadastroProduto extends JDialog {

    private final ProdutoDAO produtoDAO = new ProdutoDAO();

    private final JTextField campoNome = new JTextField();
    private final JTextField campoPreco = new JTextField();
    private final JTextField campoEstoque = new JTextField();

    public TelaCadastroProduto(Frame dono) {
        super(dono, "Cadastrar produto", true); // true = modal
        setSize(360, 200);
        setLocationRelativeTo(dono);
        setLayout(new BorderLayout(10, 10));

        // formulario com os campos
        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 5, 15));
        form.add(new JLabel("Nome:"));
        form.add(campoNome);
        form.add(new JLabel("Preço:"));
        form.add(campoPreco);
        form.add(new JLabel("Estoque:"));
        form.add(campoEstoque);
        add(form, BorderLayout.CENTER);

        JButton btnSalvar = new JButton("Salvar");
        JPanel rodape = new JPanel();
        rodape.add(btnSalvar);
        add(rodape, BorderLayout.SOUTH);

        btnSalvar.addActionListener(e -> salvar());
    }

    // TODO: avisar quando ja existe um produto com o mesmo nome
    private void salvar() {
        String nome = campoNome.getText().trim();
        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Digite o nome do produto.",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double preco;
        int estoque;
        try {
            // aceito virgula ou ponto no preco
            preco = Double.parseDouble(campoPreco.getText().trim().replace(",", "."));
            estoque = Integer.parseInt(campoEstoque.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Preço e estoque precisam ser números.",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Produto p = new Produto(0, nome, preco, estoque);
            if (produtoDAO.cadastrar(p)) {
                JOptionPane.showMessageDialog(this, "Produto cadastrado!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Não deu pra cadastrar o produto.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao falar com o servidor:\n" + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
