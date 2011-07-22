package com.controlj.jenaexp;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.VCARD;

/**
 *
 */
public class Simple {
    private static Model createSample() {
        final String jsURI = "http://somewhere/JohnSmith";
        final String jsName = "John Smith";
        final String saURI = "http://somewhere/SteveAppling";
        final String saName = "Steve Appling";

        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("vc", VCARD.getURI());

        Resource johnSmith = model.createResource(jsURI).addProperty(VCARD.FN, jsName);
        Resource steveAppling = model.createResource(saURI).addProperty(VCARD.FN, saName);
        return model;
    }

    public static void one() {
        Model model = createSample();

        System.out.println("Writing out RDF:");
        model.write(System.out, "TURTLE");
    }

    public static void two() {
        Model model = createSample();

        Query query = QueryFactory.create("SELECT ?name WHERE { <http://somewhere/SteveAppling> <" + VCARD.getURI() + "FN>" + " ?name }");
        
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet resultSet = qe.execSelect();

        ResultSetFormatter.out(System.out, resultSet, query);
        qe.close();
    }
}
