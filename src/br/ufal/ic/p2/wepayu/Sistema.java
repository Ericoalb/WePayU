package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.models.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class Sistema implements Serializable {
    private Map<String, Empregado> empregados = new LinkedHashMap<>();
    private int proximoId = 1;

    public void zerarSistema() {
        empregados.clear();
        proximoId = 1;
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws Exception {
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
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao) throws Exception {
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
            if (e instanceof EmpregadoComissionado) return formatarMoeda(((EmpregadoComissionado) e).getComissao());
            throw new Exception("Atributo nao existe.");
        }

        throw new Exception("Atributo nao existe.");
    }

    private String formatarMoeda(String valor) {
        if (valor.contains(",")) return valor;
        try {
            double d = Double.parseDouble(valor);
            return String.format("%.2f", d).replace(".", ",");
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
        if (id == null || id.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        if (!empregados.containsKey(id)) throw new Exception("Empregado nao existe.");
        empregados.remove(id);
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

    public void alteraEmpregado(String emp, String atributo, String valor) throws Exception {
        if (emp == null || emp.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        Empregado e = empregados.get(emp);
        if (e == null) throw new Exception("Empregado nao existe.");

        if (atributo.equals("sindicalizado")) {
            boolean sind = Boolean.parseBoolean(valor);
            if (!sind) {
                e.setSindicalizado(false);
                e.setIdSindicato(null);
                e.setTaxaSindical(0.0);
            }
        }
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String idSindicato, String taxaSindical) throws Exception {
        if (emp == null || emp.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        Empregado e = empregados.get(emp);
        if (e == null) throw new Exception("Empregado nao existe.");

        if (atributo.equals("sindicalizado")) {
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
            }
        }
    }

    public void lancaTaxaServico(String membro, String data, String valor) throws Exception {
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

    // Persistência
    public static void salvar(Sistema s) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("sistema.dat"))) {
            oos.writeObject(s);
        }
    }

    public static Sistema carregar() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("sistema.dat"))) {
            return (Sistema) ois.readObject();
        } catch (Exception e) {
            return new Sistema();
        }
    }
}
