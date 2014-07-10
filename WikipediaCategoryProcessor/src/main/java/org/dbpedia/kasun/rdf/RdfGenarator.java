/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * Date Author Changes Sep 17, 2013 Kasun Perera Created
 *
 */
package org.dbpedia.kasun.rdf;


import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.dbpedia.kasun.categoryprocessor.CategoryLinksDB;
import org.dbpedia.kasun.categoryprocessor.NodeDB;
import org.dbpedia.kasun.categoryprocessor.Page;
import org.dbpedia.kasun.categoryprocessor.PageDB;
import org.yago.javatools.parsers.PlingStemmer;

/**
 * TODO- describe the purpose of the class
 *
 */
public class RdfGenarator
{

    public static void main(String[] args) {
        String headCandidates = args[0];
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(headCandidates), "utf-8"));
        } catch (UnsupportedEncodingException uee) {
            System.err.println(uee.getMessage());
            System.exit(1);
        } catch (FileNotFoundException fnfe) {
            System.err.println(fnfe.getMessage());
            System.exit(1);
        }
        String line;
        List<String> heads = new ArrayList<String>();
        try {
            while ((line = br.readLine()) != null)
            {
                if (!line.startsWith("#") && !line.isEmpty())
                {
                    heads.add(line.trim());
                }
            }
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
            System.exit(1);
        }
        try {
            br.close();
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
            System.exit(1);
        }
        for (String head: heads) {
            try {
                String normalizedHead = capitalize(singularize(head));
                Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(normalizedHead + ".ttl"), "utf-8"));
                RdfGenarator.getCategoriesForHead(head, w);
                w.close();
            } catch (IOException ioe) {
                System.err.println(ioe.getMessage());
            }
        }
    }

    public static String singularize(String term) {
        return PlingStemmer.stem(term);
    }

    public static String capitalize(String word) {
        return Character.toUpperCase(word.charAt(0)) + word.substring(1);
    }

    public static void getCategoriesForHead( String head, Writer w ) throws IOException {

        ArrayList<String> categoriesForHead = NodeDB.getCategoriesByHead( head );

for(int j=0; j<categoriesForHead.size();j++){
    getPagesForCategory( categoriesForHead.get( j ), capitalize(singularize(head)), w);
}
categoriesForHead.clear();

    }

    public static void getPagesForCategory( String catName, String head, Writer w ) throws IOException {

        ArrayList<Integer> clFromPageID = CategoryLinksDB.getPagesLinkedByCatName( catName );

        for ( int i = 0; i < clFromPageID.size(); i++ )
        {

                Page page = PageDB.getPagebyID( clFromPageID.get( i ) );
                //namespace == 0 means it's an article page
                if ( page.getPageNamespace() == 0 )
                {
                    w.write("<http://dbpedia.org/resource/" + page.getPageName() + "> a <http://dbpedia.org/ontology/" + head + "> .\n");
                } else
                {
                    if ( page.getPageNamespace() == 14 )
                    {

                        //namespace==14 means it's a categorypage recurcive the categorypage
                        //recursion causes segmentation error go for only fist child
                       // getPagesForCategory( page.getPageName() );
                        getPagesForCategoryFirstChild( page.getPageName(), head, w );
                    }
                }

        }
        
        clFromPageID.clear();
    }
    
    public static void getPagesForCategoryFirstChild( String catName, String head , Writer w) throws IOException {

        ArrayList<Integer> clFromPageID = CategoryLinksDB.getPagesLinkedByCatName( catName );

        for ( int i = 0; i < clFromPageID.size(); i++ )
        {


                Page page = PageDB.getPagebyID( clFromPageID.get( i ) );
                if ( page.getPageNamespace() == 0 )
                {
                    //namespace==0 means it's a article page
                    w.write("<http://dbpedia.org/resource/" + page.getPageName() + "> a <http://dbpedia.org/ontology/" + head + "> .\n");


                } 
                /*
                else
                {
                    if ( page.getPageNamespace() == 14 )
                    {

                        //namespace==14 means it's a categorypage recurcive the categorypage
                        getPagesForCategory( page.getPageName() );
                    }
                }
                * 
                */


                   


        }
        
        clFromPageID.clear();

    }
}
