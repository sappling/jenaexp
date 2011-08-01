package com.controlj.jenaexp.namespace;

import com.hp.hpl.jena.rdf.model.*;

/**
 * Pre-Defined ControlJ namespaced properties
 */
public class CJP {
    private static Model m = ModelFactory.createDefaultModel();
    static public Model getModel() { return m; }

    protected static final String uri="http://com.controlj.semantic.cjproperty#";
    public static String getSPARQLPrefix() { return "PREFIX cjp: <"+uri+">\n"; }

    protected static final Resource resource( String local )
        { return m.createResource( uri + local ); }

    protected static final Property property( String local )
        { return m.createProperty( uri, local ); }

    public static final Resource BAI = resource("BAI");
    public static final Resource BAO = resource("BAO");
    public static final Resource BBI = resource("BBI");
    public static final Resource BBO = resource("BBO");

    public static final Resource InputPoint = resource("InputPoint");
    public static final Resource OutputPoint = resource("OutputPoint");

    public static final Resource PhysicalPoint = resource("PhysicalPoint");

    public static final Property areaType = property("areaType");
    public static final Property pointType = property("pointType");
    public static final Property dispName = property("dispName");

    public static final Property hasGeoParent = property("hasGeoParent");
}
