package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import controller.PdfGenerator;
import controller.ProdutoDAO;
import controller.VendaDAO;
import model.ItemCarrinho;
import model.Produto;
import model.Venda;

// TelaPDV: a tela do caixa (Swing).
// Tem a tabela do carrinho, o campo de ID, o botao Adicionar e o
// botao Finalizar Venda. A tela nao sabe SQL, ela fala com os DAO.
public class TelaPDV extends JFrame {

    // o carrinho atual
    private final Venda venda = new Venda();

    // acesso a API e ao PDF
    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final VendaDAO vendaDAO = new VendaDAO();
    private final PdfGenerator pdfGenerator = new PdfGenerator();

    // componentes da tela
    private final DefaultTableModel modeloTabela;
    private final JTextField campoIdProduto = new JTextField(8);
    private final JLabel rotuloTotal = new JLabel("TOTAL: R$ 0,00");

    public TelaPDV() {
        super("PDV - Ponto de Venda");

        // config da janela
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(620, 460);
        // setResizable(false); // cheguei a travar o tamanho mas preferi deixar livre
        setLocationRelativeTo(null); // centraliza
        setLayout(new BorderLayout(10, 10));

        // menu de cima (cadastro e consultas)
        setJMenuBar(criarMenu());

        // topo: campo de ID + botao Adicionar
        JPanel painelTopo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelTopo.add(new JLabel("ID do produto:"));
        painelTopo.add(campoIdProduto);

        JButton btnAdicionar = new JButton("Adicionar");
        painelTopo.add(btnAdicionar);
        add(painelTopo, BorderLayout.NORTH);

        // centro: tabela do carrinho
        modeloTabela = new DefaultTableModel(
                new Object[]{"ID", "Produto", "Preço", "Qtd", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // tabela so de leitura
            }
        };
        JTable tabela = new JTable(modeloTabela);
        tabela.setRowHeight(24);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        // rodape: total + botao Finalizar
        JPanel painelRodape = new JPanel(new BorderLayout());
        rotuloTotal.setFont(new Font("SansSerif", Font.BOLD, 18));
        rotuloTotal.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12));
        painelRodape.add(rotuloTotal, BorderLayout.WEST);

        JButton btnFinalizar = new JButton("Finalizar Venda");
        btnFinalizar.setPreferredSize(new Dimension(160, 40));
        painelRodape.add(btnFinalizar, BorderLayout.EAST);
        add(painelRodape, BorderLayout.SOUTH);

        // acoes dos botoes
        btnAdicionar.addActionListener(e -> adicionarProduto());
        // Enter no campo tambem adiciona, fica mais rapido
        campoIdProduto.addActionListener(e -> adicionarProduto());
        btnFinalizar.addActionListener(e -> finalizarVenda());
    }

    // monta o menu de cima
    private JMenuBar criarMenu() {
        JMenuBar barra = new JMenuBar();

        JMenu menuCadastro = new JMenu("Cadastro");
        JMenuItem itemCadProduto = new JMenuItem("Cadastrar produto");
        itemCadProduto.addActionListener(e -> new TelaCadastroProduto(this).setVisible(true));
        menuCadastro.add(itemCadProduto);

        JMenu menuConsultar = new JMenu("Consultar");
        JMenuItem itemProdutos = new JMenuItem("Produtos");
        itemProdutos.addActionListener(e -> new TelaProdutos(this).setVisible(true));
        JMenuItem itemRelatorio = new JMenuItem("Relatório de vendas");
        itemRelatorio.addActionListener(e -> new TelaRelatorio(this).setVisible(true));
        menuConsultar.add(itemProdutos);
        menuConsultar.add(itemRelatorio);

        barra.add(menuCadastro);
        barra.add(menuConsultar);
        return barra;
    }

    // le o ID, busca o produto e poe no carrinho
    private void adicionarProduto() {
        String texto = campoIdProduto.getText().trim();
        if (texto.isEmpty()) {
            return;
        }

        int id;
        try {
            id = Integer.parseInt(texto);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Digite um ID numérico válido.",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Produto produto = produtoDAO.buscarPorId(id);
            if (produto == null) {
                JOptionPane.showMessageDialog(this,
                        "Produto com ID " + id + " não encontrado.",
                        "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // adiciona 1 unidade (se ja existe, soma a quantidade)
            venda.adicionarItem(produto, 1);
            atualizarTabela();
            campoIdProduto.setText("");
            campoIdProduto.requestFocus();

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao falar com o servidor:\n" + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // redesenha a tabela a partir do carrinho
    private void atualizarTabela() {
        modeloTabela.setRowCount(0); // limpa
        for (ItemCarrinho item : venda.getItens()) {
            Produto p = item.getProduto();
            modeloTabela.addRow(new Object[]{
                    p.getId(),
                    p.getNome(),
                    "R$ " + String.format("%.2f", p.getPreco()),
                    item.getQuantidade(),
                    "R$ " + String.format("%.2f", item.getSubtotal())
            });
        }
        rotuloTotal.setText("TOTAL: R$ " + String.format("%.2f", venda.getTotal()));
    }

    // finaliza: grava no banco, gera o recibo.pdf e limpa o carrinho
    private void finalizarVenda() {
        if (venda.getItens().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "O carrinho está vazio.",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // manda a venda pra API salvar (ela cuida do banco e do estoque)
            int idVenda = vendaDAO.salvar(venda);

            // gera o recibo.pdf na raiz da pasta
            String caminhoPdf = new File("recibo.pdf").getAbsolutePath();
            pdfGenerator.gerarRecibo(venda, caminhoPdf);

            JOptionPane.showMessageDialog(this,
                    "Venda #" + idVenda + " finalizada!\n"
                    + "Total: R$ " + String.format("%.2f", venda.getTotal()) + "\n\n"
                    + "Recibo gerado em:\n" + caminhoPdf,
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            // zera o carrinho pra proxima venda
            venda.limpar();
            atualizarTabela();

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Não foi possível finalizar a venda:\n" + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // deixa a tela com a aparencia do sistema
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignorada) {
            // se falhar, usa o tema padrao do Swing mesmo
        }

        // Swing tem que subir na thread de eventos (EDT)
        SwingUtilities.invokeLater(() -> new TelaPDV().setVisible(true));
    }
}
