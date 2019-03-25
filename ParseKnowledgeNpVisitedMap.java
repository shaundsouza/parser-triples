package opennlp.tools.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data structure for holding ParseKnowledgeNpVisitedMap constituents. Updated
 * implementation of visited collection using Map. Higher precision and higher
 * recall.
 */
public class ParseKnowledgeNpVisitedMap {

	/**
	 * The sentence index.
	 */
	public static int ki = 0;

	/**
	 * Read subject noun phrase.
	 *
	 * @param p
	 *            Parse tree.
	 * @param levels
	 *            Tree depth.
	 * @param sent
	 *            Sentence root node.
	 */
	private void subjectNounPhrase(Parse p, int levels, Parse sent) {
		Parse[] kids = p.getChildren();
		StringBuilder levelsBuff = new StringBuilder();

		if (p.getType().equals("TK")) {
			return;
		}

		for (int li = 0; li < levels; li++) {
			levelsBuff.append("  ");
		}

		//System.out.println(levelsBuff.toString() + kids.length + " " + p.getType() + " " + p.getCoveredText());

		String subject = "";

		for (int i = 0; i < kids.length; i++) {
			if (kids[i].getType().startsWith("NP")) {
				for (int j = i + 1; j < kids.length; j++) {
					if (kids[j].getType().equals("VP") || kids[j].getType().equals("PP") || kids[j].getType().equals("SBAR")) {
						Map<Parse, String> visited = new HashMap<Parse, String>();
						int iter = 0;
						while (!(visited.containsKey(kids[j]) && (visited.get(kids[j]).contains("predicate")))) {
							String predicate = predicateVerbPhrase(kids[j], sent, visited).trim();
							if (!(predicate.isEmpty() || predicate.contains("ERR"))) {
								System.out.print(ki + "\t\"" + kids[i]);
								System.out.println("\"\t\"" + predicate);
							}
						}
					} else if (!(kids[j].getType().equals(",") || kids[j].getType().equals("CC"))) {
						break;
					}
				}
			}
			subjectNounPhrase(kids[i], levels + 1, sent);
		}
	}

	private List<String> subjectNounPhrase1(Parse p, int levels, Parse sent) {
		Parse[] kids = p.getChildren();
		StringBuilder levelsBuff = new StringBuilder();
		List<String> svo = new ArrayList<String>();

		if (p.getType().equals("TK")) {
			return svo;
		}

		for (int li = 0; li < levels; li++) {
			levelsBuff.append("  ");
		}

		//System.out.println(levelsBuff.toString() + kids.length + " " + p.getType() + " " + p.getCoveredText());

		String subject = "";

		for (int i = 0; i < kids.length; i++) {
			if (kids[i].getType().startsWith("NP")) {
				for (int j = i + 1; j < kids.length; j++) {
					if (kids[j].getType().equals("VP") || kids[j].getType().equals("PP") || kids[j].getType().equals("SBAR")) {
						Map<Parse, String> visited = new HashMap<Parse, String>();
						int iter = 0;
						while (!(visited.containsKey(kids[j]) && (visited.get(kids[j]).contains("predicate")))) {
							String predicate = predicateVerbPhrase(kids[j], sent, visited).trim();
							if (!(predicate.isEmpty() || predicate.contains("ERR"))) {
								svo.add("\"" + kids[i] + "\"\t\"" + predicate);
							}
						}
					} else if (!(kids[j].getType().equals(",") || kids[j].getType().equals("CC"))) {
						break;
					}
				}
			}
			svo.addAll(subjectNounPhrase1(kids[i], levels + 1, sent));
		}
		return svo;
	}

	/**
	 * Read predicate verb phrase.
	 *
	 * @param p
	 *            Parse tree.
	 * @param sent
	 *            Sentence root node.
	 * @param visited
	 *            Visited tree nodes.
	 * @return predicate object string.
	 */
	private String predicateVerbPhrase(Parse p, Parse sent, Map<Parse, String> visited) {
		Parse[] kids = p.getChildren();

		String predicate = "";

		for (int i = 0; i < kids.length; i++) {
			if (kids[i].getType().equals("VP") || kids[i].getType().equals("S")) {
				if (!(visited.containsKey(kids[i]) && (visited.get(kids[i]).contains("predicate")))) {
					return predicate.concat(" " + predicateVerbPhrase(kids[i], sent, visited).trim());
				}
			} else if (kids[i].getType().startsWith("VB") || kids[i].getType().startsWith("JJ") || kids[i].getType().startsWith("RB") || kids[i].getType().equals("MD")
					|| kids[i].getType().equals("ADVP") || kids[i].getType().equals("DT") || kids[i].getType().startsWith("NN") || kids[i].getType().equals("TO")
					|| ((predicate.length() > 0) && (kids[i].getType().equals("IN")))) {
				predicate = predicate.concat(" " + kids[i].getCoveredText());
				String object = "";

				for (int j = i + 1; j < kids.length; j++) {
					if ((kids[j].getType().startsWith("NP") || kids[j].getType().equals("PP") || kids[j].getType().equals("ADJP") || kids[j].getType().equals("S") || kids[j].getType().equals("SBAR"))) {

						if (visited.containsKey(kids[j]) && (visited.get(kids[j]).contains("object"))) {
							object = object.concat(" " + kids[j].getCoveredText());
						} else {
							object = object.concat(" " + objectNounPhrase(kids[j], sent, visited).trim());

							if (!object.isEmpty()) {
								return predicate.concat("\"\t\"" + object.trim() + "\"");
							}
						}
					} else if (kids[j].getType().equals(",") || kids[j].getType().equals("CC")) {
						object = "";
					} else {
						break;
					}
				}
			} else if (kids[i].getType().equals(",") || kids[i].getType().equals("CC")) {
				predicate = "";
			} else if (!kids[i].getType().equals("WHNP")) {
				break;
			}
		}

		visited.put(p, "predicate object");

		return "ERR";
	}

	/**
	 * Read object noun phrase.
	 *
	 * @param p
	 *            Parse tree.
	 * @param sent
	 *            Sentence root node.
	 * @param visited
	 *            Visited tree nodes.
	 * @return object noun string.
	 */
	private String objectNounPhrase(Parse p, Parse sent, Map<Parse, String> visited) {
		Parse[] kids = p.getChildren();
		boolean found = false;
		String object = "";

		for (int i = 0; i < kids.length; i++) {
			if (kids[i].getType().equals("IN") || kids[i].getType().equals("TO")) {
				object = object.concat(" " + kids[i].getCoveredText());
			} else if ((kids[i].getType().startsWith("NP") || kids[i].getType().equals("S"))) {
				found = true;

				if (visited.containsKey(kids[i]) && (visited.get(kids[i]).contains("object"))) {
					object = object.concat(" " + kids[i].getCoveredText());
				} else {
					return object.concat(" " + objectNounPhrase(kids[i], sent, visited).trim());
				}
			} else if (kids[i].getType().equals("PP")) {
				if (visited.containsKey(kids[i]) && (visited.get(kids[i]).contains("object"))) {
					object = object.concat(" " + kids[i].getCoveredText());
				} else {
					return object.concat(" " + objectPrepositionPhrase(kids[i], sent, visited).trim());
				}
			} else if (kids[i].getType().equals(",") || kids[i].getType().equals("CC")) {
				object = "";
			} else {
				break;
			}
		}

		visited.put(p, "object");

		if (!found && p.getType().startsWith("NP")) {
			return object.concat(" " + p.getCoveredText());
		}

		return "ERR";
	}

	/**
	 * Read object trailing preposition phrase.
	 *
	 * @param p
	 *            Parse tree.
	 * @param sent
	 *            Sentence root node.
	 * @param visited
	 *            Visited tree nodes.
	 * @return object preposition string.
	 */
	private String objectPrepositionPhrase(Parse p, Parse sent, Map<Parse, String> visited) {
		Parse[] kids = p.getChildren();
		String preposition = "";

		for (int i = 0; i < kids.length; i++) {
			if (kids[i].getType().startsWith("NP") && !(visited.containsKey(kids[i]) && (visited.get(kids[i]).contains("object")))) {
				return preposition.concat(" " + objectNounPhrase(kids[i], sent, visited).trim());
			}
			if (kids[i].getType().equals("PP") && !(visited.containsKey(kids[i]) && (visited.get(kids[i]).contains("object")))) {
				return preposition.concat(" " + objectPrepositionPhrase(kids[i], sent, visited).trim());
			} else if (kids[i].getType().equals("IN") || kids[i].getType().equals("TO") || kids[i].getType().equals("JJ") || kids[i].getType().equals("ADVP")) {
				preposition = preposition.concat(" " + kids[i].getCoveredText());
			} else {
				break;
			}
		}

		visited.put(p, "object");

		return "ERR";
	}

	/**
	 * Debug code tree.
	 *
	 * @param p
	 *            Parse tree.
	 * @param levels
	 *            Tree depth.
	 */
	private void codeTree(Parse p, int levels) {
		Parse[] kids = p.getChildren();
		StringBuilder levelsBuff = new StringBuilder();

		if (p.getType().equals("TK")) {
			return;
		}

		for (int li = 0; li < levels; li++) {
			levelsBuff.append("  ");
		}

		System.out.println(levelsBuff.toString() + p.getType() + " " + p.getCoveredText());

		for (int ki = 0; ki < kids.length; ki++) {
			codeTree(kids[ki], levels + 1);
		}
	}

	/**
	 * Show code tree knowledge.
	 *
	 * @param p
	 *            Parse tree.
	 */
	public void showCodeTree_knowledge(Parse parse) {
		subjectNounPhrase(parse, 0, parse);
	}

	public void showCodeTree_knowledge1(Parse parse) {
		List<String> svo = subjectNounPhrase1(parse, 0, parse);
		for (String s : svo)
			System.out.println(ki + "\t" + s);
	}

	/**
	 * Reads training parses (one-sentence-per-line) and displays
	 * ParseKnowledgeNpVisitedMap structure.
	 *
	 * @param args
	 *            The head rules files.
	 *
	 * @throws IOException
	 *             If the head rules file can not be opened and read.
	 */
	@Deprecated
	public static void main(String[] args) throws java.io.IOException {
		if (args.length == 0) {
			System.err.println("Usage: ParseKnowledgeNpVisitedMap -fun -pos head_rules < train_parses");
			System.err.println("Reads training parses (one-sentence-per-line) and displays ParseKnowledgeNpVisitedMap structure.");
			System.exit(1);
		}
		int ai = 0;
		boolean fixPossesives = false;
		int count = 0;
		while (args[ai].startsWith("-") && ai < args.length) {
			if (args[ai].equals("-fun")) {
				Parse.useFunctionTags(true);
				ai++;
			} else if (args[ai].equals("-pos")) {
				fixPossesives = true;
				ai++;
			}
		}

//		opennlp.tools.parser.lang.en.HeadRules rules = new opennlp.tools.parser.lang.en.HeadRules(args[ai]);
		java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));

		for (String line = in.readLine(); line != null; line = in.readLine()) {
			Parse p = Parse.parseParse(line, null);
			ParseKnowledgeNpVisitedMap pKnowledge = new ParseKnowledgeNpVisitedMap();
			Parse.pruneParse(p);
			if (fixPossesives) {
				Parse.fixPossesives(p);
			}
			// p.updateHeads(rules);
			// p.show();
			System.out.println(p.getCoveredText());
			pKnowledge.showCodeTree_knowledge(p);

			ki++;
		}
	}
}