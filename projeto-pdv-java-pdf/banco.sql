-- Banco do PDV (MySQL do XAMPP).
-- Pra usar: phpMyAdmin > Importar > escolhe esse arquivo > Executar.

CREATE DATABASE IF NOT EXISTS pdv
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE pdv;

-- produtos. preco em DECIMAL (nunca usar FLOAT pra dinheiro).
CREATE TABLE IF NOT EXISTS produtos (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    nome    VARCHAR(120)   NOT NULL,
    preco   DECIMAL(10,2)  NOT NULL,
    estoque INT            NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- cada venda finalizada vira uma linha aqui.
CREATE TABLE IF NOT EXISTS vendas (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    total      DECIMAL(10,2)  NOT NULL,
    data_venda DATETIME       NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- itens de cada venda (o que foi vendido em cada uma).
-- guardo o preco unitario aqui pra saber por quanto foi vendido na epoca.
CREATE TABLE IF NOT EXISTS itens_venda (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    venda_id       INT NOT NULL,
    produto_id     INT NOT NULL,
    quantidade     INT NOT NULL,
    preco_unitario DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_item_venda
        FOREIGN KEY (venda_id) REFERENCES vendas (id) ON DELETE CASCADE,
    CONSTRAINT fk_item_produto
        FOREIGN KEY (produto_id) REFERENCES produtos (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- produtos de teste pra ja conseguir vender algo
INSERT INTO produtos (nome, preco, estoque) VALUES
    ('Cafe Expresso',        6.50,  100),
    ('Pao de Queijo',        4.00,  50),
    ('Suco de Laranja 300ml', 8.90, 30),
    ('Agua Mineral 500ml',   3.50,  80),
    ('Bolo de Chocolate',    7.25,  20);
