/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.gumga.maven.plugins.gumgag;

/**
 *
 * @author munif
 */
public class Util {
    
    public static String primeiraMaiuscula(String s){
        return s.substring(0,1).toUpperCase().concat(s.substring(1));
    }
    
}
