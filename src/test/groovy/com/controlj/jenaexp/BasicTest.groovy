import spock.lang.Specification

import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.query.QueryExecutionFactory
import com.hp.hpl.jena.query.QueryExecution
import com.hp.hpl.jena.query.ResultSet
import com.hp.hpl.jena.query.QuerySolution
import com.hp.hpl.jena.rdf.model.RDFNode
import com.hp.hpl.jena.rdf.model.Property
import com.hp.hpl.jena.vocabulary.RDFS
import com.controlj.jenaexp.namespace.CJP
import com.controlj.jenaexp.namespace.DBIDResource
import com.hp.hpl.jena.vocabulary.RDF
import com.hp.hpl.jena.rdf.model.Resource

class BasicTest extends Specification {
    Model model = ModelFactory.createDefaultModel();

    def runQuery(String queryString, Model model) {
        QueryExecution qe = QueryExecutionFactory.create(queryString, model)
        def result = [];
        try {
            ResultSet rs = qe.execSelect()
            while (rs.hasNext()) {
                QuerySolution qs = rs.next()
                Iterator nameIt = qs.varNames()

                def nextEntry = [:]
                while (nameIt.hasNext()) {
                    String nextName = nameIt.next()
                    RDFNode nextNode = qs.get(nextName)
                    if (nextNode.isLiteral()) {
                        nextEntry.put(nextName, nextNode.asLiteral().getLexicalForm())
                    } else if (nextNode.isResource()) {
                        nextEntry.put(nextName, nextNode.asResource().getURI())
                    }
                    result << nextEntry;
                }
            }
            return result;
        } finally {
            qe.close()
        }
    }


    def basicQuery() {
        String query = CJP.getSPARQLPrefix() +
        '''SELECT ?name WHERE
        {?x cjp:areaType "building" .
         ?x cjp:dispName ?name . }'''

        setup:
        //model = ModelFactory.createDefaultModel();

        DBIDResource.resource("100")
            .addProperty(CJP.areaType, "campus")
            .addProperty(CJP.dispName, "School 1")

        DBIDResource.resource("200")
            .addProperty(CJP.areaType, "building")
            .addProperty(CJP.dispName, "Fielding Hall")
        model.add(CJP.getModel())
        model.add(DBIDResource.getModel())

        when:
        def result = runQuery(query, model)

        then:
        result.size() == 1
        result[0].name == "Fielding Hall"
        
    }

    def "RDFS sucClassOf Inference"() {
        String query = CJP.getSPARQLPrefix() +
        'PREFIX rdfs: <'+RDFS.uri+'>\n'+
        'PREFIX rdf: <'+RDF.getURI()+'>\n'+        
        '''SELECT ?label WHERE
        {?x rdf:type cjp:PhysicalPoint .
         ?x rdfs:label ?label . }'''

        setup:
        DBIDResource.resource("100")
            .addProperty(RDF.type, CJP.BAI)
            .addProperty(RDFS.label, "Sample Point")


        CJP.BAI.addProperty(RDFS.subClassOf, CJP.PhysicalPoint)
        model.add(CJP.getModel())
        model.add(DBIDResource.getModel())

        when:
        def result = runQuery(query, ModelFactory.createRDFSModel(model))

        then:
        result.size() == 1
        result[0].label == "Sample Point"
    }
    

    def "Tree Test"() {
        setup:
        Resource root = DBIDResource.resource("000")
            .addProperty(CJP.areaType, "Root")

        String query = CJP.getSPARQLPrefix() +
        'PREFIX rdfs: <'+RDFS.uri+'>\n'+
        """SELECT ?name WHERE
        { ?x cjp:hasGeoParent+ <${root.URI}> .
         ?x rdfs:label ?name . }"""

        Resource campus = DBIDResource.resource("100")
            .addProperty(CJP.areaType, "campus")
            .addProperty(RDFS.label, "U of KW")
            .addProperty(CJP.hasGeoParent, root)

        DBIDResource.resource("110")
            .addProperty(CJP.areaType, "building")
            .addProperty(RDFS.label, "Simonton Hall")
            .addProperty(CJP.hasGeoParent, campus)

        Resource dHall = DBIDResource.resource("120")
            .addProperty(CJP.areaType, "building")
            .addProperty(RDFS.label, "Duval Hall")
            .addProperty(CJP.hasGeoParent, campus)

        DBIDResource.resource("121")
            .addProperty(CJP.areaType, "gym")
            .addProperty(RDFS.label, "DH Gym")
            .addProperty(CJP.hasGeoParent, dHall)


        model.add(CJP.getModel())
        model.add(DBIDResource.getModel())

        when:
        def result = runQuery(query, model)

        then:
        result.size() == 4
        result*.name.containsAll(["U of KW", "Simonton Hall", "Duval Hall", "DH Gym"])
        //result[0].name == "Fielding Hall"

    }
}
