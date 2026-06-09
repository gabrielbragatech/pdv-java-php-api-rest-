package view;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.io.IOException;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import controller.ProdutoDAO;
import model.Produto;

// Tela que lista todos os produtos cadastrados.
public class TelaProdutos extends JDialog {

    private final ProdutoDAO produtoDAO = new ProdutoDAO();

    public TelaProdutos(Frame dono) {
        super(dono, "Produtos cadastrados", true);
        setSize(480, 360);
        setLocationRelativeTo(dono);
        setLayout(new BorderLayout());

        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"ID", "Nome", "Preço", "Estoque"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tabela = new JTable(modelo);
        tabela.setRowHeight(24);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        // busca os produtos na API e joga na tabela
        try {
            List<Produto> produtos = produtoDAO.listar();
            for (Produto p : produtos) {
                modelo.addRow(new Object[]{
                        p.getId(),
                        p.getNome(),
                        "R$ " + String.format("%.2f", p.getPreco()),
                        p.getEstoque()
                });
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao buscar os produtos:\n" + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
