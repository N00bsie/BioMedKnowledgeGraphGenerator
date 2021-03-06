import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.print.attribute.HashAttributeSet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.XML;
import org.w3c.dom.*;

import com.sun.xml.internal.ws.util.xml.NodeListIterator;

import bean.ArgumentType;
import bean.Arguments;
import bean.Context;
import bean.EntityMention;
import bean.EventMention;
import bean.Facets;
import bean.FrameType;
import bean.ObjectMeta;
import bean.ObjectType;
import bean.Passage;
import bean.Position;
import bean.Sentence;
import bean.XRefs;

/**
 * 
 */

/**
 * @author Sabbir Rashid
 *
 */
public class ReachXml2Ttl extends ReachParseXml {
	
	//obtain read and write location from config.properties
	static PropertyRead mydirs = new PropertyRead();
	public static final String READ_LOCATION = mydirs.xmlFileDirectory;
	public static final String WRITE_LOCATION = mydirs.ttlFileDirectory;
	
	
	static Set<EntityMention> entity_mentions = new HashSet<EntityMention>();
	static Set<EventMention> event_mentions = new HashSet<EventMention>();
	static Set<Context> contexts = new HashSet<Context>();
	static Set<Sentence> sentences = new HashSet<Sentence>();
	static Set<Passage> passages = new HashSet<Passage>();
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		//Files.newDirectoryStream(Paths.get(READ_LOCATION),path -> path.toString().contains(".entities"))
		ReachXmlContentClass reachxmlcontent = ReachParseXml.parseReachXml(READ_LOCATION);
		entity_mentions = reachxmlcontent.entity_mentions;
		event_mentions = reachxmlcontent.event_mentions;
		contexts = reachxmlcontent.contexts;
		sentences = reachxmlcontent.sentences;
		passages = reachxmlcontent.passages;
		
		System.out.println("Printing Entity Mention TTL to file");
		String kgcs="http://tw.rpi.edu/web/Courses/Ontologies/2017/KGCS/KGCS/";

		String prefixes="@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> .\n" +
				"@prefix sio:     <http://semanticscience.org/resource/> .\n" + 
				"@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
				"@prefix kgcs: 	<" + kgcs + ">.\n" + 
				"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n"+ 
				"@prefix owl: <http://www.w3.org/2002/07/owl#> .\n" +
				"@prefix GO: <http://purl.obolibrary.org/obo/GO_#> .\n";
		
		
		try{
		    int counter = 0;
		    int index = 0;
		    PrintWriter writer = new PrintWriter(WRITE_LOCATION + "entity_mentions-" + index + ".ttl", "UTF-8");
		    writer.println(prefixes);
		    for(EntityMention entity_mention : entity_mentions) {
		    	if(counter == entity_mentions.size()/30){
		    		index++;
		    		writer.close();
		    		counter=0;
		    		writer = new PrintWriter(WRITE_LOCATION + "entity_mentions-" + index + ".ttl", "UTF-8");
		    		writer.println(prefixes);
		    	}
		    	System.out.println("Entity Mention: " + entity_mention.getFrameID());
		    	if(entity_mention.getXref()!=null) {
		    		if(entity_mention.getXref().getID().contains(":")){
		    			writer.println("<" + kgcs + entity_mention.getType() + "-" + entity_mention.getFrameID() + "> a <"+ entity_mention.getXref().getID() + "> ;");
		    			writer.println("\trdfs:label \"" + entity_mention.getText().replace("\"", "'").replace("\\", "/") + "\" ;");
		    			writer.println("\tprov:wasDerivedFrom " + "<" + kgcs + entity_mention.getFrameID() + "> .");
		    			writer.println("");
		    		} else {
		    			writer.println("<" + kgcs + entity_mention.getType() + "-" + entity_mention.getFrameID() + "> a <"+ entity_mention.getXref().getNamespace() + ":" + entity_mention.getXref().getID() + "> ;");
		    			writer.println("\trdfs:label \"" + entity_mention.getText().replace("\"", "'").replace("\\", "/") + "\" ;");
		    			writer.println("\tprov:wasDerivedFrom " + "<" + kgcs + entity_mention.getFrameID() + "> .");
		    			writer.println("");
		    		}
		    	}
		    	writer.println("<" + kgcs + entity_mention.getFrameID() + "> a <" + kgcs + "EntityMention> ;");
		    	writer.println("\trdfs:label \"" + entity_mention.getText().replace("\"", "'").replace("\\", "/") + "\" ;");
			    writer.println("\t<" + kgcs + "hasMentionType> \"" + entity_mention.getType() + "\" ;");
			    writer.println("\t<" + kgcs + "hasFrameType> <kgcs:Frame-" + entity_mention.getFrameType().toString() + "> ;");
			    if(entity_mention.getObjectMeta()!=null){
		    		writer.println("\t\tkgcs:hasMetaObjectComponent\t\"" + entity_mention.getObjectMeta().getComponent() + "\" ;");
		    		writer.println("\t\tkgcs:hasMetaObjectType\tkgcs:" + entity_mention.getObjectMeta().getObjectType().toString().toLowerCase().replaceAll("_","-") + " ;");
		    	}
			    writer.println("\t<" + kgcs + "hasObjectType> <kgcs:" + entity_mention.getObjectType().toString().toLowerCase().replaceAll("_","-") + "> ;");
			    writer.println("\t<" + kgcs + "hasStartPositionReference> <" + kgcs + entity_mention.getStartPos().getReference() + "> ;");
                writer.println("\t<" + kgcs + "hasStartPositionOffset> \"" + entity_mention.getStartPos().getOffset() + "\"^^xsd:integer ;");
                writer.println("\t<" + kgcs + "hasStartPositionObjectType> <kgcs:" + entity_mention.getStartPos().getObjectType().toString().toLowerCase().replaceAll("_","-") + "> ;");
			    writer.println("\t<" + kgcs + "hasEndPositionReference> <" + kgcs + entity_mention.getEndPos().getReference() + "> ;");
                writer.println("\t<" + kgcs + "hasEndPositionOffset> \"" + entity_mention.getEndPos().getOffset() + "\"^^xsd:integer ;");
                writer.println("\t<" + kgcs + "hasEndPositionObjectType> <kgcs:" + entity_mention.getEndPos().getObjectType().toString().toLowerCase().replaceAll("_","-") + "> ;");
                writer.println("\t<" + kgcs + "fromSentence> <" + kgcs + entity_mention.getSentenceID() + "> ;");
			    writer.println("\t<" + kgcs + "hasXrefID> \"" + entity_mention.getXref().getID() + "\" ;");
			    writer.println("\t<" + kgcs + "hasXrefNamespace> \"" + entity_mention.getXref().getNamespace() + "\" ;");
			    writer.println("\t<" + kgcs + "hasXrefObjectType> <kgcs:" + entity_mention.getXref().getObjectType().toString().toLowerCase().replaceAll("_","-") + "> .");
			    writer.println("");
			    counter++;
		    }
		    writer.close();
		} catch (IOException e) {
			   // do something
		}
		System.out.println("Printing Event Mention TTL to file");
		
		try{
			int counter = 0;
			int index = 0;
			PrintWriter writer = new PrintWriter(WRITE_LOCATION + "event_mentions-" + index + ".ttl", "UTF-8");
			writer.println(prefixes);
		    for(EventMention event_mention : event_mentions) {
		    	if(counter == event_mentions.size()/5){
		    		index++;
		    		writer.close();
		    		counter=0;
		    		writer = new PrintWriter(WRITE_LOCATION + "event_mentions-" + index + ".ttl", "UTF-8");
		    		writer.println(prefixes);
		    	}
				System.out.println("Event Mention: " + event_mention.getFrameID());
				writer.println("<" + kgcs + event_mention.getFrameID() + "> a <" + kgcs + "EventMention> ;");
				writer.println("\t<" + kgcs + "hasFrameType> <kgcs:Frame-" + event_mention.getFrameType().toString() + "> ;");
				if(event_mention.getObjectMeta()!=null){
		    		writer.println("\t\tkgcs:hasMetaObjectComponent\t\"" + event_mention.getObjectMeta().getComponent() + "\" ;");
		    		writer.println("\t\tkgcs:hasMetaObjectType\tkgcs:" + event_mention.getObjectMeta().getObjectType().toString().toLowerCase().replaceAll("_","-") + " ;");
		    	}
				writer.println("\t<" + kgcs + "hasObjectType> <kgcs:" + event_mention.getObjectType().toString().toLowerCase().replaceAll("_","-") + "> ;");
			    writer.println("\trdfs:label \"" + event_mention.getText().replace("\"", "'").replace("\\", "/") + "\" ;");
				writer.println("\trdfs:comment \"" + event_mention.getVerboseText().replace("\"", "'").replace("\\", "/") + "\" ;");
				writer.println("\t<" + kgcs + "hasMentionType> \"" + event_mention.getType() + "\" ;");
			    writer.println("\t<" + kgcs + "hasMentionSubType> \"" + event_mention.getSubType() + "\" ;");
			    writer.println("\t<" + kgcs + "hasStartPositionReference> <" + kgcs + event_mention.getStartPos().getReference() + "> ;");
                writer.println("\t<" + kgcs + "hasStartPositionOffset> \"" + event_mention.getStartPos().getOffset() + "\"^^xsd:integer ;");
                writer.println("\t<" + kgcs + "hasStartPositionObjectType> <kgcs:" + event_mention.getStartPos().getObjectType().toString().toLowerCase().replaceAll("_","-") + "> ;");
			    writer.println("\t<" + kgcs + "hasEndPositionReference> <" + kgcs + event_mention.getEndPos().getReference() + "> ;");
                writer.println("\t<" + kgcs + "hasEndPositionOffset> \"" + event_mention.getEndPos().getOffset() + "\"^^xsd:integer ;");
                writer.println("\t<" + kgcs + "hasEndPositionObjectType> <kgcs:" + event_mention.getEndPos().getObjectType().toString().toLowerCase().replaceAll("_","-") + "> ;");
                writer.println("\t<" + kgcs + "fromSentence> <" + kgcs + event_mention.getSentenceID() + "> ;");
			    writer.println("\t<" + kgcs + "hasContext> <" + kgcs + event_mention.getContextID() + "> ;");
			    writer.println("\t<" + kgcs + "foundBy> \"" + event_mention.getFoundBy() + "\" ;");
			    writer.println("\t<" + kgcs + "hasTrigger> \"" + event_mention.getTrigger() + "\" ;");
			    writer.println("\t<" + kgcs + "hasArgument> <kgcs:" + event_mention.getArguments().getArg() + "> ;");
			    writer.println("\t<" + kgcs + "hasArgumentObjectType> <kgcs:" + event_mention.getArguments().getObjectType().toString().toLowerCase().replaceAll("_","-") + "> ;");
			    writer.println("\t<" + kgcs + "hasArgumentIndex> " + event_mention.getArguments().getIndex() + " ;");
			    writer.println("\t<" + kgcs + "hasArgumentArgumentType> <kgcs-Argument:" + event_mention.getArguments().getArgumentType() + "> ;");
			    writer.println("\t<" + kgcs + "hasArgumentType> \"" + event_mention.getArguments().getType() + "\" ;");
			    writer.println("\t<" + kgcs + "hasArgumentLabel> \"" + event_mention.getArguments().getText().replace("\"", "'").replace("\\", "/") + "\" ;");
			    writer.println("\t<" + kgcs + "boolIsDirect> \"" + event_mention.getIsDirect() + "\" ;");
			    writer.println("\t<" + kgcs + "boolIsHypothesis> \"" + event_mention.getIsHypothesis()+ "\" .");
			    writer.println("");
			    counter++;
		    }
		    writer.close();
		} catch (IOException e) {
			   // do something
		}
		
		try{
		    PrintWriter writer = new PrintWriter(WRITE_LOCATION + "contexts.ttl", "UTF-8");
		    writer.println(prefixes);
		    for(Context context : contexts) {
				System.out.println("Context: " + context.getFrameID());
				writer.println("<" + kgcs + context.getFrameID() + "> a <" + kgcs + "Context> ;");
				writer.println("\t<" + kgcs + "hasFrameType> <kgcs:Frame-" + context.getFrameType().toString() + "> ;");
				writer.println("\t<" + kgcs + "hasScope> <" + kgcs + context.getScopeID() + "> ;");
			    if(context.getFacets()!=null){
			    	writer.println("\t<" + kgcs + "hasFacetObjectType> <kgcs:" + context.getFacets().getObjectType().toString().toLowerCase().replaceAll("_","-") + "> ;");
			    	if(context.getFacets().getLocation()!=null)
			    		writer.println("\t<" + kgcs + "hasFacetLocation> \"" + context.getFacets().getLocation() + "\" ;");
			    	if(context.getFacets().getCellLine()!=null)
			    		writer.println("\t<" + kgcs + "hasFacetCellLine> \"" + context.getFacets().getCellLine() + "\" ;");
			    	if(context.getFacets().getOrganism()!=null)
			    		writer.println("\t<" + kgcs + "hasFacetOrganism> \"" + context.getFacets().getOrganism() + "\" ;");
			    	if(context.getFacets().getTissueType()!=null)
			    		writer.println("\t<" + kgcs + "hasFacetTissueType> \"" + context.getFacets().getTissueType() + "\" ;");
			    }
				writer.println("\t<" + kgcs + "hasObjectType> <kgcs:" + context.getObjectType().toString().toLowerCase().replaceAll("_","-") + "> .");
			    writer.println("");
		    }
		    writer.close();
		} catch (IOException e) {
			   // do something		
		}
		
		try{
			int counter = 0;
			int index = 0;
		    PrintWriter writer = new PrintWriter(WRITE_LOCATION + "sentences-" + index + ".ttl", "UTF-8");
		    writer.println(prefixes);
		    for(Sentence sentence : sentences) {
		    	if(counter == sentences.size()/20){
		    		index++;
		    		writer.close();
		    		counter=0;
		    		writer = new PrintWriter(WRITE_LOCATION + "sentences-" + index + ".ttl", "UTF-8");
		    		writer.println(prefixes);
		    	}
		    	System.out.println("Sentence: " + sentence.getFrameID());
				writer.println("<" + kgcs + sentence.getFrameID() + "> a <" + kgcs + "Sentence> ;");
				writer.println("\t<" + kgcs + "hasFrameType> <kgcs:Frame-" + sentence.getFrameType().toString() + "> ;");
				writer.println("\t<" + kgcs + "fromPassage> <" + kgcs + sentence.getPassageID() + "> ;");
		    	writer.println("\t<" + kgcs + "hasContent> \"" + sentence.getText().replace("\"", "'").replace("\\", "/") + "\" ;");
		    	if(sentence.getObjectMeta()!=null){
		    		writer.println("\t<" + kgcs + "hasMetaObjectComponent> \"" + sentence.getObjectMeta().getComponent() + "\" ;");
		    		writer.println("\t<" + kgcs + "hasMetaObjectType> <kgcs:" + sentence.getObjectMeta().getObjectType().toString().toLowerCase().replaceAll("_","-") + "> ;");
		    	}
		    	writer.println("\t<" + kgcs + "hasStartPositionReference> <" + kgcs + sentence.getStartPos().getReference() + "> ;");
                writer.println("\t<" + kgcs + "hasStartPositionOffset> \"" + sentence.getStartPos().getOffset() + "\"^^xsd:integer ;");
                writer.println("\t<" + kgcs + "hasStartPositionObjectType> <kgcs:" + sentence.getStartPos().getObjectType().toString().toLowerCase().replaceAll("_","-") + "> ;");
			    writer.println("\t<" + kgcs + "hasEndPositionReference> <" + kgcs + sentence.getEndPos().getReference() + "> ;");
                writer.println("\t<" + kgcs + "hasEndPositionOffset> \"" + sentence.getEndPos().getOffset() + "\"^^xsd:integer ;");
                writer.println("\t<" + kgcs + "hasEndPositionObjectType> <kgcs:" + sentence.getEndPos().getObjectType().toString().toLowerCase().replaceAll("_","-") + "> ;");
                writer.println("\t<" + kgcs + "hasObjectType> <kgcs:" + sentence.getObjectType().toString().toLowerCase().replaceAll("_","-") + "> .");
			    writer.println("");
			    counter++;
			    }
		    writer.close();
		} catch (IOException e) {
		   // do something		
		}    
		
		try{
			int counter = 0;
			int index = 0;
		    PrintWriter writer = new PrintWriter(WRITE_LOCATION + "passages-" + index + ".ttl", "UTF-8");
		    writer.println(prefixes);
		    for(Passage passage : passages) {
		    	if(counter == passages.size()/3){
		    		index++;
		    		writer.close();
		    		counter=0;
		    		writer = new PrintWriter(WRITE_LOCATION + "passages-" + index + ".ttl", "UTF-8");
		    		writer.println(prefixes);
		    	}
		    	System.out.println("Passage: " + passage.getFrameID());
		    	writer.println("<" + kgcs + passage.getFrameID() + "> a <" + kgcs + "Passage> ;");
		    	writer.println("\t<" + kgcs + "hasFrameType> <kgcs:Frame-" + passage.getFrameType().toString() + "> ;");
				writer.println("\t<" + kgcs + "hasIndex> \"" + passage.getIndex() + "\"^^xsd:integer ;");
		    	writer.println("\t<" + kgcs + "hasSectionID> \"" + passage.getSectionID()  + "\" ;");
		    	writer.println("\t<" + kgcs + "hasSectionName> \"" + passage.getSectionName() + "\" ;");
		    	writer.println("\t<" + kgcs + "hasContent> \"" + passage.getText().replace("\"", "'").replace("\\", "/")+ "\" ;");
		    	if(passage.getObjectMeta()!=null){
		    		writer.println("\t<" + kgcs + "hasMetaObjectComponent> \"" + passage.getObjectMeta().getComponent() + "\" ;");
		    		writer.println("\t<" + kgcs + "hasMetaObjectType> <kgcs:" + passage.getObjectMeta().getObjectType().toString().toLowerCase().replaceAll("_","-") + "> ;");
		    	}
		    	writer.println("\t<" + kgcs + "hasObjectType> <kgcs:" + passage.getObjectType().toString().toLowerCase().replaceAll("_","-") + "> ;");
			    writer.println("\t<" + kgcs + "boolIsTitle> \"" + passage.getIsTitle() + "\" .");
			    writer.println("");
			    counter++;
		    }
		    writer.close();
		} catch (IOException e) {
		   // do something		
		}
		
/*		System.out.println("Assigning Passages to Sentences");
		for(Sentence sentence : sentences) {
			if(sentence.getPassageID()!=null){
				System.out.println("Searching for: " + sentence.getPassageID());
				for (Passage passage : passages){
					if(passage.getFrameID().equals(sentence.getPassageID())){
						System.out.println("Found " + passage.getFrameID());
						sentence.setPassage(passage);
						break;
					}
				}
			}
		}
		
		System.out.println("Assigning Sentences to Contexts");
		for(Context context : contexts) {
			if(context.getScopeID()!=null){
				System.out.println("Searching for: " + context.getScopeID());
				for (Sentence sentence : sentences){
					if(sentence.getFrameID().equals(context.getScopeID())){
						System.out.println("Found " + sentence.getFrameID());
						context.setScope(sentence);
						break;
					}
				}
			}
		}
		
		System.out.println("Assigning Sentences to Entity Mentions");
		for(EntityMention entity_mention : entity_mentions) {
			if(entity_mention.getSentenceID()!=null){
				System.out.println("Searching for: " + entity_mention.getSentenceID());
				for (Sentence sentence : sentences){
					if(sentence.getFrameID().equals(entity_mention.getSentenceID())){
						System.out.println("Found " + sentence.getFrameID());
						entity_mention.setSentence(sentence);
						break;
					}
				}
			}
		}
		
		System.out.println("Assigning Sentences and Contexts to Event Mentions");
		for(EventMention event_mention : event_mentions) {
			if(event_mention.getSentenceID()!=null){
				System.out.println("Searching for: " + event_mention.getSentenceID());
				for (Sentence sentence : sentences){
					if(sentence.getFrameID().equals(event_mention.getSentenceID())){
						System.out.println("Found " + sentence.getFrameID());
						event_mention.setSentence(sentence);
						break;
					}
				}
				System.out.println(": " + event_mention.getSentenceID());
			}
			if(event_mention.getContextID()!=null){
				System.out.println("Searching for: " + event_mention.getContextID());
				for (Context context : contexts){
					if(context.getFrameID().equals(event_mention.getContextID())){
						System.out.println("Found " + context.getFrameID());
						event_mention.setContext(context);
						break;
					}
				}
				System.out.println(": " + event_mention.getContextID());
				
			}
		}
		
		System.out.println("Printing txt to file");
		try{
		    PrintWriter writer = new PrintWriter(WRITE_LOCATION + "entity_mentions.txt", "UTF-8");
		    for(EntityMention entity_mention : entity_mentions) {
		    	System.out.println("Entity Mention: " + entity_mention.getFrameID());
			    writer.println("Frame ID: " + entity_mention.getFrameID());
			    writer.println("Frame Type: " + entity_mention.getFrameType().toString());
			    writer.println("Object Type: " + entity_mention.getObjectType().toString());
			    writer.println("Text: " + entity_mention.getText());
			    writer.println("Type: " + entity_mention.getType());
			    writer.println("Start Position: " + entity_mention.getStartPos());
			    writer.println("\tOffset: " + entity_mention.getStartPos().getOffset());
			    writer.println("\tReference: " + entity_mention.getStartPos().getReference());
			    writer.println("\tObject Type: " + entity_mention.getStartPos().getObjectType().toString());
			    writer.println("End Position: " + entity_mention.getEndPos());
			    writer.println("\tOffset: " + entity_mention.getEndPos().getOffset());
			    writer.println("\tReference: " + entity_mention.getEndPos().getReference());
			    writer.println("\tObject Type: " + entity_mention.getEndPos().getObjectType().toString());
			    writer.println("Sentence ID: " + entity_mention.getSentenceID());
			    writer.println("Xref: " + entity_mention.getXref());
			    writer.println("\tID: " + entity_mention.getXref().getID());
			    writer.println("\tNamespace: " + entity_mention.getXref().getNamespace());
			    writer.println("\tObject Type: " + entity_mention.getXref().getObjectType().toString());
			    writer.println("");
		    }
		    writer.close();
		} catch (IOException e) {
			   // do something
		}
		
		try{
		    PrintWriter writer = new PrintWriter(WRITE_LOCATION + "event_mentions.txt", "UTF-8");
		    for(EventMention event_mention : event_mentions) {
				System.out.println("Event Mention: " + event_mention.getFrameID());
			    writer.println("Frame ID: " + event_mention.getFrameID());
			    writer.println("Frame Type: " + event_mention.getFrameType().toString());
			    writer.println("Text: " + event_mention.getText());
			    writer.println("Verbose Text: " + event_mention.getVerboseText());
			    writer.println("Type: " + event_mention.getType());
			    writer.println("SubType: " + event_mention.getSubType());
			    writer.println("Start Position: " + event_mention.getStartPos());
			    writer.println("\tOffset: " + event_mention.getStartPos().getOffset());
			    writer.println("\tReference: " + event_mention.getStartPos().getReference());
			    writer.println("\tObject Type: " + event_mention.getStartPos().getObjectType().toString());
			    writer.println("End Position: " + event_mention.getEndPos());
			    writer.println("\tOffset: " + event_mention.getEndPos().getOffset());
			    writer.println("\tReference: " + event_mention.getEndPos().getReference());
			    writer.println("\tObject Type: " + event_mention.getEndPos().getObjectType().toString());
			    writer.println("Object Type: " + event_mention.getObjectType().toString());
			    writer.println("Sentence ID: " + event_mention.getSentenceID());
			    //writer.println("\tFrame ID: " + event_mention.getSentence().getFrameID());
			    //writer.println("\tPassage ID: " + event_mention.getSentence().getPassageID());
			    //writer.println("\tText: " + event_mention.getSentence().getText());
			    //writer.println("\tFrameType: " + event_mention.getSentence().getFrameType());
			    writer.println("Context ID: " + event_mention.getContextID());
			    writer.println("Context:" + event_mention.getContext());
			    writer.println("Found By: " + event_mention.getFoundBy());
			    writer.println("Trigger: " + event_mention.getTrigger());
			    writer.println("Arguments: " + event_mention.getArguments());
			    writer.println(event_mention.getArguments().getElements());
			    writer.println("Is Direct?: " + event_mention.getIsDirect());
			    writer.println("Is Hypothesis?: " + event_mention.getIsHypothesis());
			    writer.println("");
		    }
		    writer.close();
		} catch (IOException e) {
			   // do something
		}
		
		try{
		    PrintWriter writer = new PrintWriter(WRITE_LOCATION + "contexts.txt", "UTF-8");
		    for(Context context : contexts) {
				System.out.println("Context: " + context.getFrameID());
			    writer.println("Frame ID: " + context.getFrameID());
			    writer.println("Frame Type: " + context.getFrameType().toString());
			    writer.println("Scope ID: " + context.getScopeID());
			    writer.println("Scope: " + context.getScope());
			    writer.println("Object Type: " + context.getObjectType().toString());
			    writer.println("Facets: " + context.getFacets());
			    writer.println(context.getFacets().getElements());
			    writer.println("");
		    }
		    writer.close();
		} catch (IOException e) {
			   // do something		
		}
		try{
		    PrintWriter writer = new PrintWriter(WRITE_LOCATION + "sentences.txt", "UTF-8");
		    for(Sentence sentence : sentences) {
		    	System.out.println("Sentence: " + sentence.getFrameID());
		    	writer.println("Frame ID: " + sentence.getFrameID());
		    	writer.println("Frame Type: " + sentence.getFrameType());
		    	writer.println("Passage ID: " + sentence.getPassageID());
		    	writer.println("Passage: " + sentence.getPassage());
		    	writer.println("Text: " + sentence.getText());
		    	writer.println("Object Meta: " + sentence.getObjectMeta());
		    	writer.println("Object Type: " + sentence.getObjectType());
		    	writer.println("Start Position: " + sentence.getStartPos());
			    writer.println("\tOffset: " + sentence.getStartPos().getOffset());
			    writer.println("\tReference: " + sentence.getStartPos().getReference());
			    writer.println("\tObject Type: " + sentence.getStartPos().getObjectType().toString());
			    writer.println("End Position: " + sentence.getEndPos());
			    writer.println("\tOffset: " + sentence.getEndPos().getOffset());
			    writer.println("\tReference: " + sentence.getEndPos().getReference());
			    writer.println("\tObject Type: " + sentence.getEndPos().getObjectType().toString());
		    }
		    writer.close();
		} catch (IOException e) {
		   // do something		
		}    
		
		try{
		    PrintWriter writer = new PrintWriter(WRITE_LOCATION + "passages.txt", "UTF-8");
		    for(Passage passage : passages) {
		    	System.out.println("Passage: " + passage.getFrameID());
		    	writer.println("Frame ID: " + passage.getFrameID());
		    	writer.println("Frame Type: " + passage.getFrameType());
		    	writer.println("Index: " + passage.getIndex());
		    	writer.println("Section ID: " + passage.getSectionID());
		    	writer.println("Section Name: " + passage.getSectionName());
		    	writer.println("Text: " + passage.getText());
		    	writer.println("Object Type: " + passage.getObjectType());
		    	writer.println("Object Meta: " + passage.getObjectMeta());
		    	writer.println("Is Title?: " + passage.getIsTitle());
		    }
		    writer.close();
		} catch (IOException e) {
		   // do something		
		}
		*/
		System.out.println("Done");
	}
}
