package controller;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import model.ItemCarrinho;
import model.Venda;

// PdfGenerator: escreve um recibo.pdf de verdade byte a byte, em Java
// puro, sem biblioteca nenhuma.
//
// Um PDF eh basicamente um arquivo de texto com uma estrutura certa:
// um cabecalho, uns "objetos" e, no fim, uma tabela (xref) que diz a
// posicao em bytes de cada objeto. Eu monto isso na mao aqui.
//
// Os objetos:
//   1 -> Catalog (raiz)
//   2 -> Pages
//   3 -> Page (a pagina A4)
//   4 -> Font (Courier, monoespacada pra alinhar as colunas)
//   5 -> Contents (o texto, com os operadores BT...ET)
public class PdfGenerator {

    private static final Locale BR = new Locale("pt", "BR");
    private static final int LARGURA_LINHA = 44; // colunas do cupom

    // recebe a venda e o caminho do arquivo
    public void gerarRecibo(Venda venda, String caminhoArquivo) throws IOException {
        List<String> linhas = montarLinhas(venda);
        byte[] conteudo = montarContentStream(linhas);
        escreverPdf(conteudo, caminhoArquivo);
    }

    // monta as linhas de texto do recibo
    private List<String> montarLinhas(Venda venda) {
        List<String> linhas = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        linhas.add("        LANCHONETE DO GABRIEL");
        linhas.add("          CUPOM NAO FISCAL");
        linhas.add(repetir('-', LARGURA_LINHA));
        linhas.add("Venda No: " + venda.getId());
        linhas.add("Data: " + sdf.format(venda.getDataVenda()));
        linhas.add(repetir('-', LARGURA_LINHA));
        linhas.add(coluna("PRODUTO", "QTD", "SUBTOTAL"));
        linhas.add(repetir('-', LARGURA_LINHA));

        for (ItemCarrinho item : venda.getItens()) {
            String nome = item.getProduto().getNome();
            String qtd = String.valueOf(item.getQuantidade());
            String sub = "R$ " + String.format(BR, "%.2f", item.getSubtotal());
            linhas.add(coluna(nome, qtd, sub));
        }

        linhas.add(repetir('-', LARGURA_LINHA));
        linhas.add(coluna("TOTAL", "", "R$ " + String.format(BR, "%.2f", venda.getTotal())));
        linhas.add(repetir('-', LARGURA_LINHA));
        linhas.add("");
        linhas.add("   Obrigado pela preferencia! Volte sempre.");
        return linhas;
    }

    // transforma as linhas nos operadores de texto do PDF
    private byte[] montarContentStream(List<String> linhas) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("BT\n");          // begin text
        sb.append("/F1 11 Tf\n");   // fonte F1, tamanho 11
        sb.append("16 TL\n");       // espaco entre linhas
        sb.append("40 800 Td\n");   // posicao inicial (x=40, y=800)

        boolean primeira = true;
        for (String linha : linhas) {
            if (!primeira) {
                sb.append("T*\n");  // pula pra proxima linha
            }
            sb.append("(").append(escapar(linha)).append(") Tj\n"); // escreve o texto
            primeira = false;
        }

        sb.append("ET\n");          // end text
        // Windows-1252, que combina com o /Encoding da fonte
        return sb.toString().getBytes("Cp1252");
    }

    // monta o arquivo PDF inteiro com a tabela xref certa
    private void escreverPdf(byte[] conteudo, String caminho) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        long[] offset = new long[6]; // onde cada objeto comeca, em bytes

        // cabecalho
        escrever(baos, "%PDF-1.4\n");
        // comentario com bytes altos, avisa que o arquivo eh binario
        escrever(baos, "%âãÏÓ\n");

        // objeto 1: Catalog
        offset[1] = baos.size();
        escrever(baos, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        // objeto 2: Pages
        offset[2] = baos.size();
        escrever(baos, "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");

        // objeto 3: Page (A4 = 595 x 842)
        offset[3] = baos.size();
        escrever(baos, "3 0 obj\n<< /Type /Page /Parent 2 0 R "
                + "/MediaBox [0 0 595 842] "
                + "/Resources << /Font << /F1 4 0 R >> >> "
                + "/Contents 5 0 R >>\nendobj\n");

        // objeto 4: Font (Courier + WinAnsi pros acentos)
        offset[4] = baos.size();
        escrever(baos, "4 0 obj\n<< /Type /Font /Subtype /Type1 "
                + "/BaseFont /Courier /Encoding /WinAnsiEncoding >>\nendobj\n");

        // objeto 5: Contents (o stream de texto)
        offset[5] = baos.size();
        escrever(baos, "5 0 obj\n<< /Length " + conteudo.length + " >>\nstream\n");
        baos.write(conteudo);
        escrever(baos, "\nendstream\nendobj\n");

        // tabela xref: posicao em bytes de cada objeto.
        // cada linha tem que ter 20 bytes exatos, por isso o padding.
        long inicioXref = baos.size();
        escrever(baos, "xref\n");
        escrever(baos, "0 6\n");
        escrever(baos, "0000000000 65535 f \n"); // objeto 0, sempre livre
        for (int i = 1; i <= 5; i++) {
            escrever(baos, String.format(Locale.US, "%010d 00000 n \n", offset[i]));
        }

        // trailer
        escrever(baos, "trailer\n");
        escrever(baos, "<< /Size 6 /Root 1 0 R >>\n");
        escrever(baos, "startxref\n");
        escrever(baos, inicioXref + "\n");
        escrever(baos, "%%EOF\n");

        // grava no disco
        try (FileOutputStream fos = new FileOutputStream(caminho)) {
            fos.write(baos.toByteArray());
        }
    }

    // escreve a parte estrutural como Latin-1 (1 char = 1 byte, offset certo)
    private void escrever(ByteArrayOutputStream baos, String texto) throws IOException {
        baos.write(texto.getBytes("ISO-8859-1"));
    }

    // escapa os caracteres especiais de string do PDF
    private String escapar(String s) {
        return s.replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }

    // monta a linha em 3 colunas de largura fixa (cabe na fonte monoespacada)
    private String coluna(String c1, String c2, String c3) {
        return padDireita(c1, 26) + padEsquerda(c2, 5) + padEsquerda(c3, 13);
    }

    private String padDireita(String s, int largura) {
        if (s.length() > largura) {
            s = s.substring(0, largura);
        }
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < largura) {
            sb.append(' ');
        }
        return sb.toString();
    }

    private String padEsquerda(String s, int largura) {
        if (s.length() > largura) {
            s = s.substring(0, largura);
        }
        StringBuilder sb = new StringBuilder();
        while (sb.length() < largura - s.length()) {
            sb.append(' ');
        }
        sb.append(s);
        return sb.toString();
    }

    private String repetir(char c, int vezes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vezes; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}
