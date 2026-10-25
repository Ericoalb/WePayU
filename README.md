# WePayU - Sistema de Folha de Pagamento

Projeto desenvolvido para a disciplina de **Programação 2 (P2)** da **Universidade Federal de Alagoas (UFAL)**.

---

## Documentação Técnica do Sistema

A documentação técnica completa da arquitetura, classes, métodos e regras de negócio está disponível no documento oficial em PDF:

📄 **[Documentacao_WePayU.pdf](Documentacao_WePayU.pdf)** *(versão HTML em `documentacao.html`)*

### Destaques da Arquitetura Refatorada:
1. **Pacotes Especializados**:
   - `br.ufal.ic.p2.wepayu`: Camada de Fachada (`Facade.java`).
   - `br.ufal.ic.p2.wepayu.core`: Inteligência do sistema (`Sistema.java` e `FolhaDePagamento.java`).
   - `br.ufal.ic.p2.wepayu.models`: Entidades ricas de domínio (`Empregado`, `EmpregadoHorista`, `EmpregadoAssalariado`, `EmpregadoComissionado`, `MetodoPagamento`, `FiliacaoSindical`, `CartaoPonto`, etc.).
   - `br.ufal.ic.p2.wepayu.exceptions`: Pacote com 45 classes de exceções específicas para cada caso de erro do sistema (zero exceções genéricas).
2. **Orientação a Objetos e Polimorfismo Dinâmico**:
   - Todo o comportamento de cálculo e regras de domínio é despachado via polimorfismo e métodos dinâmicos.
3. **Modelos Ricos e Encapsulados**:
   - Classes dedicadas para método de pagamento (`MetodoPagamentoEmMaos`, `MetodoPagamentoCorreios`, `MetodoPagamentoBanco`).
   - Classe dedicada para filiação sindical (`FiliacaoSindical`).
   - Salário e comissão armazenados como `double`.
   - Getters de coleções protegidos com listas imutáveis (`Collections.unmodifiableList`).
4. **Classe `CartaoPonto`**:
   - Substituição total da classe provisória anterior, mantendo consistência com o padrão de nomenclatura do projeto.
5. **Transacionalidade e Memento**:
   - Histórico completo de `undo()` e `redo()` com snapshots em memória e rollback automático diante de falhas.

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
