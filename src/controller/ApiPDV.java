package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

// ApiPDV: cuida das chamadas HTTP pra API.
// deixei num lugar so pra nao repetir isso em todo DAO.
public class ApiPDV {

    private static final String BASE_URL =
            "http://localhost/projeto-pdv-api/api-pdv.php";

    // GET na API. O "query" eh tipo "?id=3".
    public static String get(String query) throws IOException {
        URL url = new URL(BASE_URL + query);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        return lerResposta(con);
    }

    // POST mandando um corpo em JSON. O "query" diz a rota, tipo "?acao=venda".
    public static String post(String query, String corpoJson) throws IOException {
        URL url = new URL(BASE_URL + query);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        con.setDoOutput(true);

        // escreve o JSON no corpo da requisicao
        try (OutputStream saida = con.getOutputStream()) {
            byte[] dados = corpoJson.getBytes("UTF-8");
            saida.write(dados);
        }

        return lerResposta(con);
    }

    // le a resposta do servidor e devolve como texto.
    // se o status for de erro, leio o stream de erro pra nao estourar.
    private static String lerResposta(HttpURLConnection con) throws IOException {
        int status = con.getResponseCode();
        InputStream stream = (status >= 200 && status < 300)
                ? con.getInputStream()
                : con.getErrorStream();

        if (stream == null) {
            throw new IOException("Sem resposta do servidor (status " + status + ").");
        }

        StringBuilder resposta = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(stream, "UTF-8"))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                resposta.append(linha);
            }
        }
        return resposta.toString();
    }
}
