# Financial Control API

API REST de controle financeiro pessoal. Gerencia receitas, despesas (avulsas, fixas e parceladas), contas bancárias e
métodos de pagamento, e tem um dashboard com resumos mensais. A autenticação é por JWT, e cada usuário acessa apenas os
próprios dados.

## 🛠️ Stack

![Java](https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL_17-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-CC0200?style=for-the-badge&logo=flyway&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)
![JUnit 5](https://img.shields.io/badge/JUnit_5-25A162?style=for-the-badge&logo=junit5&logoColor=white)
![Testcontainers](https://img.shields.io/badge/Testcontainers-291A3F?style=for-the-badge&logo=testcontainers&logoColor=white)

## 🚀 Como rodar

📋 Pré-requisitos: **Java 21** e **Docker**.

```bash
cp .env.example .env          # Atualize o JWT_SECRET (openssl rand -hex 32)
docker compose up -d
```

O Spring não lê o `.env` automaticamente. Exporte as variáveis antes de iniciar a aplicação:

```bash
# Linux / macOS / Git Bash
set -a && source .env && set +a
./mvnw spring-boot:run
```

```powershell
# Windows (PowerShell)
Get-Content .env | Where-Object { $_ -match '^\s*[^#].*=' } | ForEach-Object { $k, $v = $_ -split '=', 2; Set-Item "env:$($k.Trim())" $v.Trim() }
.\mvnw.cmd spring-boot:run
```

A API sobe em `http://localhost:8080` por padrão. As migrações do Flyway rodam na inicialização.

## 📚 Documentação da API

Com a aplicação rodando, todos os endpoints, schemas e exemplos ficam disponíveis em:

- 🟢 [**Swagger UI**](http://localhost:8080/docs): Documentação amplamente utilizada e conhecida
- ✨ [**Scalar**](http://localhost:8080/docs/scalar): Documentação moderna para uma leitura mais limpa da referência

🔐 Todas as rotas, exceto as de `/auth`, exigem autenticação. O fluxo é este:

1. Crie uma conta em `POST /auth/sign-up` e faça login em `POST /auth/sign-in`.
2. Copie o `accessToken` da resposta e informe-o como Bearer token. No Swagger, use o botão **Authorize**.
3. Quando o access token expirar, chame `POST /auth/refresh` com o `refreshToken` para obter um novo par de tokens.

## 🧪 Testes

A suíte cobre services (unitários), repositories, controllers, jobs e fluxos E2E: autenticação, parcelamento, geração
mensal, dashboard e isolamento entre usuários.

Os testes E2E e dos repositories usam um PostgreSQL real via Testcontainers, então o **Docker precisa estar rodando**.
🐳 Não é preciso configurar variáveis de ambiente.

```bash
./mvnw test
```

## 🧠 Decisões técnicas

- 🔄 **Refresh token rotativo:**
    - O refresh token é um valor aleatório de 256 bits. O banco guarda apenas o hash SHA-256 dele.
    - Cada refresh revoga o token usado e emite um novo dentro da mesma sessão.
    - Se um token já revogado for reutilizado, o que indica um possível vazamento, **a sessão inteira é revogada**.
- 💳 **Parcelas automáticas:**
    - Ao criar uma despesa parcelada, as parcelas são geradas com `BigDecimal` truncado em 2 casas.
    - A **última parcela absorve o arredondamento**, então a soma das parcelas é sempre igual ao total (1000 / 3 =
      333,33 + 333,33 + 333,34).
    - As parcelas só podem ser regeneradas se nenhuma tiver sido paga.
- 📅 **Jobs mensais com catch-up:**
    - Os jobs rodam no dia 1 de cada mês (fuso `America/Sao_Paulo`) e também na inicialização da aplicação. Assim, um
      mês perdido com a aplicação fora do ar é gerado quando ela volta.
    - Os jobs são idempotentes: há verificação no service e uma constraint `UNIQUE` por recorrência e mês no banco.
- 🛡️ **Isolamento por usuário:**
    - As queries são sempre filtradas pelo `userId` do token, e os recursos referenciados (categoria, método de
      pagamento, conta) são validados como do mesmo usuário.
    - Um recurso de outro usuário retorna `404`, para não revelar que ele existe.
- 🔢 **Whitelist de ordenação:** cada endpoint paginado declara os campos aceitos em `sort`, e um teste garante que
  nenhum
  endpoint fique sem whitelist.
