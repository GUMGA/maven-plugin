/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.gumga.maven.plugins.gumgag;

import io.gumga.maven.plugins.gumgag.Util;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author munif
 */
public class UtilTest {

    public UtilTest() {
    }

    /**
     * Test of primeiraMaiuscula method, of class Util.
     */
    @Test
    public void testPrimeiraMaiuscula() {
        System.out.println("primeiraMaiuscula");
        String s = "teste";
        String expResult = "Teste";
        String result = Util.primeiraMaiuscula(s);
        assertEquals(expResult, result);

    }

    /**
     * Test of primeiraMinuscula method, of class Util.
     */
    @Test
    public void testPrimeiraMinuscula() {
        System.out.println("primeiraMinuscula");
        String s = "Teste";
        String expResult = "teste";
        String result = Util.primeiraMinuscula(s);
        assertEquals(expResult, result);
    }

    /**
     * Test of getTodosAtributosMenosIdAutomatico method, of class Util.
     */
    @Test
    public void testGetTodosAtributosMenosIdAutomatico() {
        System.out.println("getTodosAtributosMenosIdAutomatico");
        Class classe = Entidade.class;
        List<Field> expResult = null;
        try {
            expResult = Arrays.asList(new Field[]{Entidade.class.getDeclaredField("nome"), Entidade.class.getDeclaredField("idade")});
        } catch (Exception ex) {
            Logger.getLogger(UtilTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        List<Field> result = Util.getTodosAtributosMenosIdAutomatico(classe);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
    }

    /**
     * Test of getTodosAtributosNaoEstaticos method, of class Util.
     */
    @Test
    public void testGetTodosAtributosNaoEstaticos() {
        System.out.println("getTodosAtributosNaoEstaticos");
        Class classe = Entidade.class;
        List<Field> expResult = null;
        try {
            expResult = Arrays.asList(new Field[]{Entidade.class.getDeclaredField("id"), Entidade.class.getDeclaredField("nome"), Entidade.class.getDeclaredField("idade")});
        } catch (Exception ex) {
            Logger.getLogger(UtilTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        List<Field> result = Util.getTodosAtributosNaoEstaticos(classe);

        System.out.println("expResult:" + expResult);
        System.out.println("   Result:" + result);

        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
    }

    /**
     * Test of primeiroAtributo method, of class Util.
     */
    @Test
    public void testPrimeiroAtributo() {
        System.out.println("primeiroAtributo");
        Class classe = Entidade.class;
        Field expResult = null;
        try {
            expResult = Entidade.class.getDeclaredField("nome");
        } catch (Exception ex) {
            Logger.getLogger(UtilTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        Field result = Util.primeiroAtributo(classe);
        assertEquals(expResult, result);
    }

    /**
     * Test of getTipoGenerico method, of class Util.
     */
    @Test
    public void testGetTipoGenerico() {
        System.out.println("getTipoGenerico");
        Field atributo = null;
        try {
            atributo = ColecaoDeEntidade.class.getDeclaredField("entidades");
        } catch (Exception ex) {
            Logger.getLogger(UtilTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        Class expResult = Entidade.class;
        Class result = Util.getTipoGenerico(atributo);
        assertEquals(expResult, result);
    }

    /**
     * Test of etiqueta method, of class Util.
     */
    @Test
    public void testEtiqueta() {
        System.out.println("etiqueta");
        Field atributo = null;
        try {
            atributo = Entidade.class.getDeclaredField("nome");
        } catch (Exception ex) {
            Logger.getLogger(UtilTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        String expResult = "Nome";
        String result = Util.etiqueta(atributo);
        assertEquals(expResult, result);
    }

    /**
     * Test of windowsSafe method, of class Util.
     */
    @Test
    public void testWindowsSafe() {
        System.out.println("windowsSafe");
        String s = "c:\\windows";
        String expResult = "c:/windows";
        String result = Util.windowsSafe(s);
        assertEquals(expResult, result);
    }

    /**
     * Test of todosAtributosSeparadosPorVirgula method, of class Util.
     */
    @Test
    public void testTodosAtributosSeparadosPorVirgula() {
        System.out.println("todosAtributosSeparadosPorVirgula");
        Class classeEntidade = Entidade.class;
        String expResult = "nome,idade";
        String result = Util.todosAtributosSeparadosPorVirgula(classeEntidade);
        assertEquals(expResult, result);
    }

}
