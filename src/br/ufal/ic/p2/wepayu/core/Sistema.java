package br.ufal.ic.p2.wepayu.core;

import br.ufal.ic.p2.wepayu.exceptions.*;
import br.ufal.ic.p2.wepayu.models.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class Sistema implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, Empregado> empregados = new LinkedHashMap<>();
    private int proximoId = 1;
    private Map<LocalDate, String> folhaGeradaPorData = new HashMap<>();
    private Sindicato sindicato = new Sindicato();
    private Map<String, AgendaPagamento> agendasDisponiveis = new LinkedHashMap<>();

    private transient Stack<byte[]> undoStack = new Stack<>();
    private transient Stack<byte[]> redoStack = new Stack<>();
    private transient boolean sistemaEncerrado = false;

    private static class EstadoSistema implements Serializable {
        private static final long serialVersionUID = 1L;
        private final Map<String, Empregado> empregados;
        private final int proximoId;
        private final Map<LocalDate, String> folhaGeradaPorData;
        private final Map<String, AgendaPagamento> agendasDisponiveis;

        public EstadoSistema(Map<String, Empregado> empregados, int proximoId, Map<LocalDate, String> folhaGeradaPorData, Map<String, AgendaPagamento> agendasDisponiveis) {
            this.empregados = new LinkedHashMap<>(empregados);
            this.proximoId = proximoId;
            this.folhaGeradaPorData = new HashMap<>(folhaGeradaPorData);
            this.agendasDisponiveis = new LinkedHashMap<>(agendasDisponiveis);
        }

        public Map<String, Empregado> getEmpregados() {
            return Collections.unmodifiableMap(empregados);
        }

        public int getProximoId() {
            return proximoId;
        }

        public Map<LocalDate, String> getFolhaGeradaPorData() {
            return Collections.unmodifiableMap(folhaGeradaPorData);
        }

        public Map<String, AgendaPagamento> getAgendasDisponiveis() {
            return Collections.unmodifiableMap(agendasDisponiveis);
        }
    }

    public Sistema() {
        checkTransients();
        inicializarAgendasPadrao();
    }

    private void inicializarAgendasPadrao() {
        agendasDisponiveis.clear();
        try {
            agendasDisponiveis.put("semanal 5", new AgendaSemanal(1, 5));
            agendasDisponiveis.put("semanal 2 5", new AgendaSemanal(2, 5));
            agendasDisponiveis.put("mensal $", new AgendaMensal("$"));
        } catch (Exception ignored) {
        }
    }

    public void checkTransients() {
        if (undoStack == null) undoStack = new Stack<>();
        if (redoStack == null) redoStack = new Stack<>();
        if (sindicato == null) sindicato = new Sindicato();
        if (agendasDisponiveis == null) {
            agendasDisponiveis = new LinkedHashMap<>();
            inicializarAgendasPadrao();
        }
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        undoStack = new Stack<>();
        redoStack = new Stack<>();
        sistemaEncerrado = false;
        if (sindicato == null) sindicato = new Sindicato();
        if (agendasDisponiveis == null || agendasDisponiveis.isEmpty()) {
            agendasDisponiveis = new LinkedHashMap<>();
            inicializarAgendasPadrao();
        }
    }

    private void checarSistemaAberto() throws Exception {
        if (sistemaEncerrado) {
            throw new SistemaEncerradoException();
        }
    }

    private byte[] clonarEstado() {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(new EstadoSistema(empregados, proximoId, folhaGeradaPorData, agendasDisponiveis));
            objectOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception excecao) {
            throw new RuntimeException("Erro ao clonar estado do sistema: " + excecao.getMessage(), excecao);
        }
    }

    private void restaurarEstado(byte[] bytesEstado) {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytesEstado);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            EstadoSistema estado = (EstadoSistema) objectInputStream.readObject();
            this.empregados = new LinkedHashMap<>(estado.getEmpregados());
            this.proximoId = estado.getProximoId();
            this.folhaGeradaPorData = new HashMap<>(estado.getFolhaGeradaPorData());
            this.agendasDisponiveis = new LinkedHashMap<>(estado.getAgendasDisponiveis());
        } catch (Exception excecao) {
            throw new RuntimeException("Erro ao restaurar estado do sistema: " + excecao.getMessage(), excecao);
        }
    }

    @FunctionalInterface
    public interface ComandoSemRetorno {
        void executar() throws Exception;
    }

    @FunctionalInterface
    public interface ComandoComRetorno<T> {
        T executar() throws Exception;
    }

    public synchronized void executarComandoModificador(ComandoSemRetorno comando) throws Exception {
        checarSistemaAberto();
        checkTransients();
        byte[] estadoAnterior = clonarEstado();
        try {
            comando.executar();
            undoStack.push(estadoAnterior);
            redoStack.clear();
        } catch (Exception excecao) {
            restaurarEstado(estadoAnterior);
            throw excecao;
        }
    }

    public synchronized <T> T executarComandoModificadorComRetorno(ComandoComRetorno<T> comando) throws Exception {
        checarSistemaAberto();
        checkTransients();
        byte[] estadoAnterior = clonarEstado();
        try {
            T resultado = comando.executar();
            undoStack.push(estadoAnterior);
            redoStack.clear();
            return resultado;
        } catch (Exception excecao) {
            restaurarEstado(estadoAnterior);
            throw excecao;
        }
    }

    public synchronized void undo() throws Exception {
        checarSistemaAberto();
        checkTransients();
        if (undoStack.isEmpty()) {
            throw new NaoHaComandoADesfazerException();
        }
        byte[] estadoAtual = clonarEstado();
        byte[] estadoAnterior = undoStack.pop();
        redoStack.push(estadoAtual);
        restaurarEstado(estadoAnterior);
    }

    public synchronized void redo() throws Exception {
        checarSistemaAberto();
        checkTransients();
        if (redoStack.isEmpty()) {
            throw new NaoHaComandoARefazerException();
        }
        byte[] estadoAtual = clonarEstado();
        byte[] estadoProximo = redoStack.pop();
        undoStack.push(estadoAtual);
        restaurarEstado(estadoProximo);
    }

    public int getNumeroDeEmpregados() {
        return empregados.size();
    }

    public void encerrarSistema() throws Exception {
        sistemaEncerrado = true;
        salvar(this);
    }

    public void zerarSistema() throws Exception {
        sistemaEncerrado = false;
        executarComandoModificador(() -> {
            empregados.clear();
            proximoId = 1;
            folhaGeradaPorData.clear();
            inicializarAgendasPadrao();
        });
    }

    private Empregado obterEmpregado(String idEmpregado) throws Exception {
        checarSistemaAberto();
        if (idEmpregado == null || idEmpregado.isEmpty()) {
            throw new IdentificacaoEmpregadoNulaException();
        }
        Empregado empregado = empregados.get(idEmpregado);
        if (empregado == null) {
            throw new EmpregadoNaoExisteException();
        }
        return empregado;
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws Exception {
        return executarComandoModificadorComRetorno(() -> {
            double salarioNumerico = ValidadorEmpregado.validarDadosComuns(nome, endereco, tipo, salario);
            String idEmpregado = UUID.randomUUID().toString();
            Empregado novoEmpregado = FabricaEmpregados.criarEmpregado(idEmpregado, nome, endereco, tipo, salarioNumerico);
            empregados.put(idEmpregado, novoEmpregado);
            return idEmpregado;
        });
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao) throws Exception {
        return executarComandoModificadorComRetorno(() -> {
            double salarioNumerico = ValidadorEmpregado.validarDadosComuns(nome, endereco, tipo, salario);
            double comissaoNumerica = ValidadorEmpregado.converterParaDouble(comissao,
                    new ComissaoNaoPodeSerNulaException(),
                    new ComissaoDeveSerNumericaException(),
                    new ComissaoDeveSerNaoNegativaException());

            String idEmpregado = UUID.randomUUID().toString();
            Empregado novoEmpregado = FabricaEmpregados.criarComissionado(idEmpregado, nome, endereco, tipo, salarioNumerico, comissaoNumerica);
            empregados.put(idEmpregado, novoEmpregado);
            return idEmpregado;
        });
    }

    public String getAtributoEmpregado(String idEmpregado, String atributo) throws Exception {
        checarSistemaAberto();
        Empregado empregado = obterEmpregado(idEmpregado);

        if ("nome".equals(atributo)) return empregado.getNome();
        if ("endereco".equals(atributo)) return empregado.getEndereco();
        if ("tipo".equals(atributo)) return empregado.getTipo();
        if ("sindicalizado".equals(atributo)) return String.valueOf(empregado.isSindicalizado());
        if ("idSindicato".equals(atributo)) {
            if (!empregado.isSindicalizado()) throw new EmpregadoNaoEhSindicalizadoException();
            return empregado.getIdSindicato();
        }
        if ("taxaSindical".equals(atributo)) {
            if (!empregado.isSindicalizado()) throw new EmpregadoNaoEhSindicalizadoException();
            return ValidadorEmpregado.formatarMoeda(empregado.getTaxaSindical());
        }
        if ("salario".equals(atributo)) return ValidadorEmpregado.formatarMoeda(empregado.getSalario());
        if ("comissao".equals(atributo)) return ValidadorEmpregado.formatarMoeda(empregado.getComissao());
        if ("metodoPagamento".equals(atributo)) return empregado.getMetodoPagamento().getTipo();
        if ("banco".equals(atributo)) return empregado.getMetodoPagamento().getBanco();
        if ("agencia".equals(atributo)) return empregado.getMetodoPagamento().getAgencia();
        if ("contaCorrente".equals(atributo)) return empregado.getMetodoPagamento().getContaCorrente();
        if ("agendaPagamento".equals(atributo)) return empregado.getAgendaPagamento().getDescricao();

        throw new AtributoNaoExisteException();
    }

    public String getEmpregadoPorNome(String nome, int indice) throws Exception {
        checarSistemaAberto();
        List<String> idsCorrespondentes = new ArrayList<>();
        for (Empregado empregado : empregados.values()) {
            if (empregado.getNome().equals(nome)) {
                idsCorrespondentes.add(empregado.getId());
            }
        }
        if (idsCorrespondentes.isEmpty()) throw new NaoHaEmpregadoComEsseNomeException();
        return idsCorrespondentes.get(indice - 1);
    }

    public void removerEmpregado(String idEmpregado) throws Exception {
        executarComandoModificador(() -> {
            obterEmpregado(idEmpregado);
            empregados.remove(idEmpregado);
        });
    }

    public void lancaCartao(String idEmpregado, String data, String horas) throws Exception {
        executarComandoModificador(() -> {
            Empregado empregado = obterEmpregado(idEmpregado);
            LocalDate dataCartao = ValidadorEmpregado.converterParaData(data, new DataInvalidaException());
            double horasTrabalhadas = ValidadorEmpregado.validarHorasTrabalhadas(horas);

            empregado.adicionarCartao(new CartaoPonto(dataCartao, horasTrabalhadas));
        });
    }

    public String getHorasNormaisTrabalhadas(String idEmpregado, String dataInicial, String dataFinal) throws Exception {
        checarSistemaAberto();
        Empregado empregado = obterEmpregado(idEmpregado);
        LocalDate inicio = ValidadorEmpregado.converterParaData(dataInicial, new DataInicialInvalidaException());
        LocalDate fim = ValidadorEmpregado.converterParaData(dataFinal, new DataFinalInvalidaException());
        ValidadorEmpregado.validarIntervaloDatas(inicio, fim);

        double totalHorasNormais = empregado.getHorasNormaisTrabalhadas(inicio, fim);
        return ValidadorEmpregado.formatarHoras(totalHorasNormais);
    }

    public String getHorasExtrasTrabalhadas(String idEmpregado, String dataInicial, String dataFinal) throws Exception {
        checarSistemaAberto();
        Empregado empregado = obterEmpregado(idEmpregado);
        LocalDate inicio = ValidadorEmpregado.converterParaData(dataInicial, new DataInicialInvalidaException());
        LocalDate fim = ValidadorEmpregado.converterParaData(dataFinal, new DataFinalInvalidaException());
        ValidadorEmpregado.validarIntervaloDatas(inicio, fim);

        double totalHorasExtras = empregado.getHorasExtrasTrabalhadas(inicio, fim);
        return ValidadorEmpregado.formatarHoras(totalHorasExtras);
    }

    public void lancaVenda(String idEmpregado, String data, String valor) throws Exception {
        executarComandoModificador(() -> {
            Empregado empregado = obterEmpregado(idEmpregado);
            LocalDate dataVenda = ValidadorEmpregado.converterParaData(data, new DataInvalidaException());
            double valorVenda = ValidadorEmpregado.validarValorMonetarioPositivo(valor);

            empregado.adicionarVenda(new ResultadoVenda(dataVenda, valorVenda));
        });
    }

    public String getVendasRealizadas(String idEmpregado, String dataInicial, String dataFinal) throws Exception {
        checarSistemaAberto();
        Empregado empregado = obterEmpregado(idEmpregado);
        LocalDate inicio = ValidadorEmpregado.converterParaData(dataInicial, new DataInicialInvalidaException());
        LocalDate fim = ValidadorEmpregado.converterParaData(dataFinal, new DataFinalInvalidaException());
        ValidadorEmpregado.validarIntervaloDatas(inicio, fim);

        double totalVendas = empregado.getVendasRealizadas(inicio, fim);
        return ValidadorEmpregado.formatarMoeda(totalVendas);
    }

    public void alteraEmpregado(String idEmpregado, String atributo, String valor) throws Exception {
        executarComandoModificador(() -> {
            Empregado empregado = obterEmpregado(idEmpregado);

            if ("nome".equals(atributo)) {
                empregado.setNome(valor);
            } else if ("endereco".equals(atributo)) {
                empregado.setEndereco(valor);
            } else if ("tipo".equals(atributo)) {
                if ("comissionado".equals(valor)) {
                    throw new ComissaoNaoPodeSerNulaException();
                }
                Empregado novoEmpregado = FabricaEmpregados.converterTipo(empregado, valor, null);
                empregados.put(empregado.getId(), novoEmpregado);
            } else if ("salario".equals(atributo)) {
                double salarioNumerico = ValidadorEmpregado.converterParaDouble(valor,
                        new SalarioNaoPodeSerNuloException(),
                        new SalarioDeveSerNumericoException(),
                        new SalarioDeveSerNaoNegativoException());
                empregado.setSalario(salarioNumerico);
            } else if ("comissao".equals(atributo)) {
                double comissaoNumerica = ValidadorEmpregado.converterParaDouble(valor,
                        new ComissaoNaoPodeSerNulaException(),
                        new ComissaoDeveSerNumericaException(),
                        new ComissaoDeveSerNaoNegativaException());
                empregado.setComissao(comissaoNumerica);
            } else if ("metodoPagamento".equals(atributo)) {
                if ("correios".equals(valor)) {
                    empregado.setMetodoPagamento(new MetodoPagamentoCorreios());
                } else if ("emMaos".equals(valor)) {
                    empregado.setMetodoPagamento(new MetodoPagamentoEmMaos());
                } else {
                    throw new MetodoPagamentoInvalidoException();
                }
            } else if ("sindicalizado".equals(atributo)) {
                if ("false".equals(valor)) {
                    empregado.desfiliarSindicato();
                } else if ("true".equals(valor)) {
                    throw new IdentificacaoSindicatoNulaException();
                } else {
                    throw new ValorDeveSerTrueOuFalseException();
                }
            } else if ("agendaPagamento".equals(atributo)) {
                AgendaPagamento agenda = agendasDisponiveis.get(valor);
                if (agenda == null) {
                    throw new AgendaNaoDisponivelException();
                }
                empregado.setAgendaPagamento(agenda);
            } else {
                throw new AtributoNaoExisteException();
            }
        });
    }

    public void alteraEmpregado(String idEmpregado, String atributo, String valor, String arg4) throws Exception {
        executarComandoModificador(() -> {
            Empregado empregado = obterEmpregado(idEmpregado);

            if ("tipo".equals(atributo)) {
                double paramNumerico;
                if ("comissionado".equals(valor)) {
                    paramNumerico = ValidadorEmpregado.converterParaDouble(arg4,
                            new ComissaoNaoPodeSerNulaException(),
                            new ComissaoDeveSerNumericaException(),
                            new ComissaoDeveSerNaoNegativaException());
                } else if ("horista".equals(valor)) {
                    paramNumerico = ValidadorEmpregado.converterParaDouble(arg4,
                            new SalarioNaoPodeSerNuloException(),
                            new SalarioDeveSerNumericoException(),
                            new SalarioDeveSerNaoNegativoException());
                } else if ("assalariado".equals(valor)) {
                    paramNumerico = ValidadorEmpregado.converterParaDouble(arg4,
                            new SalarioNaoPodeSerNuloException(),
                            new SalarioDeveSerNumericoException(),
                            new SalarioDeveSerNaoNegativoException());
                } else {
                    throw new TipoInvalidoException();
                }
                Empregado novoEmpregado = FabricaEmpregados.converterTipo(empregado, valor, paramNumerico);
                empregados.put(empregado.getId(), novoEmpregado);
            } else {
                throw new AtributoNaoExisteException();
            }
        });
    }

    public void alteraEmpregado(String idEmpregado, String atributo, String valor, String arg4, String arg5) throws Exception {
        executarComandoModificador(() -> {
            Empregado empregado = obterEmpregado(idEmpregado);

            if ("sindicalizado".equals(atributo)) {
                if ("true".equals(valor)) {
                    if (arg4 == null || arg4.trim().isEmpty()) {
                        throw new IdentificacaoSindicatoNulaException();
                    }
                    sindicato.validarIdDisponivel(empregados.values(), idEmpregado, arg4);
                    double taxaSindical = ValidadorEmpregado.converterParaDouble(arg5,
                            new TaxaSindicalNaoPodeSerNulaException(),
                            new TaxaSindicalDeveSerNumericaException(),
                            new TaxaSindicalDeveSerNaoNegativaException());
                    empregado.filiarSindicato(arg4, taxaSindical);
                } else if ("false".equals(valor)) {
                    empregado.desfiliarSindicato();
                } else {
                    throw new ValorDeveSerTrueOuFalseException();
                }
            } else {
                throw new AtributoNaoExisteException();
            }
        });
    }

    public void alteraEmpregado(String idEmpregado, String atributo, String valor, String banco, String agencia, String contaCorrente) throws Exception {
        executarComandoModificador(() -> {
            Empregado empregado = obterEmpregado(idEmpregado);

            if ("metodoPagamento".equals(atributo)) {
                if ("banco".equals(valor)) {
                    if (banco == null || banco.trim().isEmpty()) {
                        throw new BancoNaoPodeSerNuloException();
                    }
                    if (agencia == null || agencia.trim().isEmpty()) {
                        throw new AgenciaNaoPodeSerNulaException();
                    }
                    if (contaCorrente == null || contaCorrente.trim().isEmpty()) {
                        throw new ContaCorrenteNaoPodeSerNulaException();
                    }
                    empregado.setMetodoPagamento(new MetodoPagamentoBanco(banco, agencia, contaCorrente));
                } else {
                    throw new MetodoPagamentoInvalidoException();
                }
            } else {
                throw new AtributoNaoExisteException();
            }
        });
    }

    public void lancaTaxaServico(String idMembro, String data, String valor) throws Exception {
        executarComandoModificador(() -> {
            if (idMembro == null || idMembro.trim().isEmpty()) {
                throw new IdentificacaoMembroNulaException();
            }

            Empregado membroEncontrado = null;
            for (Empregado emp : empregados.values()) {
                if (emp.isSindicalizado() && idMembro.equals(emp.getIdSindicato())) {
                    membroEncontrado = emp;
                    break;
                }
            }

            if (membroEncontrado == null) {
                throw new MembroNaoExisteException();
            }

            LocalDate dataTaxa = ValidadorEmpregado.converterParaData(data, new DataInvalidaException());
            double valorTaxa = ValidadorEmpregado.validarValorMonetarioPositivo(valor);

            membroEncontrado.adicionarTaxaServico(new TaxaServico(dataTaxa, valorTaxa));
        });
    }

    public String getTaxasServico(String idEmpregado, String dataInicial, String dataFinal) throws Exception {
        checarSistemaAberto();
        Empregado empregado = obterEmpregado(idEmpregado);
        if (!empregado.isSindicalizado()) {
            throw new EmpregadoNaoEhSindicalizadoException();
        }

        LocalDate inicio = ValidadorEmpregado.converterParaData(dataInicial, new DataInicialInvalidaException());
        LocalDate fim = ValidadorEmpregado.converterParaData(dataFinal, new DataFinalInvalidaException());
        ValidadorEmpregado.validarIntervaloDatas(inicio, fim);

        double totalTaxas = empregado.getFiliacaoSindical().getTaxasServicoNoPeriodo(inicio, fim);
        return ValidadorEmpregado.formatarMoeda(totalTaxas);
    }

    public void criarAgendaDePagamentos(String descricao) throws Exception {
        checarSistemaAberto();
        executarComandoModificador(() -> {
            AgendaPagamento novaAgenda = ValidadorAgenda.validarECriar(descricao);
            if (agendasDisponiveis.containsKey(novaAgenda.getDescricao())) {
                throw new AgendaJaExisteException();
            }
            agendasDisponiveis.put(novaAgenda.getDescricao(), novaAgenda);
        });
    }

    public String totalFolha(String dataStr) throws Exception {
        checarSistemaAberto();
        LocalDate data = ValidadorEmpregado.converterParaData(dataStr, new DataInvalidaException());
        FolhaDePagamento.ResultadoFolha resultado = FolhaDePagamento.calcular(empregados, data, false);
        return ValidadorEmpregado.formatarMoeda(resultado.getTotalFolha());
    }

    public void rodaFolha(String dataStr, String caminhoArquivoSaida) throws Exception {
        checarSistemaAberto();
        executarComandoModificador(() -> {
            LocalDate data = ValidadorEmpregado.converterParaData(dataStr, new DataInvalidaException());
            FolhaDePagamento.ResultadoFolha resultado = FolhaDePagamento.calcular(empregados, data, false);
            String relatorio = resultado.getRelatorio();

            try (Writer escritorArquivo = new OutputStreamWriter(new FileOutputStream(caminhoArquivoSaida), "ISO-8859-1")) {
                escritorArquivo.write(relatorio);
            }

            FolhaDePagamento.calcular(empregados, data, true);
            folhaGeradaPorData.put(data, relatorio);
        });
    }

    public static void salvar(Sistema sistema) throws IOException {
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream("sistema.dat"))) {
            objectOutputStream.writeObject(sistema);
        }
    }

    public static Sistema carregar() {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream("sistema.dat"))) {
            Sistema sistema = (Sistema) objectInputStream.readObject();
            sistema.checkTransients();
            return sistema;
        } catch (Exception excecao) {
            return new Sistema();
        }
    }
}
