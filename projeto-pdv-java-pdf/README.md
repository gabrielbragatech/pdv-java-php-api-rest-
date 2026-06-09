# PDV em Java com recibo em PDF

Esse foi o que eu mais curti fazer. É um caixa de lanchonete em Java (tela Swing): digito o ID do produto, ele entra no carrinho, e quando finalizo a venda o sistema salva tudo e gera um `recibo.pdf`.

A parte diferente é a arquitetura: em vez do Java conectar direto no MySQL, ele **fala com uma API em PHP** e a API que mexe no banco. O caminho fica assim:

```
App Java (desktop)  ->  HTTP / JSON  ->  api-pdv.php  ->  MySQL
```

Fiz assim porque separa as coisas: o Java não precisa saber a senha do banco nem carregar driver nenhum, ele só pede e manda dados pela internet, igual um site faz.

## O que ele faz

- Janela de caixa feita em Swing
- Digito o ID do produto, ele busca **na API** e joga no carrinho (uma `JTable`)
- Se eu adicionar o mesmo produto de novo, ele só soma a quantidade
- O total vai recalculando sozinho
- No "Finalizar Venda": manda a venda pra API (que grava e baixa o estoque) e gera o `recibo.pdf`
- No menu de cima dá pra **cadastrar produto**, **ver a lista de produtos** e abrir o **relatório de vendas** (com os produtos vendidos, quantidade, horário e o total geral)

## Como tá organizado

O lado Java segue o MVC:

```
src/
├── model/        -> os dados
│   ├── Produto.java
│   ├── ItemCarrinho.java
│   ├── Venda.java
│   └── ItemRelatorio.java   (uma linha do relatorio)
├── controller/   -> regra e comunicacao com a API
│   ├── ApiPDV.java        (faz as chamadas HTTP pro PHP)
│   ├── JsonUtil.java      (le o JSON da resposta sem biblioteca)
│   ├── ProdutoDAO.java    (busca, lista e cadastra produto)
│   ├── VendaDAO.java      (manda a venda pra API salvar)
│   ├── RelatorioDAO.java  (pega os itens vendidos)
│   └── PdfGenerator.java  (escreve o PDF na mao)
└── view/         -> as telas
    ├── TelaPDV.java               (o caixa + o menu + o main)
    ├── TelaCadastroProduto.java   (cadastrar produto)
    ├── TelaProdutos.java          (lista de produtos)
    └── TelaRelatorio.java         (relatorio de vendas)
```

E o backend fica numa pasta separada, que vai pro servidor:

```
projeto-pdv-api/
└── api-pdv.php   -> recebe os pedidos do Java e conversa com o MySQL
```

A API roteia pelo parametro `acao`: `?acao=produto&id=N`, `?acao=produtos`,
`?acao=relatorio` (GET) e `?acao=venda`, `?acao=produto` (POST). No banco, alem de
`produtos` e `vendas`, tem a `itens_venda`, que guarda o que foi vendido em cada venda
(eh ela que faz o relatorio detalhado funcionar).

## Como rodar

### 1. Banco
Ligue o MySQL no XAMPP e importe o `banco.sql` pelo phpMyAdmin. Cria o banco `pdv` com as tabelas `produtos`, `vendas` e `itens_venda`, mais uns produtos de teste (IDs de 1 a 5).

> Se você já tinha o banco antigo (sem a `itens_venda`), reimporte o `banco.sql` ou rode só o `CREATE TABLE itens_venda ...` que está nele, senão o relatório não funciona.

### 2. A API PHP
1. Ligue também o **Apache** no XAMPP.
2. Copie a pasta `projeto-pdv-api` pra dentro de `C:\xampp\htdocs`.
3. Testa no navegador: `http://localhost/projeto-pdv-api/api-pdv.php?id=1`. Tem que aparecer o JSON do produto 1. Se apareceu, a API tá no ar.

### 3. O programa Java
Agora **não precisa mais** do driver do MySQL (o `.jar`), porque quem fala com o banco é o PHP. Então é só compilar e rodar.

Pelo terminal (precisa de um JDK instalado, eu usei o 21):

```powershell
javac -encoding UTF-8 -d out src/model/*.java src/controller/*.java src/view/*.java
java -cp out view.TelaPDV
```

Ou pelo NetBeans: cria um projeto Java, joga as pastas `model`, `controller` e `view` no `src` e dá play na `TelaPDV`.

Com a janela aberta, digite um ID (1 a 5), **Adicionar**, depois **Finalizar Venda**. O `recibo.pdf` aparece na pasta de onde rodou.

## Desafios Técnicos e Soluções

### Fazer o Java conversar com a API por HTTP

Esse foi o ponto novo. Antes o Java ia direto no banco; agora ele tem que mandar e receber dados por HTTP, igual um navegador. Usei a classe `HttpURLConnection`, que já vem no Java (sem instalar nada). Pra buscar um produto eu faço um GET, e pra salvar a venda eu faço um POST mandando o corpo em JSON.

A parte chata foi o JSON sem biblioteca. Pra **mandar**, eu monto o texto do JSON na mão com um `StringBuilder`. Pra **ler** a resposta, como eu não quis usar lib, eu pego os valores com regex (tipo `"venda_id":(\d+)`). Quando a resposta é uma lista (produtos e relatório), eu quebro o array em pedaços `{...}` e leio cada um. Pra não repetir esse código joguei tudo num `JsonUtil`. Não é o jeito mais bonito, mas pra um JSON simples funciona e me fez entender o que uma biblioteca faria por baixo.

### Onde fica a transação agora

Como quem grava é o PHP, a transação foi pra lá. No `api-pdv.php`, salvar a venda e baixar o estoque acontecem dentro de um `beginTransaction` / `commit`. Se der erro no meio, eu chamo `rollBack` e nada fica gravado pela metade. A regra de "tudo ou nada" continua, só que do lado do servidor.

### Separar o Java do banco (MVC + API)

Gostei de como isso deixou o Java mais limpo: ele não conhece o banco, só conhece a API. A `TelaPDV` fala com os DAO, e os DAO falam com a `ApiPDV`. Se um dia eu trocar o MySQL por outro banco, só mexo no PHP, o Java nem fica sabendo. Foi aí que eu saquei pra que serve separar as camadas.

### Gerar o PDF na mão

Essa parte continua igual e é a que eu mais me orgulho. Em vez de usar uma lib (tipo iText), eu escrevo o `recibo.pdf` byte a byte. Um PDF é um arquivo de texto com uma estrutura certinha: cabeçalho, uns "objetos" (catálogo, página, fonte, conteúdo) e no fim uma tabela `xref` que diz a posição em bytes de cada objeto. O truque foi montar tudo num `ByteArrayOutputStream` e anotar a posição (`baos.size()`) antes de cada objeto, pra os números da xref ficarem exatos. Se errar 1 byte, o PDF abre corrompido.
