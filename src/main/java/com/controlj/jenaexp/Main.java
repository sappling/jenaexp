package com.controlj.jenaexp;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.tdb.TDBFactory;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.List;

public class Main {
    static private final String dbDir = "database/test";
    static private final String exportFileName = "export/db.ttl";
    static private final Format numFormat = new DecimalFormat("#,###");

    static public void main(String args[]) {
        /*
        System.out.print("Running with:");
        for (String arg : args) {
            System.out.print(arg);
            System.out.print(" ");
        }
        System.out.println();
        */
        if (args.length == 0) {
            System.out.println("Invalid syntax, must have at least one argument, the command name.");
            System.exit(-1);
        }
        if (args[0].equalsIgnoreCase("maketree")) {
            Model model = createModel(dbDir);

            try {
                long startTree = System.currentTimeMillis();
                TreeMaker maker = createTree(model);

                long numMade = maker.getLastDbid();
                long endTree = System.currentTimeMillis();

                model.commit();
                long endCommit = System.currentTimeMillis();

                System.out.println("Created " + numFormat.format(numMade) + " resources with " +
                        numFormat.format(maker.getStatementCount()) + " statements.");
                System.out.println("Total of "+numFormat.format(maker.getPointCount()) + " points");
                System.out.println("creation= "+(endTree - startTree)+ " mSec, commit= "+(endCommit - endTree)+ " mSec.");
            } finally {
                if (model != null) {
                    model.close();
                }
            }
        } else if (args[0].equalsIgnoreCase("export")) {
            Model model = createModel(dbDir);
            try {
                exportTurtle(model);
            } finally {
                if (model != null) {
                    model.close();
                }
            }
        }  else if (args[0].equalsIgnoreCase("clean")) {
            try {

                File dir = new File(dbDir);
                if (dir.exists()) {
                    FileUtils.forceDelete(dir);
                }
            } catch (IOException e) {
                System.err.println("Error deleting database dir at: "+ dbDir);
                e.printStackTrace(System.err);
            }
        } else if (args[0].equalsIgnoreCase("query1")) {
            Model model = createModel(dbDir);
            QueryRunner qr = new QueryRunner(model);

            String qString =
            "SELECT ?name WHERE\n"+
            "{?y rdfs:label \"Building #10 in campus #2\" .\n"+
            "?x cjp:hasGeoParent+ ?y .\n" +
            "?x rdf:type <http://com.controlj.semantic.cjproperty#BAI> .\n"+
            "?x rdfs:label ?name . }";


            Query q = qr.buildQuery(qString);
            long start = System.currentTimeMillis();
            List<String> names = qr.runStringListQuery(q, "name");
            long end = System.currentTimeMillis();

            System.out.printf("Found %d names in %d mSec\n", names.size(), (end - start));

        }  else if (args[0].equalsIgnoreCase("query2")) {
            Model model = createModel(dbDir);
            QueryRunner qr = new QueryRunner(ModelFactory.createRDFSModel(model));

            String qString =
            "SELECT ?name WHERE\n"+
            "{?y rdfs:label \"Building #10 in campus #2\" .\n"+
            "?x cjp:hasGeoParent+ ?y .\n" +
            "?x rdf:type <http://com.controlj.semantic.cjproperty#InputPoint> .\n"+
            "?x rdfs:label ?name . }";


            Query q = qr.buildQuery(qString);
            long start = System.currentTimeMillis();
            List<String> names = qr.runStringListQuery(q, "name");
            long end = System.currentTimeMillis();

            try {
                Thread.currentThread().sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            long start2 = System.currentTimeMillis();
            names = qr.runStringListQuery(q, "name");
            long end2 = System.currentTimeMillis();

            System.out.printf("Found %d names in %d mSec, 2'nd time was %dmSec\n", names.size(), (end - start), (end2 - start2));

        }  else if (args[0].equalsIgnoreCase("query3")) {
            Model model = createModel(dbDir);
            QueryRunner qr = new QueryRunner(model);

            String qString =
            "SELECT ?name WHERE\n"+
            "{?y rdfs:label \"Building #10 in campus #2\" .\n"+
            "?x cjp:hasGeoParent+ ?y .\n" +
            "{ ?x rdf:type <http://com.controlj.semantic.cjproperty#BAI> .} UNION { ?x rdf:type <http://com.controlj.semantic.cjproperty#BBI> .}\n"+
            "?x rdfs:label ?name . }";


            Query q = qr.buildQuery(qString);
            long start = System.currentTimeMillis();
            List<String> names = qr.runStringListQuery(q, "name");
            long end = System.currentTimeMillis();

            try {
                Thread.currentThread().sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            long start2 = System.currentTimeMillis();
            names = qr.runStringListQuery(q, "name");
            long end2 = System.currentTimeMillis();

            System.out.printf("Found %d names in %d mSec, 2'nd time was %dmSec\n", names.size(), (end - start), (end2 - start2));

        } else {
            System.err.println("Unknown command '"+args[0]+"'");
            System.exit(-1);
        }
    }

    static private Model createModel(String modelDir) {

        try {
            FileUtils.forceMkdir(new File(modelDir));
        } catch (IOException e) {
            System.err.println("Error creating directory for model at "+new File(modelDir));
            System.exit(-1);
        }
        return TDBFactory.createModel(modelDir);
    }


    static private TreeMaker createTree(Model model) {
        TreeMaker maker = new TreeMaker(10, 25, 100, 50, model);
        //TreeMaker maker = new TreeMaker(1, 1, 1, 4, model);
        maker.generate();
        return maker;
    }

    static private void exportTurtle(Model model) {
        File outFile = new File(exportFileName);
        if (outFile.exists()) {
            try {
                FileUtils.forceDelete(outFile);
            } catch (IOException e) {
                System.err.println("Error deleting '"+outFile.toString()+"'");
                System.exit(-1);
            }
        }


        try {
            FileUtils.forceMkdir(outFile.getParentFile());
            
            FileWriter writer = new FileWriter(outFile);
            model.write(writer, "TURTLE");
            writer.close();
        } catch (IOException e) {
            System.err.println("Error writing file");
            e.printStackTrace(System.err);
        }
    }

}