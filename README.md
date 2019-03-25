# parser-triples

Shaun D’Souza. Parser extraction of triples in unstructured text. arXiv preprint arXiv:1811.05768, 2018. url: https://arxiv.org/abs/1811.05768

* Steps to compile and run jar

* System requirements
	* Install Java JDK
	* Add jdk\bin to Windows PATH
```
javac -cp opennlp-tools-1.6.0.jar ParseKnowledgeNpVisited.java ParseKnowledgeNpVisitedMap.java -d .
jar cvf opennlp-parser-svo-new.jar ./opennlp

java -cp opennlp-parser-svo-new.jar;opennlp-tools-1.6.0.jar opennlp.tools.parser.ParseKnowledgeNpVisited -fun -pos head_rules < ie-parser.txt
```

* Expected output

```
Google is located in Mountain view
0       "Google"        "is located"    "in Mountain view"
Mountain view is in California
1       "Mountain view" "is"    "in California"
Google will acquire YouTube , announced the New York Times .
2       "Google"        "will acquire"  "YouTube"
2       "Google"        "announced"     "the New York Times"
Google and Apple are headquartered in California .
3       "Google and Apple"      "are headquartered"     "in California"

java -cp opennlp-parser-svo-new.jar;opennlp-tools-1.6.0.jar opennlp.tools.parser.ParseKnowledgeNpVisitedMap -fun -pos head_rules < ie-parser.txt
```
