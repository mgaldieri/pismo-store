# Pismo Store

#### Introdução

Esse aplicativo simula um sistema de vendas uma loja fictícia, com carrinho de compras e pagamento.
Toda a comunicação é feita através de interface REST utilizando dados em JSON e autenticação por header HTTP.

O aplicativo foi desenvolvido em [Kotlin](http://kotlinlang.org) e roda sobre JVM para Java 8, enquanto os testes foram
desenvolvidos em Python 3. A execução do aplicativo pode ser feito de duas maneiras:
* Compilação do código através do Gradle, utilizando o script `runserver.sh`
* Em um container Docker, através do script `rundocker.sh`

Da mesma maneira, para encerrar o aplicativo pode-se utilizar os scripts `stopserver.sh` e `stopdocker.sh`, respectivamente.

Os testes são realizados utilizando a versão Docker e são executados pelo script `runtests.sh`, que instala as dependências
do Python automaticamente em um virtualenv que é removido após o término destes.

A execução deste aplicativo depende do [Pismo Warehouse](https://github.com/mgaldieri/pismo-warehouse) que controla
o estoque de produtos, portanto este deve estar rodando para que as funcionalidades possam ser testadas. Os scripts para
a versão Docker cuida para que o Pismo Warehouse esteja rodando, realizando a conexão entre os containers.

#### Especificações

Todas as respostas do servidor seguem o mesmo padrão:
```
{
    "data": <object, nullable>,
    "error": <object, nullable>
}
```
Sendo que o campo `data` conterá os dados requisitados (nulo quando não houver dados) e o campo `error` a mensagem de erro específica, quando houver
(nulo quando não houver erros)

As mensagens de erro, por sua vez seguem o modelo:
```
{
    "type": <string>,       // Tipo de error (default: "Server error")
    "errorCode": <string>,  // Código interno de erro (default: "0000")
    "httpCode": <int>,      // Status HTTP (padrão RFC7231)
    "message": <string>     // Mensagem descritiva
}
```

A interação com o aplicativo é feita através de dois tipos de enpoints: um público, cujo acesso é livre, e outro que depende da autenticação
prévia de um usuário cadastrado no sistema. O banco de dados já possui um usuário cadastrado para testes com as seguintes credenciais:

- email: `user@email.com`
- senha: `user123`

A disponibilidade do sistema pode ser verificada pelo endpoint:

`[GET] /ping`

Que deve retornar um status code `200` e a mensagem `Alive`

##### Autenticação

Inicialmente o usuário deverá logar no sistema:


`[POST] /user/login`
```
REQUEST BODY:
{
    "email": <string>,
    "password": <string>
}

RESPONSE:
{
    "data": {
        "jwt": <string>
    },
    "error": <string, nullable>
}
```

O campo de retorno `jwt` conterá um token que identifica o usuário no sistema, e deverá ser inserido no header
`Authentication`, com o valor `Bearer <TOKEN>` para todos os acessos subsequentes.

##### Endpoints públicos

`[GET] /products`

Lista todos os produtos cadastrados

```
REQUEST BODY:
N/A

RESPONSE:
{
    "data": {
        "products": [
            {
                "id": <int>,                // Id do produto no sistema
                "name": <string>,           // Nome do produto
                "description": <string>,    // Descrição do produto
                "priceCents": <int>,        // Preço do produto, em centavos
                "qty": <int>                // Quantidade do produto em estoque
            },...
        ]
    },
    "error": <null>
}
```

---

`[GET] /product/<productId: int>`

Lista as informações do produto com o id `productId`

```
REQUEST BODY:
N/A

RESPONSE:
{
    "data": {
        "product": {
            "id": <int>,                // Id do produto no sistema
            "name": <string>,           // Nome do produto
            "description": <string>,    // Descrição do produto
            "priceCents": <int>,        // Preço do produto, em centavos
            "qty": <int>                // Quantidade do produto em estoque
        }
    },
    "error": <null>
}
```

---

##### Endpoints restritos

`[POST] /user/logout`

Realiza a desautenticação do usuário no sistema.

```
REQUEST BODY:
N/A

RESPONSE:
{
    "data": <null>,
    "error": <null>
}
```
---

`[POST] /user/checkout`

Realiza o checkout do carrinho de compras, registrando a venda dos produtos neste.

```
REQUEST BODY:
N/A

RESPONSE:
{
    "data": <null>,
    "error": <null>
}
```

---

`[GET] /user/cart`

Retorna as informações dos produtos no carrinho de compras do usuário bem como o valor total destes.

```
REQUEST BODY:
N/A

RESPONSE:
{
    "data": {
        "cart": {
            "products": {
                "<productId: int>": {
                    "productId": <int>  // Id do produto no sistema
                    "qty": <int>        // Quantidade do produto no carrinho de compras
                }

            },
            "totalPriceCents": <int>    // Valor total do itens no carrinho
        }
    },
    "error": <null>
}
```

---

`[PUT] /user/cart/<productId: int>`

Insere um novo produto com id `productId` no carrinho de compras

```
REQUEST BODY:
{
    "qty": <int>    // Quantidade do produto a ser inserido no carrinho de compras
}

RESPONSE:
{
    "data": {
        "cart": {
            "products": {
                "<productId: int>": {
                    "productId": <int>  // Id do produto no sistema
                    "qty": <int>        // Quantidade do produto no carrinho de compras
                }

            },
            "totalPriceCents": <int>    // Valor total do itens no carrinho
        }
    },
    "error": <null>
}
```

---

`[DELETE] /user/cart/<productId: int>`

Remove um produto com id `productId` do carrinho de compras

```
REQUEST BODY:
N/A

RESPONSE:
{
    "data": {
        "cart": {
            "products": {
                "<productId: int>": {
                    "productId": <int>  // Id do produto no sistema
                    "qty": <int>        // Quantidade do produto no carrinho de compras
                }

            },
            "totalPriceCents": <int>    // Valor total do itens no carrinho
        }
    },
    "error": <null>
}
```

---

`[POST] /user/cart/<productId: int>/increase`

Aumente a quantidade de um produto com id `productId` no carrinho de compras

```
REQUEST BODY:
{
    "qty": <int>    // Quantidade do produto a ser inserida no carrinho de compras
}

RESPONSE:
{
    "data": {
        "cart": {
            "products": {
                "<productId: int>": {
                    "productId": <int>  // Id do produto no sistema
                    "qty": <int>        // Quantidade do produto no carrinho de compras
                }

            },
            "totalPriceCents": <int>    // Valor total do itens no carrinho
        }
    },
    "error": <null>
}
```

---

`[POST] /user/cart/<productId: int>/decrease`

Reduz a quantidade de um produto com id `productId` no carrinho de compras

```
REQUEST BODY:
{
    "qty": <int>    // Quantidade do produto a ser removida do carrinho de compras
}

RESPONSE:
{
    "data": {
        "cart": {
            "products": {
                "<productId: int>": {
                    "productId": <int>  // Id do produto no sistema
                    "qty": <int>        // Quantidade do produto no carrinho de compras
                }

            },
            "totalPriceCents": <int>    // Valor total do itens no carrinho
        }
    },
    "error": <null>
}
```
