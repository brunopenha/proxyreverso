package br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl;

public class RequisicaoRaiz {

	private RequisicaoACL requisicaoACL;

	public RequisicaoRaiz() {
		requisicaoACL = new RequisicaoACL();
	}

	public RequisicaoACL getRequisicaoACL() {
		return requisicaoACL;
	}

	public void setRequisicaoACL(RequisicaoACL aclRequest) {
		this.requisicaoACL = aclRequest;
	}
}
