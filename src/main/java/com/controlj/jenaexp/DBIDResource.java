package com.controlj.jenaexp;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 *
 */
public class DBIDResource {
    protected static final String uri="http://com.controlj.semantic.dbid#";

    public static Resource resource( String local, Model model )
        { return model.createResource( uri + local ); }
}
