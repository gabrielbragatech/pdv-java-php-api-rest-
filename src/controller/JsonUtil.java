package controller;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// JsonUtil: uns ajudantes pra ler JSON sem usar biblioteca.
// Fiz na base do regex. Nao eh um parser de verdade, mas pros JSON
// simples da API ja resolve (da pra melhorar depois).
public class JsonUtil {

    // pega o valor de uma chave (serve pra texto ou numero).
    // ex: valor("{\"nome\":\"Cafe\"}", "nome") -> "Cafe"
    public static String valor(String json, String chave) {
        // "chave": com aspas opcionais em volta do valor
        Matcher m = Pattern.compile("\"" + chave + "\"\\s*:\\s*\"?([^\",}]*)\"?").matcher(json);
        if (m.find()) {
            return m.group(1).trim();
        }
        return "";
    }

    // quebra um array de objetos em pedacos {...}.
    // funciona porque nossos objetos sao simples (sem chaves dentro).
    public static List<String> objetos(String json) {
        List<String> lista = new ArrayList<>();
        Matcher m = Pattern.compile("\\{[^{}]*\\}").matcher(json);
        while (m.find()) {
            lista.add(m.group());
        }
        return lista;
    }
}
