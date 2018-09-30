package br.nom.penha.bruno.proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import org.junit.Test;

import br.nom.penha.bruno.proxy.reverso.autenticacao.MultipartUtil;
import br.nom.penha.bruno.proxy.reverso.comum.TrataProxyReverso;


public class ProxyReversoTest {


    @Test
    public void testParseTokenDeUmQueryString() throws MalformedURLException, URISyntaxException {

    	// caso de teste basico positivo
        URI uri = new URI("http://java.sun.com?forum=2");
        String resultado = TrataProxyReverso.parseTokenDeUmaQueryString(uri, "forum");
        assertTrue(resultado.equals("2"));

        uri = new URI("http://java.sun.com?forum=2&foo=bar");
        resultado = TrataProxyReverso.parseTokenDeUmaQueryString(uri, "foo");
        assertTrue(resultado.equals("bar"));

        // caso de teste basico negativo
        uri = new URI("http://java.sun.com?forum=2");
        resultado = TrataProxyReverso.parseTokenDeUmaQueryString(uri, "forumXYZ");
        assertNull(resultado);

    }

    @Test
    public void testMultipartUtil() {
        String requisicaoEntrada = MultipartUtil.constroiRequisicaoEntrada("boundaryTeste", "tokenAutenticacao", new Date().toString(), "dados enviados de teste");
        assertEquals("boundaryTeste", MultipartUtil.getBoundary(requisicaoEntrada));

        String requisicaoInvalida = "{ \"invalido\": { \"dadoscarregados\": \"dados\" } }";
        assertNull(MultipartUtil.getBoundary(requisicaoInvalida));

        String[] multiparts = MultipartUtil.parseRequisicaoMultipart(requisicaoEntrada, "boundaryTeste");
        assertEquals(5, multiparts.length);
        assertEquals("tokenAutenticacao", multiparts[1].split("\n")[3]);
        assertEquals("dados enviados de teste", multiparts[3].split("\n")[3]);
    }
}
