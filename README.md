# WePayU - Sistema de Folha de Pagamento

Projeto desenvolvido para a disciplina de **Programação 2 (P2)** da **Universidade Federal de Alagoas (UFAL)**.

---

## Documentação Completa do Sistema

A documentação técnica detalhada de todas as classes, métodos, arquitetura, padrões de projeto e regras de negócio está disponível no documento oficial em PDF:

📄 **[Documentacao_WePayU.pdf](Documentacao_WePayU.pdf)** *(ou via versão HTML em `documentacao.html`)*

O documento abrange:
1. **Visão Geral do Sistema**: Categorias de empregados (Horistas, Assalariados, Comissionados), métodos de pagamento e sindicato.
2. **Arquitetura e Padrões de Projeto**: Padrão *Façade*, *Memento* com snapshots em memória para Undo/Redo e modelo orientado a objetos.
3. **Mapeamento das User Stories**: Detalhamento das histórias US1 a US8.
4. **Documentação Detalhada de Classes e Métodos**:
   - `Facade.java`: Todos os métodos da fachada documentados com assinaturas, parâmetros, retornos e exceções.
   - `Sistema.java`: Núcleo da lógica de negócio e controle transacional.
   - `FolhaDePagamento.java`: Motor de cálculo financeiro e formatação tabular.
   - Pacote `models`: `Empregado`, `EmpregadoHorista`, `EmpregadoAssalariado`, `EmpregadoComissionado`, `point_card_pay_u`, `ResultadoVenda`, `TaxaServico`.
   - Exceções e validações do sistema.
5. **Ciclo de Vida, Undo/Redo e Persistência**.
6. **Guia de Compilação e Execução dos Testes**.

---

## Como Compilar e Rodar os Testes

### Compilação
No terminal (PowerShell) na raiz do projeto:
```powershell
javac -parameters -cp "lib/easyaccept.jar;src" -d out (Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName })
```

### Execução dos Testes com EasyAccept
```powershell
java -cp "out;lib/easyaccept.jar" Main
```

**Resultado:** 590 testes executados com 100% de sucesso (0 erros, 0 falhas).
