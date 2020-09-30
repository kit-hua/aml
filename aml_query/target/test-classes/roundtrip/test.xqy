
import module namespace caex215 = "http://ipr.kit.edu/caex" at "/Users/aris/Documents/repositories/ipr/aml/aml_query/src/main/resources/caex.xqy";

let $this := doc("/Users/aris/Documents/repositories/ipr/aml/aml_query/src/test/resources/roundtrip/data1.xml")/CAEXFile
let $other := doc("/Users/aris/Documents/repositories/ipr/aml/aml_query/src/test/resources/roundtrip/data2.xml")/CAEXFile
return(
  for $ieThis in $this/InternalElement
  return(
    (:if we can find the ie in the other file:)
    let $ieOther := $other/InternalElement[@ID=$ieThis/@ID]
    return (
      if ($ieOther)
      then (
        for $attributeThis in $ieThis/Attribute
        return (
          let $attributeOther := $ieOther/Attribute[@Name=$attributeThis/@Name]
          return (
            if ($attributeOther)
            (:Update:)
            then $attributeThis/Value = $attributeOther/Value
          )
        )   
      )
    )
  )
)