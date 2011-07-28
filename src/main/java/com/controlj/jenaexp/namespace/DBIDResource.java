package com.controlj.jenaexp.namespace;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 *
 */
public class DBIDResource {
    private static Model m = ModelFactory.createDefaultModel();
    static public Model getModel() { return m; }

    protected static final String uri="http://com.controlj.semantic.dbid#";


    public static Resource resource( String local)
        { return m.createResource( uri + local ); }
}
