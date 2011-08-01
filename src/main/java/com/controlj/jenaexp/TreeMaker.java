package com.controlj.jenaexp;

import com.controlj.jenaexp.namespace.CJP;
import com.controlj.jenaexp.namespace.DBIDResource;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 *
 */
public class TreeMaker {
    private final int campusLimit;
    private final int bldgLimit;
    private final int roomLimit;
    private final int ptLimit;
    private final Model model;
    private final DBIDResource dbid;

    private long pointCount = 0;
    private long statementCount = 0;
    private long lastDbid = 0;

    public TreeMaker(int campusLimit, int bldgLimit, int roomLimit, int ptLimit, Model model) {
        this.campusLimit = campusLimit;
        this.bldgLimit = bldgLimit;
        this.roomLimit = roomLimit;
        this.ptLimit = ptLimit;
        this.model = model;
        dbid = new DBIDResource(model);
    }

    public long generate() {

        Resource root = nextDbidNode();
        root.addProperty(RDFS.label, "Root");
        statementCount += 2;

        for (int i=0; i<campusLimit; i++) {
            generateCampus(i, root);
        }

        CJP.BAI.addProperty(RDFS.subClassOf, CJP.InputPoint);
        CJP.BAO.addProperty(RDFS.subClassOf, CJP.OutputPoint);
        CJP.BBI.addProperty(RDFS.subClassOf, CJP.InputPoint);
        CJP.BBO.addProperty(RDFS.subClassOf, CJP.OutputPoint);
        model.add(CJP.getModel());

        return lastDbid;
    }

    private Resource nextDbidNode() {
        return dbid.resource(Long.toString(lastDbid++));
    }

    private void generateCampus(int campusNum, Resource parent) {
        Resource campus = nextDbidNode();
        campus.addProperty(RDFS.label, "Campus #"+campusNum);
        campus.addProperty(CJP.hasGeoParent, parent);
        campus.addProperty(CJP.areaType, "campus");
        statementCount += 4;

        for (int i=0; i<bldgLimit; i++) {
            generateBuildings(campusNum, i, campus);
        }
    }

    private void generateBuildings(int campusNum, int buildingNum, Resource parent) {
        Resource building = nextDbidNode();

        building.addProperty(RDFS.label, "Building #"+buildingNum+" in campus #"+campusNum);
        building.addProperty(CJP.hasGeoParent, parent);
        building.addProperty(CJP.areaType, "building");
        statementCount += 4;

        for (int i=0; i<roomLimit; i++) {
            generateRoom(campusNum, buildingNum, i, building);
        }
    }

    private void generateRoom(int campusNum, int buildingNum, int roomNum, Resource parent) {
        Resource room = nextDbidNode();

        room.addProperty(RDFS.label, "Room #" + campusNum + "-" + buildingNum + "-"+ roomNum);
        room.addProperty(CJP.hasGeoParent, parent);
        room.addProperty(CJP.areaType, "room");
        statementCount += 4;

        for (int i=0; i<ptLimit; i++) {
            generatePoint(campusNum, buildingNum, roomNum, i, room);
        }
    }

    private void generatePoint(int campusNum, int buildingNum, int roomNum, int pointNum, Resource parent) {
        Resource point = nextDbidNode();

        point.addProperty(RDFS.label, "Point #"+pointNum+" in Room#" + campusNum + "-" + buildingNum + "-"+ roomNum);
        point.addProperty(CJP.hasGeoParent, parent);

        Resource typeRes = null;
        switch (pointNum % 4) {
            case 0:
                typeRes = CJP.BAI;
                break;
            case 1:
                typeRes = CJP.BAO;
                break;
            case 2:
                typeRes = CJP.BBI;
                break;
            default:
                typeRes = CJP.BBO;
        }

        point.addProperty(RDF.type, typeRes);
        statementCount += 4;
        pointCount++;
    }

    public long getPointCount() {
        return pointCount;
    }

    public long getStatementCount() {
        return statementCount;
    }

    public long getLastDbid() {
        return lastDbid;
    }
}
