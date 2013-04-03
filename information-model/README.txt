The main class responsible for generating Java classes is :
	eu.novi.imgen.Generator

Located in :
	src/main/java/eu/novi/imgen/Generator.java

There is maven profile called 'generate' that can be used to create the new java classes from the owl file. 
This profile can be called as :
 
	mvn -P generate-core (for generating novi-im core)
	mvn -P generate-policy (for generating policy im)

The owl file is located in information-model/novi-im.owl

The generated classes will be created under directory src/main/java/eu/novi/im
	
git log


Troubleshouting.
if you have the error:
Caused by: org.openrdf.rio.RDFParseException: Content is not allowed in prolog. [line 1, column 1] in http://www.w3.org/2002/07/owl [line 1, column 1]
then remove the 
    <owl:Ontology rdf:about="http://fp7-novi.eu/unit.owl">
        <owl:imports rdf:resource="http://www.w3.org/2002/07/owl"/>
    </owl:Ontology>
    
  from the unit.owl