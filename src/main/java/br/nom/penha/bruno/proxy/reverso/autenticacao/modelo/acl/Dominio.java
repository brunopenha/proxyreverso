package br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl;

import java.util.ArrayList;
import java.util.List;

public class Dominio {

	private String tipo;
	private String nome;
	private List<String> papeis;

	public Dominio() {
		this.papeis = new ArrayList<String>();
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String type) {
		this.tipo = type;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String name) {
		this.nome = name;
	}

	public List<String> getPapeis() {
		return papeis;
	}

	public void setPapeis(List<String> roles) {
		this.papeis = roles;
	}
}
