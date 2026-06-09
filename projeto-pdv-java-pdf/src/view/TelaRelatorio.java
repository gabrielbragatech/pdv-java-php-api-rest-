package view;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.io.IOException;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import controller.RelatorioDAO;
import model.ItemRelatorio;

// Relatorio de vendas: mostra os produtos vendidos, quantidade, horario
// e o total geral vendido.
public class TelaRelatorio extends JDialog {

    private final RelatorioDAO relatorioDAO = new RelatorioDAO();

    public TelaRelatorio(Frame dono) {
        super(dono, "Relatório de vendas", true);
        setSize(600, 400);
        setLocationRelativeTo(dono);
        setLayout(new BorderLayout());

        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"Venda", "Data / Hora", "Produto", "Qtd", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tabela = new JTable(modelo);
        tabela.setRowHeight(24);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        JLabel rotuloTotal = new JLabel("Total geral: R$ 0,00");
        rotuloTotal.setFont(new Font("SansSerif", Font.BOLD, 16));
        rotuloTotal.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12));
        add(rotuloTotal, BorderLayout.SOUTH);

        // TODO: seria legal poder filtrar por dia depois
        // busca os itens vendidos e vai somando o total geral
        try {
            List<ItemRelatorio> itens = relatorioDAO.listar();
            double totalGeral = 0;
            for (ItemRelatorio it : itens) {
                modelo.addRow(new Object[]{
                        it.getVendaId(),
                        it.getData(),
                        it.getProduto(),
                        it.getQuantidade(),
                        "R$ " + String.format("%.2f", it.getSubtotal())
                });
                totalGeral += it.getSubtotal();
            }
            rotuloTotal.setText("Total geral: R$ " + String.format("%.2f", totalGeral));

            if (itens.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ainda não tem nenhuma venda registrada.");
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao buscar o relatório:\n" + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
