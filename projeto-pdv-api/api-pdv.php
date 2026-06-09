<?php
// API do PDV. Faz a ponte entre o programa Java e o MySQL.
// As rotas vao pelo parametro "acao": produto (um produto pelo id),
// produtos (lista), relatorio (itens vendidos), venda e produto no POST
// (salvar venda e cadastrar produto). Sempre responde JSON.

header("Content-Type: application/json; charset=utf-8");

$host  = "localhost";
$banco = "pdv";
$usuario = "root";
$senha   = "";

// atalho pra devolver JSON e parar
function responder($dados) {
    echo json_encode($dados, JSON_UNESCAPED_UNICODE);
    exit;
}

// conecta no banco
try {
    $pdo = new PDO(
        "mysql:host=$host;dbname=$banco;charset=utf8mb4",
        $usuario,
        $senha,
        [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC
        ]
    );
} catch (PDOException $e) {
    responder(["sucesso" => false, "erro" => "Falha ao conectar no banco."]);
}

$acao = isset($_GET["acao"]) ? $_GET["acao"] : "";

// ----- GET: um produto pelo id -----
if ($acao === "produto" && $_SERVER["REQUEST_METHOD"] === "GET") {
    $id = isset($_GET["id"]) ? (int) $_GET["id"] : 0;

    $stmt = $pdo->prepare("SELECT id, nome, preco, estoque FROM produtos WHERE id = ?");
    $stmt->execute([$id]);
    $produto = $stmt->fetch();

    if (!$produto) {
        responder(["sucesso" => false, "erro" => "Produto não encontrado."]);
    }

    $produto["id"] = (int) $produto["id"];
    $produto["preco"] = (float) $produto["preco"];
    $produto["estoque"] = (int) $produto["estoque"];

    responder(["sucesso" => true, "produto" => $produto]);
}

// ----- GET: lista de produtos -----
if ($acao === "produtos") {
    $lista = $pdo->query("SELECT id, nome, preco, estoque FROM produtos ORDER BY id")->fetchAll();

    foreach ($lista as &$p) {
        $p["id"] = (int) $p["id"];
        $p["preco"] = (float) $p["preco"];
        $p["estoque"] = (int) $p["estoque"];
    }

    responder(["sucesso" => true, "produtos" => $lista]);
}

// ----- GET: relatorio (uma linha por item vendido) -----
if ($acao === "relatorio") {
    $sql = "SELECT v.id AS venda_id,
                   DATE_FORMAT(v.data_venda, '%d/%m/%Y %H:%i:%s') AS data,
                   p.nome AS produto,
                   i.quantidade AS quantidade,
                   (i.quantidade * i.preco_unitario) AS subtotal
              FROM itens_venda i
              JOIN vendas v   ON i.venda_id = v.id
              JOIN produtos p ON i.produto_id = p.id
          ORDER BY v.id DESC, p.nome";
    $itens = $pdo->query($sql)->fetchAll();

    foreach ($itens as &$it) {
        $it["venda_id"] = (int) $it["venda_id"];
        $it["quantidade"] = (int) $it["quantidade"];
        $it["subtotal"] = (float) $it["subtotal"];
    }

    responder(["sucesso" => true, "itens" => $itens]);
}

// ----- POST: cadastrar produto -----
if ($acao === "produto" && $_SERVER["REQUEST_METHOD"] === "POST") {
    $dados = json_decode(file_get_contents("php://input"), true);

    $nome = isset($dados["nome"]) ? trim($dados["nome"]) : "";
    $preco = isset($dados["preco"]) ? (float) $dados["preco"] : 0;
    $estoque = isset($dados["estoque"]) ? (int) $dados["estoque"] : 0;

    if ($nome === "") {
        responder(["sucesso" => false, "erro" => "Nome do produto é obrigatório."]);
    }

    $stmt = $pdo->prepare("INSERT INTO produtos (nome, preco, estoque) VALUES (?, ?, ?)");
    $stmt->execute([$nome, $preco, $estoque]);

    responder(["sucesso" => true, "id" => (int) $pdo->lastInsertId()]);
}

// ----- POST: salvar a venda -----
if ($acao === "venda") {
    $dados = json_decode(file_get_contents("php://input"), true);
    $itens = isset($dados["itens"]) ? $dados["itens"] : [];

    if (count($itens) === 0) {
        responder(["sucesso" => false, "erro" => "Venda sem itens."]);
    }

    try {
        // transacao: ou grava tudo, ou nada
        $pdo->beginTransaction();

        // pega o preco de cada item no banco e ja calcula o total
        $buscaPreco = $pdo->prepare("SELECT preco FROM produtos WHERE id = ?");
        $total = 0;
        foreach ($itens as $i => $item) {
            $buscaPreco->execute([$item["produto_id"]]);
            $linha = $buscaPreco->fetch();
            $preco = $linha ? (float) $linha["preco"] : 0;
            $itens[$i]["preco"] = $preco;
            $total += $preco * $item["quantidade"];
        }

        // grava a venda
        $insereVenda = $pdo->prepare("INSERT INTO vendas (total, data_venda) VALUES (?, NOW())");
        $insereVenda->execute([$total]);
        $vendaId = (int) $pdo->lastInsertId();

        // grava os itens e baixa o estoque
        $insereItem = $pdo->prepare(
            "INSERT INTO itens_venda (venda_id, produto_id, quantidade, preco_unitario)
             VALUES (?, ?, ?, ?)"
        );
        $baixaEstoque = $pdo->prepare("UPDATE produtos SET estoque = estoque - ? WHERE id = ?");
        foreach ($itens as $item) {
            $insereItem->execute([$vendaId, $item["produto_id"], $item["quantidade"], $item["preco"]]);
            $baixaEstoque->execute([$item["quantidade"], $item["produto_id"]]);
        }

        $pdo->commit();
        responder(["sucesso" => true, "venda_id" => $vendaId]);

    } catch (PDOException $e) {
        $pdo->rollBack();
        responder(["sucesso" => false, "erro" => "Erro ao salvar a venda."]);
    }
}

// se nao caiu em nenhuma rota
responder(["sucesso" => false, "erro" => "Requisição inválida."]);
