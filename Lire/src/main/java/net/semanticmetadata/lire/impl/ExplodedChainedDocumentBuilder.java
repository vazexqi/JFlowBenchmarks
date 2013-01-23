package net.semanticmetadata.lire.impl;

import java.awt.image.BufferedImage;
import java.util.Iterator;

import net.semanticmetadata.lire.AbstractDocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilder;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;

/**
 * This class mimics ChainedDocumentBuilder but makes each stage more explicit compared to the
 * original ChainedDocumentBuilder with its nested loop. This is necessary for the tool to infer the
 * stages of the pipeline instead of just blindly transforming everything..
 * 
 * @author vazexqi
 * 
 */
public class ExplodedChainedDocumentBuilder extends AbstractDocumentBuilder {
    private DocumentBuilder colorLayoutBuilder;

    private DocumentBuilder edgeBuilder;

    private DocumentBuilder colorBuilder;

    private DocumentBuilder autoBuilder;

    private DocumentBuilder CEDDBuilder;

    private DocumentBuilder FCTHBuilder;

    private DocumentBuilder histogramBuilder;

    private DocumentBuilder tamuraBuilder;

    private DocumentBuilder gaborBuilder;

    public Document createDocument(BufferedImage image, String identifier) {
        Document doc= new Document();

        if (identifier != null)
            doc.add(new Field(DocumentBuilder.FIELD_NAME_IDENTIFIER, identifier, Field.Store.YES, Field.Index.NOT_ANALYZED));

        Document colorLayoutDoc= colorLayoutBuilder.createDocument(image, identifier);
        accumulateFields(doc, colorLayoutDoc);

        Document edgeBuilderDoc= edgeBuilder.createDocument(image, identifier);
        accumulateFields(doc, edgeBuilderDoc);

        Document colorBuilderDoc= colorBuilder.createDocument(image, identifier);
        accumulateFields(doc, colorBuilderDoc);

        Document autoBuilderDoc= autoBuilder.createDocument(image, identifier);
        accumulateFields(doc, autoBuilderDoc);

        Document CEDDBuilderDoc= CEDDBuilder.createDocument(image, identifier);
        accumulateFields(doc, CEDDBuilderDoc);

        Document FCTHBuilderDoc= FCTHBuilder.createDocument(image, identifier);
        accumulateFields(doc, FCTHBuilderDoc);

        Document histogramBuilderDoc= histogramBuilder.createDocument(image, identifier);
        accumulateFields(doc, histogramBuilderDoc);

        Document tamuraBuilderDoc= tamuraBuilder.createDocument(image, identifier);
        accumulateFields(doc, tamuraBuilderDoc);

        Document gaborBuilderDoc= gaborBuilder.createDocument(image, identifier);
        accumulateFields(doc, gaborBuilderDoc);

        return doc;
    }

    private void accumulateFields(Document accumulatorDoc, Document augmenterDoc) {
        for (Iterator<Fieldable> iterator= augmenterDoc.getFields().iterator(); iterator.hasNext();) {
            Field f= (Field)iterator.next();
            if (!f.name().equals(DocumentBuilder.FIELD_NAME_IDENTIFIER)) {
                accumulatorDoc.add(f);
            }
        }
    }

    public void addColorLayoutBuilder(DocumentBuilder colorLayoutBuilder) {
        this.colorLayoutBuilder= colorLayoutBuilder;
    }

    public void addEdgeBuilder(DocumentBuilder edgeHistogramBuilder) {
        this.edgeBuilder= edgeHistogramBuilder;
    }

    public void addColorBuilder(DocumentBuilder scalableColorBuilder) {
        this.colorBuilder= scalableColorBuilder;
    }

    public void addAutoBuilder(DocumentBuilder autoColorCorrelogramDocumentBuilder) {
        this.autoBuilder= autoColorCorrelogramDocumentBuilder;
    }

    public void addCEDDBuilder(DocumentBuilder ceddDocumentBuilder) {
        this.CEDDBuilder= ceddDocumentBuilder;
    }

    public void addFCTHBuilder(DocumentBuilder fcthDocumentBuilder) {
        this.FCTHBuilder= fcthDocumentBuilder;
    }

    public void addHistogramBuilder(DocumentBuilder colorHistogramDocumentBuilder) {
        this.histogramBuilder= colorHistogramDocumentBuilder;
    }

    public void addTamuraBuilder(DocumentBuilder tamuraDocumentBuilder) {
        this.tamuraBuilder= tamuraDocumentBuilder;
    }

    public void addGaborBuilder(DocumentBuilder gaborDocumentBuilder) {
        this.gaborBuilder= gaborDocumentBuilder;
    }
}
