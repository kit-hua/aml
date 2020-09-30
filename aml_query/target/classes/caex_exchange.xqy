module namespace caex215exchange = "http://ipr.kit.edu/caex/exchange";
import module namespace functx = "http://www.functx.com" at "./functx-1.0.xqy";

(: return a deep copy of  the element and all sub elements :)
declare function caex215exchange:copy($element as element()) as element() {
   element {node-name($element)}
      {$element/@*,
          for $child in $element/node()
              return
               if ($child instance of element())
                 then caex215exchange:copy($child)
                 else $child
      }
};

declare function caex215exchange:isProjectDefault ($node as node()) as xs:boolean {
  if($node instance of element () and (string(node-name($node))='ExternalInterface' 
      or string(node-name($node))='RoleRequirements' 
      or string(node-name($node))='SupportedRoleClass' 
      or string(node-name($node))='InternalLink'))
  then true()
  else false()
};