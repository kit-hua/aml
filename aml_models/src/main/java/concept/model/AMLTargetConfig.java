package concept.model;

import java.util.HashSet;
import java.util.Iterator;

import CAEX215.AttributeType;
import CAEX215.CAEX215Factory;
import CAEX215.CAEXObject;
import CAEX215.InstanceHierarchyType;
import CAEX215.util.AMLHelperFunctions;
import concept.util.ConceptWarning;


public class AMLTargetConfig implements IAMLConceptConfig{
	
	public enum ExchangeMode {
		COPY, 
		PROJECTION,
		NEW
	}
	
	public static final String CONFIG_TARGET = "targetConfig";
	public static final String CONFIG_MODE = "mode";
	public static final String CONFIG_MODE_COPY = "copy";
	public static final String CONFIG_MODE_PROJ = "projection";
	public static final String CONFIG_SOURCEID = "sourceID";
	
	
//	private HashSet<String> sourceID;
	private String sourceID;
	private ExchangeMode mode;
//	private InstanceHierarchyType sourceIh;
	
	public AMLTargetConfig () {
		// by default, mode = new: necessary because otherwise it is null and thus invalid for new target objects
		mode = ExchangeMode.NEW;
//		sourceID = new HashSet<String>();		
	}
	
	public boolean isValid (InstanceHierarchyType sourceIh) {
		if (mode == null) {
			return false;
		}
		
		if (!mode.equals(ExchangeMode.NEW) && sourceID.isEmpty()) {
			return false;
		}
				
//		for(String id : sourceID) {
//			if(!isSourceObjectValid(sourceIh, id)) {
//				return false;
//			}
//		}
		
		if(mode.equals(ExchangeMode.NEW))
			return true;
		
		if(!isSourceObjectValid(sourceIh, sourceID)) {
			return false;
		}
		
		return true;
	}
	
	// TODO: test if the source object can be found
	private boolean isSourceObjectValid (InstanceHierarchyType sourceIh, String sourceId) {
		
		if (AMLHelperFunctions.getObjectWithID(sourceIh, sourceId) == null)
			return false;
		
		return true;
	}
	
	public void setSource (InstanceHierarchyType sourceIh, String sourceId) {
		if(isSourceObjectValid(sourceIh, sourceId))
			this.sourceID = sourceId;
		else {
			ConceptWarning warning = new ConceptWarning(new Throwable().getStackTrace(), "invalid source object ID [" + sourceId + "]");
			warning.print();
		}
	}
	
//	public void addSource (InstanceHierarchyType sourceIh, String sourceId) {
//		if(isSourceObjectValid(sourceIh, sourceId))
//			this.sourceID.add(sourceId);
//		else {
//			ConceptWarning warning = new ConceptWarning(new Throwable().getStackTrace(), "invalid source object ID [" + sourceId + "]");
//			warning.print();
//		}			
//	}
	
	public String getSourceID() {
		return this.sourceID;
	}
	
//	/**
//	 * @return the sourceID
//	 */
//	public HashSet<String> getSourceID() {
//		return sourceID;
//	}
//	/**
//	 * @param sourceID the sourceID to set
//	 */
//	public void setSourceID(HashSet<String> sourceID) {
//		this.sourceID = sourceID;
//	}
	
	/**
	 * @return the mode
	 */
	public ExchangeMode getMode() {
		return mode;
	}
	/**
	 * @param mode the mode to set
	 */
	public void setMode(ExchangeMode mode) {
		this.mode = mode;
	}

	
	@Override
	public AttributeType toCAEXAttribute() {
		AttributeType amlConf = CAEX215Factory.eINSTANCE.createAttributeType();
		amlConf.setName(AMLTargetConfig.CONFIG_TARGET);		
		
		// set mode
		AttributeType attrMode = CAEX215Factory.eINSTANCE.createAttributeType();
		attrMode.setName(AMLTargetConfig.CONFIG_MODE);		
		if(this.mode.equals(AMLTargetConfig.ExchangeMode.COPY))
			attrMode.setValue(AMLHelperFunctions.toAMLAnyType(AMLTargetConfig.CONFIG_MODE_COPY));
		else if(this.mode.equals(AMLTargetConfig.ExchangeMode.PROJECTION))
			attrMode.setValue(AMLHelperFunctions.toAMLAnyType(AMLTargetConfig.CONFIG_MODE_PROJ));
		attrMode.setAttributeDataType("xs:string");
		amlConf.getAttribute().add(attrMode);
		
		// set source id
		AttributeType attrSourceID = CAEX215Factory.eINSTANCE.createAttributeType();
		attrSourceID.setName(AMLTargetConfig.CONFIG_SOURCEID);	
		if(sourceID != null)
			attrSourceID.setValue(AMLHelperFunctions.toAMLAnyType(this.sourceID));
		
//		Iterator<String> iter = this.sourceID.iterator();
//		if(this.sourceID.size() == 1) {
//			attrSourceID.setValue(AMLHelperFunctions.toAMLAnyType(iter.next()));
//		}
//		else {
//			int idx = 0;
//			for(String id : this.sourceID) {
//				AttributeType attrId = CAEX215Factory.eINSTANCE.createAttributeType();
//				attrId.setName("source" + idx);
//				attrId.setValue(AMLHelperFunctions.toAMLAnyType(iter.next()));
//				idx++;
//				attrSourceID.getAttribute().add(attrId);
//			}
//		}				
		attrSourceID.setAttributeDataType("xs:string");	
		amlConf.getAttribute().add(attrSourceID);

		return amlConf;
	}
	
	
	@Override
	public void fromCAEXAttribute(AttributeType attr) {
		// TODO Auto-generated method stub		
		for(AttributeType child : attr.getAttribute()) {
			if(child.getName().equalsIgnoreCase(AMLTargetConfig.CONFIG_MODE)) {
				
				String mode = AMLHelperFunctions.getAMLAttributeValue(child);
				if(mode!=null) {
					if(mode.equals(AMLTargetConfig.CONFIG_MODE_COPY))
						this.mode = AMLTargetConfig.ExchangeMode.COPY;
					else if (mode.equals(AMLTargetConfig.CONFIG_MODE_PROJ))
						this.mode = AMLTargetConfig.ExchangeMode.PROJECTION;
					else{
						ConceptWarning warning = new ConceptWarning(new Throwable().getStackTrace(), "invalid mode [" + mode + "]");
						warning.print();
					}
				}
									
			}
			
			else if(child.getName().equalsIgnoreCase(AMLTargetConfig.CONFIG_SOURCEID)) {
				
				if(child.getAttribute().isEmpty()) {
					String id = AMLHelperFunctions.getAMLAttributeValue(child);
					if(id!=null)
						// TODO: this might generate invalid config, if the id does not exist in source
						this.sourceID = id;
					else {
						ConceptWarning warning = new ConceptWarning(new Throwable().getStackTrace(), "empty source ID");
						warning.print();
					}						
				}
				
				// check if it is a singleton: then take its value
//				if(child.getAttribute().isEmpty()) {
//					String id = AMLHelperFunctions.getAMLAttributeValue(child);
//					if(id!=null)
//						// TODO: this might generate invalid config, if the id does not exist in source
//						this.sourceID.add(id);
//					else {
//						ConceptWarning warning = new ConceptWarning(new Throwable().getStackTrace(), "empty source ID");
//						warning.print();
//					}						
//				}
				
//				// if it is a nested one: all its children are ids
//				else {
//					for (AttributeType grandChild : child.getAttribute()) {
//						String id = AMLHelperFunctions.getAMLAttributeValue(grandChild);
//						if(id!=null)
//							// TODO: this might generate invalid config, if the id does not exist in source
//							this.sourceID.add(id);
//						else {
//							ConceptWarning warning = new ConceptWarning(new Throwable().getStackTrace(), "empty source ID");
//							warning.print();
//						}
//					}
//				}
								
			}
//			else
//				System.err.println("unknown query config parameter [" + child.getName() + "]!");
		}
	}

	@Override
	public boolean canBeFusedWith(IAMLConceptConfig other) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public String toString() {
		String s = "";
		
		// mode
		if(mode == ExchangeMode.NEW) {
			return " [" + mode + "]  ";
		}
		
		s += " [" + mode + ":";
		// source
		
//		for (String id : sourceID) {			
//			s += id + ", ";
//		}
		s += sourceID + ", ";
		
		
		s = s.substring(0, s.length()-2);
		
		s += "], ";
		
		return s;		
	}

}
