package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.models.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class Sistema implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, Empregado> empregados = new LinkedHashMap<>();
    private int proximoId = 1;
    private Map<LocalDate, String> folhaGeradaPorData = new HashMap<>();

    private transient Stack<byte[]> undoStack = new Stack<>();
    private transient Stack<byte[]> redoStack = new Stack<>();
    private transient boolean sistemaEncerrado = false;

    private static class EstadoSistema implements Serializable {
        private static final long serialVersionUID = 1L;
        Map<String, Empregado> empregados;
        int proximoId;
        Map<LocalDate, String> folhaGeradaPorData;

        EstadoSistema(Map<String, Empregado> empregados, int proximoId, Map<LocalDate, String> folhaGeradaPorData) {
            this.empregados = empregados;
            this.proximoId = proximoId;
            this.folhaGeradaPorData = folhaGeradaPorData;
        }
    }

    public Sistema() {
        checkTransients();
    }

    private void checkTransients() {
        if (undoStack == null) undoStack = new Stack<>();
        if (redoStack == null) redoStack = new Stack<>();
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        undoStack = new Stack<>();
        redoStack = new Stack<>();
        sistemaEncerrado = false;
    }

    private byte[] clonarEstado() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(new EstadoSistema(empregados, proximoId, folhaGeradaPorData));
            oos.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao clonar estado do sistema: " + e.getMessage(), e);
        }
    }

    private void restaurarEstado(byte[] bytes) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            EstadoSistema estado = (EstadoSistema) ois.readObject();
            this.empregados = estado.empregados;
            this.proximoId = estado.proximoId;
            this.folhaGeradaPorData = estado.folhaGeradaPorData;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao restaurar estado do sistema: " + e.getMessage(), e);
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

    public synchronized void executarComandoModificador(ComandoSemRetorno cmd) throws Exception {
        if (sistemaEncerrado) {
            throw new Exception("Nao pode dar comandos depois de encerrarSistema.");
        }
        checkTransients();
        byte[] estadoAnterior = clonarEstado();
        try {
            cmd.executar();
            undoStack.push(estadoAnterior);
            redoStack.clear();
        } catch (Exception e) {
            restaurarEstado(estadoAnterior);
            throw e;
        }
    }

    public synchronized <T> T executarComandoModificadorComRetorno(ComandoComRetorno<T> cmd) throws Exception {
        if (sistemaEncerrado) {
            throw new Exception("Nao pode dar comandos depois de encerrarSistema.");
        }
        checkTransients();
        byte[] estadoAnterior = clonarEstado();
        try {
            T resultado = cmd.executar();
            undoStack.push(estadoAnterior);
            redoStack.clear();
            return resultado;
        } catch (Exception e) {
            restaurarEstado(estadoAnterior);
            throw e;
        }
    }

    public synchronized void undo() throws Exception {
        if (sistemaEncerrado) {
            throw new Exception("Nao pode dar comandos depois de encerrarSistema.");
        }
        checkTransients();
        if (undoStack.isEmpty()) {
            throw new Exception("Nao ha comando a desfazer.");
        }
        byte[] estadoAtual = clonarEstado();
        byte[] estadoAnterior = undoStack.pop();
        redoStack.push(estadoAtual);
        restaurarEstado(estadoAnterior);
    }

    public synchronized void redo() throws Exception {
        if (sistemaEncerrado) {
            throw new Exception("Nao pode dar comandos depois de encerrarSistema.");
        }
        checkTransients();
        if (redoStack.isEmpty()) {
            throw new Exception("Nao ha comando a refazer.");
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
        executarComandoModificador(() -> {
            empregados.clear();
            proximoId = 1;
            folhaGeradaPorData.clear();
        });
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws Exception {
        return executarComandoModificadorComRetorno(() -> {
            validarComum(nome, endereco, tipo, salario);
            
            String id = UUID.randomUUID().toString();
            Empregado e;
            if (tipo.equals("horista")) {
                e = new EmpregadoHorista(id, nome, endereco, salario);
            } else if (tipo.equals("assalariado")) {
                e = new EmpregadoAssalariado(id, nome, endereco, "assalariado", salario);
            } else if (tipo.equals("comissionado")) {
                 throw new Exception("Tipo nao aplicavel.");
            } else {
                throw new Exception("Tipo invalido.");
            }
            
            empregados.put(id, e);
            return id;
        });
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao) throws Exception {
        return executarComandoModificadorComRetorno(() -> {
            validarComum(nome, endereco, tipo, salario);
            if (comissao == null || comissao.isEmpty()) throw new Exception("Comissao nao pode ser nula.");
            
            double c = parseDouble(comissao, "Comissao deve ser numerica.");
            if (c < 0) throw new Exception("Comissao deve ser nao-negativa.");

            if (!tipo.equals("comissionado")) {
                throw new Exception("Tipo nao aplicavel.");
            }

            String id = UUID.randomUUID().toString();
            Empregado e = new EmpregadoComissionado(id, nome, endereco, salario, comissao);
            empregados.put(id, e);
            return id;
        });
    }

    private void validarComum(String nome, String endereco, String tipo, String salario) throws Exception {
        if (nome == null || nome.isEmpty()) throw new Exception("Nome nao pode ser nulo.");
        if (endereco == null || endereco.isEmpty()) throw new Exception("Endereco nao pode ser nulo.");
        if (salario == null || salario.isEmpty()) throw new Exception("Salario nao pode ser nulo.");
        
        double s = parseDouble(salario, "Salario deve ser numerico.");
        if (s < 0) throw new Exception("Salario deve ser nao-negativo.");
    }

    private double parseDouble(String valor, String mensagemErro) throws Exception {
        try {
            return Double.parseDouble(valor.replace(",", "."));
        } catch (NumberFormatException e) {
            throw new Exception(mensagemErro);
        }
    }

    public String getAtributoEmpregado(String id, String atributo) throws Exception {
        if (id == null || id.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        Empregado e = empregados.get(id);
        if (e == null) throw new Exception("Empregado nao existe.");

        if (atributo.equals("nome")) return e.getNome();
        if (atributo.equals("endereco")) return e.getEndereco();
        if (atributo.equals("tipo")) return e.getTipo();
        if (atributo.equals("sindicalizado")) return String.valueOf(e.isSindicalizado());
        if (atributo.equals("idSindicato")) {
            if (!e.isSindicalizado()) throw new Exception("Empregado nao eh sindicalizado.");
            return e.getIdSindicato();
        }
        if (atributo.equals("taxaSindical")) {
            if (!e.isSindicalizado()) throw new Exception("Empregado nao eh sindicalizado.");
            return formatarMoeda(e.getTaxaSindical());
        }
        
        if (atributo.equals("salario")) {
            if (e instanceof EmpregadoHorista) return formatarMoeda(((EmpregadoHorista) e).getSalarioHorario());
            if (e instanceof EmpregadoAssalariado) return formatarMoeda(((EmpregadoAssalariado) e).getSalarioMensal());
        }
        
        if (atributo.equals("comissao")) {
            if (!(e instanceof EmpregadoComissionado)) throw new Exception("Empregado nao eh comissionado.");
            return formatarMoeda(((EmpregadoComissionado) e).getComissao());
        }

        if (atributo.equals("metodoPagamento")) return e.getMetodoPagamento();
        if (atributo.equals("banco")) {
            if (!"banco".equals(e.getMetodoPagamento())) throw new Exception("Empregado nao recebe em banco.");
            return e.getBanco();
        }
        if (atributo.equals("agencia")) {
            if (!"banco".equals(e.getMetodoPagamento())) throw new Exception("Empregado nao recebe em banco.");
            return e.getAgencia();
        }
        if (atributo.equals("contaCorrente")) {
            if (!"banco".equals(e.getMetodoPagamento())) throw new Exception("Empregado nao recebe em banco.");
            return e.getContaCorrente();
        }

        throw new Exception("Atributo nao existe.");
    }

    private String formatarMoeda(String valor) {
        try {
            double v = Double.parseDouble(valor.replace(",", "."));
            return String.format(Locale.GERMAN, "%.2f", v);
        } catch (Exception e) {
            return valor;
        }
    }

    public String getEmpregadoPorNome(String nome, int indice) throws Exception {
        List<String> ids = new ArrayList<>();
        for (Empregado e : empregados.values()) {
            if (e.getNome().equals(nome)) {
                ids.add(e.getId());
            }
        }
        if (ids.isEmpty()) throw new Exception("Nao ha empregado com esse nome.");
        return ids.get(indice - 1);
    }

    public void removerEmpregado(String id) throws Exception {
        executarComandoModificador(() -> {
            if (id == null || id.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
            if (!empregados.containsKey(id)) throw new Exception("Empregado nao existe.");
            empregados.remove(id);
        });
    }

    private LocalDate parseData(String dataStr, String mensagemErro) throws Exception {
        if (dataStr == null || dataStr.trim().isEmpty()) {
            throw new Exception(mensagemErro);
        }
        String[] partes = dataStr.split("/");
        if (partes.length != 3) {
            throw new Exception(mensagemErro);
        }
        try {
            int dia = Integer.parseInt(partes[0]);
            int mes = Integer.parseInt(partes[1]);
            int ano = Integer.parseInt(partes[2]);
            return LocalDate.of(ano, mes, dia);
        } catch (Exception e) {
            throw new Exception(mensagemErro);
        }
    }

    private String formatarHoras(double horas) {
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.##", new java.text.DecimalFormatSymbols(new Locale("pt", "BR")));
        return df.format(horas);
    }

    public void lancaCartao(String emp, String data, String horas) throws Exception {
        executarComandoModificador(() -> {
            if (emp == null || emp.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
            Empregado e = empregados.get(emp);
            if (e == null) throw new Exception("Empregado nao existe.");
            if (!(e instanceof EmpregadoHorista)) throw new Exception("Empregado nao eh horista.");

            LocalDate dataCartao = parseData(data, "Data invalida.");
            double h;
            try {
                h = Double.parseDouble(horas.replace(",", "."));
            } catch (Exception ex) {
                throw new Exception("Horas devem ser numericas.");
            }
            if (h <= 0) throw new Exception("Horas devem ser positivas.");

            point_card_pay_u cartao = new point_card_pay_u(dataCartao, h);
            ((EmpregadoHorista) e).adicionarCartao(cartao);
        });
    }

    public String getHorasNormaisTrabalhadas(String emp, String dataInicial, String dataFinal) throws Exception {
        if (emp == null || emp.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        Empregado e = empregados.get(emp);
        if (e == null) throw new Exception("Empregado nao existe.");
        if (!(e instanceof EmpregadoHorista)) throw new Exception("Empregado nao eh horista.");

        LocalDate dInicio = parseData(dataInicial, "Data inicial invalida.");
        LocalDate dFim = parseData(dataFinal, "Data final invalida.");
        if (dInicio.isAfter(dFim)) throw new Exception("Data inicial nao pode ser posterior aa data final.");

        EmpregadoHorista horista = (EmpregadoHorista) e;
        double total = 0.0;
        for (point_card_pay_u c : horista.getCartoes()) {
            if (!c.getData().isBefore(dInicio) && c.getData().isBefore(dFim)) {
                total += c.getHorasNormais();
            }
        }
        return formatarHoras(total);
    }

    public String getHorasExtrasTrabalhadas(String emp, String dataInicial, String dataFinal) throws Exception {
        if (emp == null || emp.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        Empregado e = empregados.get(emp);
        if (e == null) throw new Exception("Empregado nao existe.");
        if (!(e instanceof EmpregadoHorista)) throw new Exception("Empregado nao eh horista.");

        LocalDate dInicio = parseData(dataInicial, "Data inicial invalida.");
        LocalDate dFim = parseData(dataFinal, "Data final invalida.");
        if (dInicio.isAfter(dFim)) throw new Exception("Data inicial nao pode ser posterior aa data final.");

        EmpregadoHorista horista = (EmpregadoHorista) e;
        double total = 0.0;
        for (point_card_pay_u c : horista.getCartoes()) {
            if (!c.getData().isBefore(dInicio) && c.getData().isBefore(dFim)) {
                total += c.getHorasExtras();
            }
        }
        return formatarHoras(total);
    }

    private String formatarMoeda(double valor) {
        return String.format(Locale.GERMAN, "%.2f", valor);
    }

    public void lancaVenda(String emp, String data, String valor) throws Exception {
        executarComandoModificador(() -> {
            if (emp == null || emp.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
            Empregado e = empregados.get(emp);
            if (e == null) throw new Exception("Empregado nao existe.");
            if (!(e instanceof EmpregadoComissionado)) throw new Exception("Empregado nao eh comissionado.");

            LocalDate dataVenda = parseData(data, "Data invalida.");
            double v;
            try {
                v = Double.parseDouble(valor.replace(",", "."));
            } catch (Exception ex) {
                throw new Exception("Valor deve ser numerico.");
            }
            if (v <= 0) throw new Exception("Valor deve ser positivo.");

            ResultadoVenda venda = new ResultadoVenda(dataVenda, v);
            ((EmpregadoComissionado) e).adicionarVenda(venda);
        });
    }

    public String getVendasRealizadas(String emp, String dataInicial, String dataFinal) throws Exception {
        if (emp == null || emp.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        Empregado e = empregados.get(emp);
        if (e == null) throw new Exception("Empregado nao existe.");
        if (!(e instanceof EmpregadoComissionado)) throw new Exception("Empregado nao eh comissionado.");

        LocalDate dInicio = parseData(dataInicial, "Data inicial invalida.");
        LocalDate dFim = parseData(dataFinal, "Data final invalida.");
        if (dInicio.isAfter(dFim)) throw new Exception("Data inicial nao pode ser posterior aa data final.");

        EmpregadoComissionado comissionado = (EmpregadoComissionado) e;
        double total = 0.0;
        for (ResultadoVenda v : comissionado.getVendas()) {
            if (!v.getData().isBefore(dInicio) && v.getData().isBefore(dFim)) {
                total += v.getValor();
            }
        }
        return formatarMoeda(total);
    }

    private void copiarAtributosComuns(Empregado origem, Empregado destino) {
        destino.setMetodoPagamento(origem.getMetodoPagamento());
        destino.setBanco(origem.getBanco());
        destino.setAgencia(origem.getAgencia());
        destino.setContaCorrente(origem.getContaCorrente());
        destino.setSindicalizado(origem.isSindicalizado());
        destino.setIdSindicato(origem.getIdSindicato());
        destino.setTaxaSindical(origem.getTaxaSindical());
        destino.setDebitoSindicato(origem.getDebitoSindicato());
        for (TaxaServico ts : origem.getTaxasServico()) {
            destino.adicionarTaxaServico(ts);
        }
        if (origem instanceof EmpregadoHorista && destino instanceof EmpregadoHorista) {
            for (point_card_pay_u c : ((EmpregadoHorista) origem).getCartoes()) {
                ((EmpregadoHorista) destino).adicionarCartao(c);
            }
        }
        if (origem instanceof EmpregadoComissionado && destino instanceof EmpregadoComissionado) {
            for (ResultadoVenda v : ((EmpregadoComissionado) origem).getVendas()) {
                ((EmpregadoComissionado) destino).adicionarVenda(v);
            }
        }
    }

    public void alteraEmpregado(String emp, String atributo, String valor) throws Exception {
        executarComandoModificador(() -> {
            if (emp == null || emp.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
            Empregado e = empregados.get(emp);
            if (e == null) throw new Exception("Empregado nao existe.");

            if (atributo.equals("nome")) {
                if (valor == null || valor.isEmpty()) throw new Exception("Nome nao pode ser nulo.");
                e.setNome(valor);
            } else if (atributo.equals("endereco")) {
                if (valor == null || valor.isEmpty()) throw new Exception("Endereco nao pode ser nulo.");
                e.setEndereco(valor);
            } else if (atributo.equals("tipo")) {
                if (valor.equals("assalariado")) {
                    String sal = (e instanceof EmpregadoHorista) ? ((EmpregadoHorista) e).getSalarioHorario() : (e instanceof EmpregadoAssalariado ? ((EmpregadoAssalariado) e).getSalarioMensal() : "0");
                    EmpregadoAssalariado novo = new EmpregadoAssalariado(e.getId(), e.getNome(), e.getEndereco(), "assalariado", sal);
                    copiarAtributosComuns(e, novo);
                    empregados.put(e.getId(), novo);
                } else if (valor.equals("horista")) {
                    String sal = (e instanceof EmpregadoAssalariado) ? ((EmpregadoAssalariado) e).getSalarioMensal() : "0";
                    EmpregadoHorista novo = new EmpregadoHorista(e.getId(), e.getNome(), e.getEndereco(), sal);
                    copiarAtributosComuns(e, novo);
                    empregados.put(e.getId(), novo);
                } else if (valor.equals("comissionado")) {
                    throw new Exception("Comissao nao pode ser nula.");
                } else {
                    throw new Exception("Tipo invalido.");
                }
            } else if (atributo.equals("salario")) {
                if (valor == null || valor.isEmpty()) throw new Exception("Salario nao pode ser nulo.");
                double s = parseDouble(valor, "Salario deve ser numerico.");
                if (s < 0) throw new Exception("Salario deve ser nao-negativo.");
                if (e instanceof EmpregadoHorista) {
                    ((EmpregadoHorista) e).setSalarioHorario(valor);
                } else if (e instanceof EmpregadoAssalariado) {
                    ((EmpregadoAssalariado) e).setSalarioMensal(valor);
                }
            } else if (atributo.equals("comissao")) {
                if (!(e instanceof EmpregadoComissionado)) throw new Exception("Empregado nao eh comissionado.");
                if (valor == null || valor.isEmpty()) throw new Exception("Comissao nao pode ser nula.");
                double c = parseDouble(valor, "Comissao deve ser numerica.");
                if (c < 0) throw new Exception("Comissao deve ser nao-negativa.");
                ((EmpregadoComissionado) e).setComissao(valor);
            } else if (atributo.equals("metodoPagamento")) {
                if (valor.equals("correios") || valor.equals("emMaos")) {
                    e.setMetodoPagamento(valor);
                    e.setBanco(null);
                    e.setAgencia(null);
                    e.setContaCorrente(null);
                } else {
                    throw new Exception("Metodo de pagamento invalido.");
                }
            } else if (atributo.equals("sindicalizado")) {
                if (valor.equals("false")) {
                    e.setSindicalizado(false);
                    e.setIdSindicato(null);
                    e.setTaxaSindical(0.0);
                } else if (valor.equals("true")) {
                    throw new Exception("Identificacao do sindicato nao pode ser nula.");
                } else {
                    throw new Exception("Valor deve ser true ou false.");
                }
            } else {
                throw new Exception("Atributo nao existe.");
            }
        });
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String arg4) throws Exception {
        executarComandoModificador(() -> {
            if (emp == null || emp.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
            Empregado e = empregados.get(emp);
            if (e == null) throw new Exception("Empregado nao existe.");

            if (atributo.equals("tipo")) {
                if (valor.equals("comissionado")) {
                    String comissao = arg4;
                    if (comissao == null || comissao.isEmpty()) throw new Exception("Comissao nao pode ser nula.");
                    double c = parseDouble(comissao, "Comissao deve ser numerica.");
                    if (c < 0) throw new Exception("Comissao deve ser nao-negativa.");
                    String sal = (e instanceof EmpregadoHorista) ? ((EmpregadoHorista) e).getSalarioHorario() : (e instanceof EmpregadoAssalariado ? ((EmpregadoAssalariado) e).getSalarioMensal() : "0");
                    EmpregadoComissionado novo = new EmpregadoComissionado(e.getId(), e.getNome(), e.getEndereco(), sal, comissao);
                    copiarAtributosComuns(e, novo);
                    empregados.put(e.getId(), novo);
                } else if (valor.equals("horista")) {
                    String salario = arg4;
                    if (salario == null || salario.isEmpty()) throw new Exception("Salario nao pode ser nulo.");
                    double s = parseDouble(salario, "Salario deve ser numerico.");
                    if (s < 0) throw new Exception("Salario deve ser nao-negativo.");
                    EmpregadoHorista novo = new EmpregadoHorista(e.getId(), e.getNome(), e.getEndereco(), salario);
                    copiarAtributosComuns(e, novo);
                    empregados.put(e.getId(), novo);
                } else {
                    throw new Exception("Tipo invalido.");
                }
            } else {
                throw new Exception("Atributo nao existe.");
            }
        });
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String idSindicato, String taxaSindical) throws Exception {
        executarComandoModificador(() -> {
            if (emp == null || emp.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
            Empregado e = empregados.get(emp);
            if (e == null) throw new Exception("Empregado nao existe.");

            if (atributo.equals("sindicalizado")) {
                if (!valor.equals("true") && !valor.equals("false")) {
                    throw new Exception("Valor deve ser true ou false.");
                }
                boolean sind = Boolean.parseBoolean(valor);
                if (sind) {
                    if (idSindicato == null || idSindicato.isEmpty()) {
                        throw new Exception("Identificacao do sindicato nao pode ser nula.");
                    }
                    for (Empregado outro : empregados.values()) {
                        if (!outro.getId().equals(emp) && outro.isSindicalizado() && idSindicato.equals(outro.getIdSindicato())) {
                            throw new Exception("Ha outro empregado com esta identificacao de sindicato");
                        }
                    }
                    if (taxaSindical == null || taxaSindical.isEmpty()) {
                        throw new Exception("Taxa sindical nao pode ser nula.");
                    }
                    double taxa;
                    try {
                        taxa = Double.parseDouble(taxaSindical.replace(",", "."));
                    } catch (Exception ex) {
                        throw new Exception("Taxa sindical deve ser numerica.");
                    }
                    if (taxa < 0) {
                        throw new Exception("Taxa sindical deve ser nao-negativa.");
                    }

                    e.setSindicalizado(true);
                    e.setIdSindicato(idSindicato);
                    e.setTaxaSindical(taxa);
                } else {
                    e.setSindicalizado(false);
                    e.setIdSindicato(null);
                    e.setTaxaSindical(0.0);
                }
            } else {
                throw new Exception("Atributo nao existe.");
            }
        });
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String banco, String agencia, String contaCorrente) throws Exception {
        executarComandoModificador(() -> {
            if (emp == null || emp.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
            Empregado e = empregados.get(emp);
            if (e == null) throw new Exception("Empregado nao existe.");

            if (atributo.equals("metodoPagamento")) {
                if (!valor.equals("banco")) {
                    throw new Exception("Metodo de pagamento invalido.");
                }
                if (banco == null || banco.isEmpty()) {
                    throw new Exception("Banco nao pode ser nulo.");
                }
                if (agencia == null || agencia.isEmpty()) {
                    throw new Exception("Agencia nao pode ser nulo.");
                }
                if (contaCorrente == null || contaCorrente.isEmpty()) {
                    throw new Exception("Conta corrente nao pode ser nulo.");
                }
                e.setMetodoPagamento("banco");
                e.setBanco(banco);
                e.setAgencia(agencia);
                e.setContaCorrente(contaCorrente);
            } else {
                throw new Exception("Atributo nao existe.");
            }
        });
    }

    public void lancaTaxaServico(String membro, String data, String valor) throws Exception {
        executarComandoModificador(() -> {
            if (membro == null || membro.isEmpty()) throw new Exception("Identificacao do membro nao pode ser nula.");
            Empregado empSindicato = null;
            for (Empregado e : empregados.values()) {
                if (e.isSindicalizado() && membro.equals(e.getIdSindicato())) {
                    empSindicato = e;
                    break;
                }
            }
            if (empSindicato == null) throw new Exception("Membro nao existe.");

            LocalDate dataTaxa = parseData(data, "Data invalida.");
            double v;
            try {
                v = Double.parseDouble(valor.replace(",", "."));
            } catch (Exception ex) {
                throw new Exception("Valor deve ser numerico.");
            }
            if (v <= 0) throw new Exception("Valor deve ser positivo.");

            TaxaServico ts = new TaxaServico(dataTaxa, v);
            empSindicato.adicionarTaxaServico(ts);
        });
    }

    public String getTaxasServico(String emp, String dataInicial, String dataFinal) throws Exception {
        if (emp == null || emp.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        Empregado e = empregados.get(emp);
        if (e == null) throw new Exception("Empregado nao existe.");
        if (!e.isSindicalizado()) throw new Exception("Empregado nao eh sindicalizado.");

        LocalDate dInicio = parseData(dataInicial, "Data inicial invalida.");
        LocalDate dFim = parseData(dataFinal, "Data final invalida.");
        if (dInicio.isAfter(dFim)) throw new Exception("Data inicial nao pode ser posterior aa data final.");

        double total = 0.0;
        for (TaxaServico t : e.getTaxasServico()) {
            if (!t.getData().isBefore(dInicio) && t.getData().isBefore(dFim)) {
                total += t.getValor();
            }
        }
        return formatarMoeda(total);
    }

    public String totalFolha(String dataStr) throws Exception {
        LocalDate data = parseData(dataStr, "Data invalida.");
        FolhaDePagamento.ResultadoFolha resultado = FolhaDePagamento.calcular(empregados, data, false);
        return formatarMoeda(resultado.totalFolha);
    }

    public void rodaFolha(String dataStr, String saida) throws Exception {
        executarComandoModificador(() -> {
            LocalDate data = parseData(dataStr, "Data invalida.");
            String relatorio;
            if (folhaGeradaPorData.containsKey(data)) {
                relatorio = folhaGeradaPorData.get(data);
            } else {
                FolhaDePagamento.ResultadoFolha resultado = FolhaDePagamento.calcular(empregados, data, true);
                relatorio = resultado.relatorio;
                folhaGeradaPorData.put(data, relatorio);
            }

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(saida), "ISO-8859-1")) {
                writer.write(relatorio);
            }
        });
    }

    public static void salvar(Sistema s) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("sistema.dat"))) {
            oos.writeObject(s);
        }
    }

    public static Sistema carregar() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("sistema.dat"))) {
            Sistema s = (Sistema) ois.readObject();
            s.checkTransients();
            return s;
        } catch (Exception e) {
            return new Sistema();
        }
    }
}
