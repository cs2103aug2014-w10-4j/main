package chirptask.storage;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
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
import org.xml.sax.SAXException;

public class LocalStorage implements Storage {
	private static final String DATE_FORMAT = "EEE MMM dd HH:mm:SS z yyyy";
	private static final String XPATH_EXPRESSION = "//task[@TaskId = '%1$s']";
	File local;
	DocumentBuilder docBuilder;
	Transformer trans;
	Document localStorage;

	public LocalStorage() {
		localStorageInit();
		addRoot();
		writeToFile();
	}

	/**
	 * 
	 */
	private void localStorageInit() {
		try {
			local = new File("local.xml");
			docBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			localStorage = docBuilder.newDocument();
			trans = TransformerFactory.newInstance().newTransformer();
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void addRoot() {
		Element rootElement = localStorage.createElement("Tasks");
		localStorage.appendChild(rootElement);
	}

	/**
	 * @throws TransformerException
	 */
	private void writeToFile() {
		try {
			localStorage.normalize();
			DOMSource source = new DOMSource(localStorage);
			StreamResult file = new StreamResult(local);
			trans.transform(source, file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean storeNewTask(Task task) {
		Element root = getRoot();

		if (root == null) {
			return false;
		}

		root.appendChild(getTaskNode(localStorage, task));
		writeToFile();
		return true;
	}

	/**
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 */
	private Element getRoot() {
		try {
			localStorage = docBuilder.parse(local);
			Element root = localStorage.getDocumentElement();
			return root;
		} catch (Exception e) {
			return null;
		}
	}

	private static Node getTaskNode(Document doc, Task taskToAdd) {
		Element node = doc.createElement("task");
		node.setAttribute("TaskId", String.valueOf(taskToAdd.getTaskId()));
		node.setAttribute("event", "no");

		node.appendChild(getElement(doc, "description",
				taskToAdd.getDescription()));

		ArrayList<String> contexts = taskToAdd.getContexts();
		if (contexts != null && !contexts.isEmpty()) {
			for (String s : contexts) {
				node.appendChild(getElement(doc, "contexts", s));
			}
		}

		ArrayList<String> categories = taskToAdd.getCategories();
		if (categories != null && !categories.isEmpty()) {
			for (String s : categories) {
				node.appendChild(getElement(doc, "categories", s));
			}
		}

		node.appendChild(getElement(doc, "date", taskToAdd.getDate().toString()));

		if (taskToAdd instanceof TimedTask) {
			node.setAttribute("event", "yes");
			TimedTask timedTask = (TimedTask) taskToAdd;
			node.appendChild(getElement(doc, "end", timedTask.getEndTime()
					.toString()));
		}

		return node;
	}

	private static Node getElement(Document doc, String tag, String value) {
		Element node = doc.createElement(tag);
		node.appendChild(doc.createTextNode(value));
		return node;
	}

	public Task removeTask(Task task) {
		Node taskNode = getTaskNode(task.getTaskId());
		Task taskToReturn;
		
		if (taskNode == null) {
			return null;
		} else {
			taskToReturn = getTaskFromFile(taskNode);
			taskNode.getParentNode().removeChild(taskNode);
			writeToFile();
		}
		return taskToReturn;
	}

	public boolean modifyTask(Task T) {
		Task toDelete = getTask(T.getTaskId());
		removeTask(toDelete);
		storeNewTask(T);
		return true;
	}

	/**
	 * This method takes in a number (taskId) and return the corresponding task
	 * 
	 * @param taskId
	 *            (assume taskId to be unique)
	 * @return task
	 */
	public Task getTask(int taskId) {
		Node taskNode = getTaskNode(taskId);
		if (taskNode == null) {
			return null;
		} else {
			return getTaskFromFile(taskNode);
		}

	}

	private Node getTaskNode(int taskId) {
		Node taskNode;
		try {
			localStorage = docBuilder.parse(local);
			localStorage.getDocumentElement().normalize();

			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			String expression = String.format(XPATH_EXPRESSION,
					String.valueOf(taskId));

			taskNode = (Node) xpath.compile(expression).evaluate(localStorage,
					XPathConstants.NODE);

			if (taskNode == null) {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return taskNode;
	}

	public ArrayList<Task> getAllTasks() {
		ArrayList<Task> tasks = new ArrayList<Task>();

		try {

			localStorage = docBuilder.parse(local);
			localStorage.getDocumentElement().normalize();
			NodeList taskNodes = localStorage.getElementsByTagName("task");
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

			task.setTaskId(Integer.parseInt(item.getAttribute("TaskId")));
			task.setDescription(getValues("description", item).get(0));
			task.setContexts(getValues("contexts", item));
			task.setCategories(getValues("categories", item));

			try {
				task.setDate(new SimpleDateFormat(DATE_FORMAT).parse(getValues(
						"date", item).get(0)));

				if (item.getAttribute("event") == "yes") {
					((TimedTask) task).setEndTime(new SimpleDateFormat(
							DATE_FORMAT).parse(getValues("date", item).get(0)));
				}
			} catch (Exception e) {
				return null;
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

	public void close() {

	}

}
