//package clause;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//import javax.xml.parsers.ParserConfigurationException;
//import javax.xml.transform.TransformerException;
//
//import concept.model.AMLQueryConfig;
//import concept.model.AMLTargetConfig;
//import concept.model.GenericAMLConceptModel;
//import concept.tree.GenericTreeNode;
//import exporter.AMLExporter;
//import generator.BindingRegister;
//import generator.TPQRegister;
//import generator.XQueryVariableRegister;
//import xquery.XQueryVariable;
//
///**
// * Class for target nodes which are not bound, i.e., new elements
// * @author Yingbing Hua, IAR-IPR, KIT
// * @email yingbing.hua@kit.edu, yingbing.hua@gmail.com
// */
//public class NewElementTransformationOLD extends AbstractTransformation{
//	
////	private List<IdentityTransformation> subTrans;
////	private List<NewElementConstructor> subNewElements;
//	private AMLExporter exporter;
//	private GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> lca;
//	
//	public NewElementTransformationOLD(GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> targetNode,
//			GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> sourceRoot,
//			XQueryVariable baseVariable,
//			GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> lca, XQueryVariableRegister varReg, BindingRegister bindReg, TPQRegister tpqReg) {		
//		super(targetNode, sourceRoot, baseVariable, varReg, bindReg, tpqReg);
//		this.lca = lca;
////		subTrans = new ArrayList<IdentityTransformation>();
////		subNewElements = new ArrayList<NewElementConstructor>();
//		try {
//			exporter = new AMLExporter();
//		} catch (ParserConfigurationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
////	public void addSubIdentityTransformation (IdentityTransformation iTran) {
////		this.subTrans.add(iTran);
////	}
////	
////	public void addSubNewElement (NewElementConstructor newElement) {
////		this.subNewElements.add(newElement);
////	}
////	
//	/**
//	 * invent a ID string
//	 * TODO: make sure of uniqueness across the target document
//	 * TODO: this is wrong, ID invention shall happen within XQuery, the new ID here will be used for each matched object!
//	 * @return
//	 */
//	private String inventID () {
////		UUID uuid = UUID.randomUUID();
////		return uuid.toString();
////		return "caex215:createID ()";
//		return "uuid:randomUUID()";
////		return "caex215:generate-uuid-v4($id?next())";
////		return "caex215:createID2 (fn:random-number-generator(), 1)";
////		return "caex215:generate-uuid-v4-multiple(fn:random-number-generator(), 1)";
//	}
//	
//	@Override
//	public String getXQuery () {		
//		
//		String s = "";
//		// if the new element is a simple one, use its XML code
//		// Typical case is a new RR or SRC
//		// It could also be a static XML structure
//		if(isSimpleNewElement(targetNode)) {
//			try {
//				String exported = exporter.elementToString(targetNode.data.getObj());
//				exported = exported.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
//				s = exported;
//			} catch (TransformerException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		
//		// if the new element is a complex one, i.e., it contains sub-transformations
//		// a complex new element has the form of FOR-RETURN
//		// - FOR: lca correspondence
//		// - Return: new element tags, with {CONTENT}
//		//	- CONTENT: nested clauses
//		// TODO: we assume here the new element is always an IE
//		else {			
//						
//			String name = "IE";
////			String id = inventID();
////			XQueryVariable idVar = new XQueryVariable("id");
////			LetClause idlet = new LetClause(idVar, inventID()); 
////			ReturnClause idRet = new ReturnClause();
////			String start = "<InternalElement Name=\"" + name + "\" ID=\"{" + idVar.getExpression() + "}\">\n";
//			String start = "<InternalElement Name=\"" + name + "\" ID=\"{" + inventID() + "}\">\n";
//			String mainPart = start;
//			mainPart += "{\n";
//			
//			// handle all sub identity transformations
//			int nrSubTrans = getSubIdentityTransformations().size();
//			if(nrSubTrans > 0) {				
//				// each sub identity transformation is for a bound target node
//				for(IdentityTransformation subTran : getSubIdentityTransformations()) {
//					GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> subTarget = subTran.targetNode;
//					String xpath = varReg.getVariable(lca).getExpression() + tpqReg.getTPQ(bindReg.getBoundSourceNode(subTarget));
//					ForClause xfor = new ForClause(varReg.getChildVariable(lca), xpath);
//					ReturnClause xret = new ReturnClause();
//					xret.addClause(subTran);
//					mainPart += xfor.getXQuery();
//					mainPart += xret.getXQuery();
//					if(nrSubTrans > 1) {
//						mainPart += ",\n";
//						nrSubTrans--;
//					}
//				}
//			}			
//				
//			// handle all sub new elements
//			int nrSubNews = getSubNewElementConstructors().size();
//			if(nrSubNews > 0) {
//				mainPart = mainPart.substring(0, mainPart.length()-1);
//				mainPart = mainPart + ",\n";
//			}
//			
//			for(NewElementTransformationOLD subNewElement : getSubNewElementConstructors()) {
//				mainPart += subNewElement.getXQuery();
//				if(nrSubNews > 1) {
//					mainPart += ",\n";
//					nrSubNews--;
//				}
//			}			
//								
//			mainPart += "\n}\n";
//			String end = "</InternalElement>\n";
//			mainPart += end;
//			
////			idRet.addClause(new StringClause(mainPart));
////			NestedClause nested = new NestedClause();
////			nested.addClause(idlet);
////			nested.addClause(idRet);
////			s = nested.getXQuery();
//			s = mainPart;
//		}
//		
//		return s;
//	}
//
//	@Override
//	public NestedClause getXQueryClause() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	
//	
//}
