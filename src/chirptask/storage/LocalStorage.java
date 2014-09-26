package chirptask.storage;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
		
		task.appendChild(getElement(doc, "TaskId", String.valueOf(taskToAdd.getTaskId())));
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
				task.appendChild(getElement(doc, "contexts", s));
			}
		}
		
		task.appendChild(getElement(doc, "date", taskToAdd.getDate().toString()));
		
		if (taskToAdd instanceof TimedTask) {
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

	@Override
	public Task getTask(int taskId) {
		// TODO Auto-generated method stub
		return null;
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

	private Task getTaskFromFile(Node node) {
		Task task = new Task();
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element item = (Element) node;
			task.setTaskId(Integer.parseInt(getValue("TaskId", item)));
			task.setDescription(getValue("description", item));
			task.setContexts(getValues("contexts", item));
			task.setCategories(getValues("categories", item));
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
