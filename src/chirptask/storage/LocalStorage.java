package chirptask.storage;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class LocalStorage implements Storage {
	File local;
	DocumentBuilderFactory docBuilderFact = DocumentBuilderFactory.newInstance();
	TransformerFactory transFact = TransformerFactory.newInstance();
	
	public LocalStorage() {
		Document localStorage;
		DocumentBuilder docBuilder;
		try {
			local = new File("local.xml");
			docBuilder = docBuilderFact.newDocumentBuilder();
			localStorage = docBuilder.newDocument();
			
			Element rootElement = localStorage.createElement("Tasks");
			localStorage.appendChild(rootElement);
			
			Transformer trans = transFact.newTransformer();
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(localStorage);
			StreamResult file = new StreamResult(local);
			trans.transform(source, file);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean storeNewTask(Task task) {
		DocumentBuilder docBuilder;
		Document parser;
		try {
			docBuilder = docBuilderFact.newDocumentBuilder();
			parser = docBuilder.parse(local);
			
			Element root = parser.getDocumentElement();
			root.appendChild(getTaskNode(parser, task));
			
			Transformer trans = transFact.newTransformer();
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(parser);
			StreamResult file = new StreamResult(local);
			trans.transform(source, file);
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private static Node getTaskNode(Document doc, Task taskToAdd) {
		Element task = doc.createElement("task");
		task.setAttribute("TaskId", String.valueOf(taskToAdd.getTaskId()));
		task.setIdAttribute("TaskId", true);
		task.setAttribute("event", "no");
		
		task.appendChild(getElement(doc, "description", taskToAdd.getDescription()));
		
		ArrayList<String> contexts = taskToAdd.getContexts();
		if (contexts != null && !contexts.isEmpty()) {
			for (String s: contexts) {
				task.appendChild(getElement(doc, "contexts", s));
			}
		}
		
		ArrayList<String> categories = taskToAdd.getCategories();
		if (categories != null && !categories.isEmpty()) {
			for (String s: categories) {
				task.appendChild(getElement(doc, "categories", s));
			}
		}
		
		task.appendChild(getElement(doc, "date", taskToAdd.getDate().toString()));
		
		if (taskToAdd instanceof TimedTask) {
			task.setAttribute("event", "yes");
			TimedTask timedTask = (TimedTask) taskToAdd;
			task.appendChild(getElement(doc, "end", timedTask.getEndTime().toString()));
		}
		
		return task;
	}

	private static Node getElement(Document doc, String tag, String value) {
		Element node = doc.createElement(tag);
		node.appendChild(doc.createTextNode(value));
		return node;
	}

	public Task removeTask(Task task) {
		
		return null;
	}

	@Override
	public boolean modifyTask(Task T) {
		// TODO Auto-generated method stub
		return false;
	}

	public Task getTask(int taskId) {
		DocumentBuilder docBuilder;
		Document parser;
		Task task = new Task();
		try {
			docBuilder = docBuilderFact.newDocumentBuilder();
			parser = docBuilder.parse(local);
			parser.getDocumentElement().normalize();
			
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			String general = "//task[@TaskId = '%1$s']";
			String expression = String.format(general, String.valueOf(taskId));
			System.out.println(expression);
			Node taskNode = (Node) xpath.compile(expression).evaluate(parser, XPathConstants.NODE);
			System.out.println(taskNode);
			if (taskNode == null) {
				return null;
			}
			else {
				task = getTaskFromFile(taskNode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return task;
	}

	public ArrayList<Task> getAllTasks() {
		ArrayList<Task> tasks = new ArrayList<Task>();
		DocumentBuilder docBuilder;
		Document parser;
		
		try {
			docBuilder = docBuilderFact.newDocumentBuilder();
			parser = docBuilder.parse(local);
			parser.getDocumentElement().normalize();
			NodeList taskNodes = parser.getElementsByTagName("task"); 
			for (int i = 0; i < taskNodes.getLength(); i++) {
				tasks.add(getTaskFromFile(taskNodes.item(i)));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return tasks;
	}

	private Task getTaskFromFile(Node node) throws ParseException {
		Task task = new Task();
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element item = (Element) node;

			task.setTaskId(Integer.parseInt(item.getAttribute("TaskId")));
			task.setDescription(getValue("description", item));
			task.setContexts(getValues("contexts", item));
			task.setCategories(getValues("categories", item));
			task.setDate(new SimpleDateFormat("EEE MMM dd HH:mm:SS z yyyy").
					parse(getValue("date", item)));
			
			if (item.getAttribute("event") == "yes") {
				((TimedTask) task).setEndTime(new SimpleDateFormat("EEE MMM dd HH:mm:SS z yyyy").
						parse(getValue("date", item)));
			}
		}
		return task;
	}

	private static ArrayList<String> getValues(String tag, Element item) {
		ArrayList<String> contents = new ArrayList<String>();
		NodeList nodes = item.getElementsByTagName(tag);
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = (Node) nodes.item(i);
			contents.add(node.getTextContent());
		}
		return contents;
	}

	private static String getValue(String tag, Element item) {
		NodeList nodes = item.getElementsByTagName(tag);
			Node node = (Node) nodes.item(0);
			return node.getTextContent();
	}

	public void close() {
		
	}

}
