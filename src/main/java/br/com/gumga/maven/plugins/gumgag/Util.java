/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.gumga.maven.plugins.gumgag;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.persistence.GeneratedValue;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 *
 * @author munif
 */
public class Util {

    public static String primeiraMaiuscula(String s) {
        return s.substring(0, 1).toUpperCase().concat(s.substring(1));
    }

    public static String primeiraMinuscula(String s) {
        return s.substring(0, 1).toUpperCase().concat(s.substring(1));
    }

    public static List<Field> getTodosAtributosMenosIdAutomatico(Class classe) {
        List<Field> todosAtributos = getTodosAtributos(classe);
        Field aRemover = null;
        for (Field f : todosAtributos) {
            if (f.isAnnotationPresent(GeneratedValue.class)) {
                aRemover = f;
                break;
            }
        }
        if (aRemover != null) {
            todosAtributos.remove(aRemover);
        }
        return todosAtributos;
    }

    public static List<Field> getTodosAtributos(Class classe) throws SecurityException {
        List<Field> aRetornar = new ArrayList<Field>();
        if (!classe.getSuperclass().equals(Object.class)) {
            aRetornar.addAll(getTodosAtributosMenosIdAutomatico(classe.getSuperclass()));
        }
        aRetornar.addAll(Arrays.asList(classe.getDeclaredFields()));
        return aRetornar;
    }

    public static ClassLoader getClassLoader(MavenProject project) {
        ClassLoader aRetornar = null;
        try {
            List elementos = new ArrayList();
            elementos.addAll(project.getRuntimeClasspathElements());
            elementos.addAll(project.getTestClasspathElements());

            URL[] runtimeUrls = new URL[elementos.size()];
            for (int i = 0; i < elementos.size(); i++) {
                String element = (String) elementos.get(i);
                runtimeUrls[i] = new File(element).toURI().toURL();
            }
            aRetornar = new URLClassLoader(runtimeUrls,
                    Thread.currentThread().getContextClassLoader());

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return aRetornar;
    }

    public static Field primeiroAtributo(Class classe) {
        return getTodosAtributosMenosIdAutomatico(classe).get(0);
    }

    public static Class getTipoGenerico(Field atributo) {
        ParameterizedType type = (ParameterizedType) atributo.getGenericType();
        Type[] typeArguments = type.getActualTypeArguments();
        Class tipoGenerico = (Class) typeArguments[atributo.getType().equals(Map.class) ? 1 : 0];
        return tipoGenerico;
    }

    public static void geraGumga(Log log) {
        log.info("\n"
                + "   _____ _    _ __  __  _____          \n"
                + "  / ____| |  | |  \\/  |/ ____|   /\\    \n"
                + " | |  __| |  | | \\  / | |  __   /  \\   \n"
                + " | | |_ | |  | | |\\/| | | |_ | / /\\ \\  \n"
                + " | |__| | |__| | |  | | |__| |/ ____ \\ \n"
                + "  \\_____|\\____/|_|  |_|\\_____/_/    \\_\\\n"
                + "                                       \n"
                + "                                       \n"
                + "");
    }

    public static String etiqueta(Field atributo) {
        return primeiraMaiuscula(atributo.getName());
    }
    
    public static String windowsSafe(String s){
        return s.replaceAll("\\\\", "/");
    }

}

/*

 mvn archetype:generate -DinteractiveMode=false -DarchetypeGroupId=ex.empresa  -DarchetypeArtifactId=sistema-archetype  -DgroupId=br.com.gumga -DartifactId=exemplodominios -Dversion=0.1
 cd exemplodominios
 mvn clean install
 cd exemplodominios-domain 
 mvn br.com.gumga:gumgag:entidade -Dentidade=br.com.gumga.exemplodominios.domain.model.Teste -Datributos="nome:String,logico:gumga.framework.domain.domains.GumgaBoolean,cep:gumga.framework.domain.domains.GumgaCEP,cnpj:gumga.framework.domain.domains.GumgaCNPJ,cpf:gumga.framework.domain.domains.GumgaCPF,email:gumga.framework.domain.domains.GumgaEMail,ip4:gumga.framework.domain.domains.GumgaIP4,ip6:gumga.framework.domain.domains.GumgaIP6,money:gumga.framework.domain.domains.GumgaMoney,multiLine:gumga.framework.domain.domains.GumgaMultiLineString,telefone:gumga.framework.domain.domains.GumgaPhoneNumber,url:gumga.framework.domain.domains.GumgaURL"
 mvn br.com.gumga:gumgag:entidade -Dentidade=br.com.gumga.exemplodominios.domain.model.TesteAddress -Datributos="residencial:gumga.framework.domain.domains.GumgaAddress,comercial:gumga.framework.domain.domains.GumgaAddress"
 mvn br.com.gumga:gumgag:entidade -Dentidade=br.com.gumga.exemplodominios.domain.model.TesteGeo     -Datributos="location:gumga.framework.domain.domains.GumgaGeoLocation"
 mvn br.com.gumga:gumgag:entidade -Dentidade=br.com.gumga.exemplodominios.domain.model.TesteFile    -Datributos="file:gumga.framework.domain.domains.GumgaFile"
 mvn br.com.gumga:gumgag:entidade -Dentidade=br.com.gumga.exemplodominios.domain.model.TesteImage   -Datributos="image:gumga.framework.domain.domains.GumgaImage"
 mvn br.com.gumga:gumgag:entidade -Dentidade=br.com.gumga.exemplodominios.domain.model.TesteTime    -Datributos="horario:gumga.framework.domain.domains.GumgaTime"
 mvn br.com.gumga:gumgag:entidade -Dentidade=br.com.gumga.exemplodominios.domain.model.Usuario      -Datributos="nome:String"
 mvn br.com.gumga:gumgag:entidade -Dentidade=br.com.gumga.exemplodominios.domain.model.GrupoUsuario -Datributos="nome:String,usuarios:List<Usuario>:@ManyToMany"

 mvn clean install
 cd ..
 cd exemplodominios-application
 mvn br.com.gumga:gumgag:aplicacao -Dentidade=br.com.gumga.exemplodominios.domain.model.Teste
 mvn br.com.gumga:gumgag:aplicacao -Dentidade=br.com.gumga.exemplodominios.domain.model.TesteAddress 
 mvn br.com.gumga:gumgag:aplicacao -Dentidade=br.com.gumga.exemplodominios.domain.model.TesteGeo 
 mvn br.com.gumga:gumgag:aplicacao -Dentidade=br.com.gumga.exemplodominios.domain.model.TesteFile 
 mvn br.com.gumga:gumgag:aplicacao -Dentidade=br.com.gumga.exemplodominios.domain.model.TesteImage  
 mvn br.com.gumga:gumgag:aplicacao -Dentidade=br.com.gumga.exemplodominios.domain.model.TesteTime  
 mvn br.com.gumga:gumgag:aplicacao -Dentidade=br.com.gumga.exemplodominios.domain.model.Usuario      
 mvn br.com.gumga:gumgag:aplicacao -Dentidade=br.com.gumga.exemplodominios.domain.model.GrupoUsuario 

 cd ..
 cd exemplodominios-presentation
 mvn br.com.gumga:gumgag:apresentacao -Dentidade=br.com.gumga.exemplodominios.domain.model.Teste
 mvn br.com.gumga:gumgag:apresentacao -Dentidade=br.com.gumga.exemplodominios.domain.model.TesteAddress 
 mvn br.com.gumga:gumgag:apresentacao -Dentidade=br.com.gumga.exemplodominios.domain.model.TesteGeo 
 mvn br.com.gumga:gumgag:apresentacao -Dentidade=br.com.gumga.exemplodominios.domain.model.TesteFile
 mvn br.com.gumga:gumgag:apresentacao -Dentidade=br.com.gumga.exemplodominios.domain.model.TesteImage  
 mvn br.com.gumga:gumgag:apresentacao -Dentidade=br.com.gumga.exemplodominios.domain.model.TesteTime  
 mvn br.com.gumga:gumgag:apresentacao -Dentidade=br.com.gumga.exemplodominios.domain.model.Usuario  
 mvn br.com.gumga:gumgag:apresentacao -Dentidade=br.com.gumga.exemplodominios.domain.model.GrupoUsuario  
 cd ..
 mvn clean install




 */
