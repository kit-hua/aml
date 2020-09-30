package translation.ontology;
/**
 * 
 */


import org.semanticweb.owlapi.model.OWLOntology;

import CAEX215.CAEXFileType;

/**
 * @author hua
 * This interface transforms AML to RDF triples
 */
public interface IAML2OWL {
	
	public boolean verifiy();
	
	public OWLOntology transform();
	
	
}
