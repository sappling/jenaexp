import spock.lang.Specification
import com.hp.hpl.jena.vocabulary.VCARD
import com.hp.hpl.jena.rdf.model.Resource
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.query.QueryExecutionFactory
import com.hp.hpl.jena.query.QueryExecution
import com.hp.hpl.jena.query.ResultSet
import com.hp.hpl.jena.query.QuerySolution
import com.hp.hpl.jena.rdf.model.RDFNode
import com.hp.hpl.jena.rdf.model.Property
import com.hp.hpl.jena.vocabulary.RDFS
import com.controlj.jenaexp.CJP
import com.controlj.jenaexp.DBIDResource
import com.hp.hpl.jena.vocabulary.RDF
import com.hp.hpl.jena.reasoner.ReasonerRegistry

class BasicTest extends Specification {
    private static final String baseURI = "http://com.controlj.semantic.";
    private static final String locURI = baseURI + "dbid#";
    private static final String cjpropURI = baseURI + "cjproperty#";
    //private static final String areaTypeProp = cjpropURI +"areatype";
    //private static final String dispNameProp = cjpropURI + "dispname"


    Model model;
    private Property areaTypeProp, dispNameProp;

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
        model = ModelFactory.createDefaultModel();

        DBIDResource.resource("100", model)
            .addProperty(CJP.areaType, "campus")
            .addProperty(CJP.dispName, "School 1")

        DBIDResource.resource("200", model)
            .addProperty(CJP.areaType, "building")
            .addProperty(CJP.dispName, "Fielding Hall")

        when:
        def result = runQuery(query, model)

        then:
        result.size() == 1
        result[0].name == "Fielding Hall"
        
    }

    def withRDFS() {
        String query = CJP.getSPARQLPrefix() +
        'PREFIX rdfs: <'+RDFS.uri+'>\n'+
        'PREFIX rdf: <'+RDF.getURI()+'>\n'+        
        '''SELECT ?label WHERE
        {?x rdf:type cjp:PhysicalPoint .
         ?x rdfs:label ?label . }'''

        setup:
        model = ModelFactory.createDefaultModel();
        DBIDResource.resource("100", model)
            .addProperty(RDF.type, CJP.BAI)
            .addProperty(RDFS.label, "Sample Point")


        CJP.BAI.addProperty(RDFS.subClassOf, CJP.PhysicalPoint)
        model.add(CJP.getModel())

        when:
        def result = runQuery(query, ModelFactory.createRDFSModel(model))

        then:
        result.size() == 1
        result[0].label == "Sample Point"
    }
}
