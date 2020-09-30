import module namespace caex215 = "http://ipr.kit.edu/caex" at"/Users/aris/Documents/repositories/ipr/aml/aml_query/src/main/resources/caex.xqy";
import module namespace functx = "http://www.functx.com" at "/Users/aris/Documents/repositories/ipr/aml/aml_query/src/main/resources/functx-1.0.xqy";

let $root := doc("data.aml")/CAEXFile/InstanceHierarchy
return (

for $n0 in $root//InternalElement
	for $n4 in $n0/InternalElement[RoleRequirements[@RefBaseRoleClassPath="AutomationMLExtendedRoleClassLib/Clamp"]], $n1 in $n0/InternalElement[RoleRequirements[@RefBaseRoleClassPath="AutomationMLBaseRoleClassLib/AutomationMLBaseRole/Structure"]]
	return (
		for $n6 in $n4/InternalElement[RoleRequirements[@RefBaseRoleClassPath="AutomationMLBaseRoleClassLib/AutomationMLBaseRole/Structure"]], $n5 in $n4/InternalElement[RoleRequirements[@RefBaseRoleClassPath="AutomationMLDMIRoleClassLib/DiscManufacturingEquipment/Robot"]]
		return (<n1>{
			$n5,
			$n6
      }</n1>
		),
		for $n2 in $n1/InternalElement[RoleRequirements[@RefBaseRoleClassPath="AutomationMLDMIRoleClassLib/DiscManufacturingEquipment/Robot"]], $n3 in $n1/InternalElement[RoleRequirements[@RefBaseRoleClassPath="AutomationMLBaseRoleClassLib/AutomationMLBaseRole/Structure"]]
		return (<n2>{
			$n2,
			$n3
      }</n2>
		)
	)


)

(:/Attribute[@Name="Frame"]/Attribute[@Name="x"]:)

(:
let $root := doc("RobotCell.aml")/CAEXFile
let $candidates := $root//ExternalInterface[caex215:refsIC("AttachmentInterface","AutomationMLInterfaceClassLib", .)]
for $ei in doc("RobotCell.aml")/CAEXFile/InstanceHierarchy//InternalElement/ExternalInterface
let $connections := caex215:getConnections($ei)
  return 
    if (count(functx:value-intersect($connections/partner/iid, $candidates/@ID))>0)
    then $ei/@Name
:)

(:return doc("RobotCell.aml")/CAEXFile/InstanceHierarchy//InternalElement/ExternalInterface[caex215:connectsToAny(., $root//ExternalInterface[caex215:refsIC("AttachmentInterface","AutomationMLInterfaceClassLib", .)])]:)


  (:
    count($links[link/partner/oname=doc("RobotCell.aml")/CAEXFile/InstanceHierarchy//ExternalInterface[caex215:refsIC("AttachmentInterface","AutomationMLInterfaceClassLib", .)]/@Name] 
      or $links[link/partner/oid=doc("RobotCell.aml")/CAEXFile/InstanceHierarchy//ExternalInterface[caex215:refsIC("AttachmentInterface","AutomationMLInterfaceClassLib", .)]/@ID]
      or $links[link/partner/iname=doc("RobotCell.aml")/CAEXFile/InstanceHierarchy//ExternalInterface[caex215:refsIC("AttachmentInterface","AutomationMLInterfaceClassLib", .)]/@Name]
      or $links[link/partner/iid=doc("RobotCell.aml")/CAEXFile/InstanceHierarchy//ExternalInterface[caex215:refsIC("AttachmentInterface","AutomationMLInterfaceClassLib", .)]/@ID])>0:)


(:return caex215:connectsTo($ei, ExternalInterface):)




(:for $ie in /CAEXFile/InstanceHierarchy/InternalElement[@ID="1"]/InternalElement
return (
  $ie/ExternalInterface/@ID, $ie/InternalElement/@ID
):)

(:
for $ie in /CAEXFile/InstanceHierarchy/InternalElement[@ID="1"]/InternalElement
return (
  for $ei in $ie/ExternalInterface, $ie2 in $ie/InternalElement
  return(
    $ie2/@ID
  )
)
:)

(:
for $n0 in A
for $n1 in $n0/B[D/H], $n2 in $n0/K
return(
    for $n3 in $n1/C[F], $n4 in $n1/E[I]
    return(
      $n3/G, $n4/J
    ),
    for $n5 in $n2/L
    return(
      $n5
    )
):)
(:
for $n0 in A
for $n1 in $n0/B[D/H], $n2 in $n0/K
return(
    for $n3 in $n1/C[F]
    return(
      $n3/G
    ),
    for $n4 in $n1/E[I]
    return(
      $n4/J
    )
):)

(:
for $n0 in A
  for $n1 in $n0/B[D/H], $n2 in $n0/K
    for $n3 in $n1/C[F], $n4 in $n1/E[I]
    return(
      $n3/G,
      $n4/J
    )
:)