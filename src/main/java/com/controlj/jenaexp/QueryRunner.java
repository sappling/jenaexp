package com.controlj.jenaexp;

import com.controlj.jenaexp.namespace.CJP;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class QueryRunner {
    private final Model model;

    public QueryRunner(Model model) {
        this.model = model;
    }

    Query buildQuery(String userQuery)
    {
        String queryString = "PREFIX rdfs: <"+ RDFS.getURI()+">\n"+
        "PREFIX rdf: <"+ RDF.getURI()+">\n"+
        CJP.getSPARQLPrefix() + userQuery;
        return QueryFactory.create(queryString);
    }

    List<String> runStringListQuery(Query query, String varName) {
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet resultSet = qe.execSelect();

        List<String> result = new ArrayList<String>();

        while (resultSet.hasNext()){
            QuerySolution qs = resultSet.next();

            Literal nextResult = qs.getLiteral(varName);
            result.add(nextResult.getLexicalForm());
        }

        qe.close();
        return result;
    }
}
