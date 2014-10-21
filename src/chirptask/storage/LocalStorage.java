package chirptask.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class handles the tasks list in XML format. The XML file it manages
 * contains tasks id, description, contexts, categories and deadline/start time
 * - end time
 * 
 */
//@author A0113022
public class LocalStorage implements IStorage {
	
	private static final String DATE_FORMAT = "EEE MMM dd HH:mm:SS z yyyy";
	private static final String XPATH_EXPRESSION_ID = "//task[@TaskId = '%1$s']";
	private static final String XPATH_EXPRESSION_SPACE = "//text()[normalize-space(.) = '']";

	private File local;
	private DocumentBuilder docBuilder;
	private Transformer trans;
	private Document localStorage;
	private static int idGenerator;

	public LocalStorage() {
		localStorageInit();
	}

	/**
	 * Initialize all components of LocalStorage
	 */
	private void localStorageInit() {
		setUpXmlWriter();
		if (local.exists()) {
			try {
				setIdGenerator(getLatestId());
			} catch (SAXException e) { //if file cannot be parsed
				clearContent(local);
				addRoot();
				writeToFile();
				setIdGenerator(0);
//				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			addRoot();
			writeToFile();
			setIdGenerator(0);
		} 
	}

	private void clearContent(File file) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(file);
			writer.print("");
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	/**
	 * @throws ParserConfigurationException
	 * @throws TransformerConfigurationException
	 * @throws TransformerFactoryConfigurationError
	 */
	private void setUpXmlWriter() {
		try {
			local = new File("local.xml");
			docBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			localStorage = docBuilder.newDocument();
			trans = TransformerFactory.newInstance().newTransformer();
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
		} catch (ParserConfigurationException
				| TransformerConfigurationException
				| TransformerFactoryConfigurationError e) {
			
		}
	}

	/**
	 * returns latest ID stored as root attribute
	 * 
	 * @throws SAXException
	 * @throws IOException
	 */
	private int getLatestId() throws SAXException, IOException {
		int id;
		localStorage = docBuilder.parse(local);
		Node root = getRoot();
		id = Integer.parseInt(root.getAttributes().getNamedItem("LatestId")
				.getNodeValue());
		return id;
	}

	private void setIdGenerator(int id) {
		idGenerator = id;
	}

	/**
	 * Add the first element in XML file (<Tasks>)
	 */
	private void addRoot() {
		Element rootElement = localStorage.createElement("Tasks");
		rootElement.setAttribute("LatestId", "1");
		localStorage.appendChild(rootElement);
	}

	/**
	 * Write from DOMSource to text file
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

	/**
	 * This methods writes new task to XML file
	 */
	public boolean storeNewTask(Task task) {
		Element root = getRoot();

		if (root == null) {
			return false;
		}
		root.setAttribute("LatestId", String.valueOf(idGenerator));
		root.appendChild(generateTaskNode(localStorage, task));
		writeToFile();
		return true;
	}

	/**
	 * @return root element
	 * @throws SAXException
	 * @throws IOException
	 */
	private Element getRoot() {
		try {
			Element root = localStorage.getDocumentElement();
			return root;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * This method add a task to the XML file, one attribute at a time
	 * 
	 * @param doc
	 * @param taskToAdd
	 * @return the corresponding node
	 */
	private static Node generateTaskNode(Document doc, Task taskToAdd) {
	    if (doc == null || taskToAdd == null) {
	        return null;
	    }
	    
		Element node = doc.createElement("task");
		node.setAttribute("TaskId", String.valueOf(taskToAdd.getTaskId()));
		node.setAttribute("done", String.valueOf(taskToAdd.isDone()));

		node.appendChild(getElement(doc, "description",
				taskToAdd.getDescription()));

		node.appendChild(getElement(doc, "googleId", taskToAdd.getGoogleId()));
		
		node.appendChild(getElement(doc, "googleETag", taskToAdd.getETag()));
		
		node.appendChild(getElement(doc, "isDeleted", taskToAdd.isDeleted()+""));
		
		node.appendChild(getElement(doc, "isModified", taskToAdd.isModified()+""));

		List<String> contexts = taskToAdd.getContexts();
		if (contexts != null && !contexts.isEmpty()) {
			for (String s : contexts) {
				node.appendChild(getElement(doc, "contexts", s));
			}
		}

		List<String> categories = taskToAdd.getCategories();
		if (categories != null && !categories.isEmpty()) {
			for (String s : categories) {
				node.appendChild(getElement(doc, "categories", s));
			}
		}

		if (taskToAdd instanceof TimedTask) {
			TimedTask timedTask = (TimedTask) taskToAdd;
			node.appendChild(getElement(doc, "type", "Timed Task"));
			node.appendChild(getElement(doc, "start", timedTask.getStartTime()
					.getTime().toString()));
			node.appendChild(getElement(doc, "end", timedTask.getEndTime()
					.getTime().toString()));
		} else if (taskToAdd instanceof DeadlineTask) {
			node.appendChild(getElement(doc, "deadline", taskToAdd.getDate()
					.getTime().toString()));
			node.appendChild(getElement(doc, "type", "Deadline Task"));
		} else if (taskToAdd.getType().equalsIgnoreCase("floating")) {
			node.appendChild(getElement(doc, "type", "Floating Task"));
		}

		return node;
	}

	/**
	 * This method writes an attribute of task between its enclosing tags
	 * 
	 * @param doc
	 * @param tag
	 * @param value
	 * @return the corresponding node
	 */
	private static Node getElement(Document doc, String tag, String value) {
		Element node = doc.createElement(tag);
		node.appendChild(doc.createTextNode(value));
		return node;
	}

	/**
	 * This method deletes a task from XML file
	 */
	public Task removeTask(Task task) {
		if (task == null) {
		    return null;
		}
	    Node taskNode = getTaskNode(task.getTaskId());
		Task taskToReturn;

		if (taskNode == null) {
			return null;
		} else {
			taskToReturn = retrieveTaskFromFile(taskNode);
			taskNode.getParentNode().removeChild(taskNode);
			removeWhiteSpace();
			writeToFile();
		}
		return taskToReturn;
	}

	private void removeWhiteSpace() {
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		try {
			NodeList spaces = (NodeList) xpath.compile(XPATH_EXPRESSION_SPACE)
					.evaluate(localStorage, XPathConstants.NODESET);
			for (int i = 0; i < spaces.getLength(); i++) {
				Node space = spaces.item(i);
				space.getParentNode().removeChild(space);
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

	}

	/**
	 * This methods deletes a task in XML file and write its updated version
	 * back.
	 */
	public boolean modifyTask(Task T) {
	    boolean isModified = false;
		Task toDelete = getTask(T.getTaskId());
		Task removedTask = removeTask(toDelete);
		isModified = storeNewTask(T);
		if (removedTask == null) {
		    isModified = false;
		}
		return isModified;
	}

	/**
	 * This method takes in a number (taskId) and return the corresponding task
	 * 
	 * @param taskId
	 *            (assume taskId to be unique)
	 * @return task
	 */
	public Task getTask(int taskId) {
	    if (taskId < 0) {
	        return null;
	    }
		Node taskNode = getTaskNode(taskId);
		if (taskNode == null) {
			return null;
		} else {
			return retrieveTaskFromFile(taskNode);
		}

	}

	/**
	 * This method takes in taskId and returns the corresponding node
	 * 
	 * @param taskId
	 * @return node
	 */
	private Node getTaskNode(int taskId) {
	    if (taskId < 0) {
	        return null;
	    }
		Node taskNode = null;
		try {
			localStorage = docBuilder.parse(local);
			localStorage.getDocumentElement().normalize();

			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			String expression = String.format(XPATH_EXPRESSION_ID,
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

	/**
	 * This method returns a list of tasks stored in XML file
	 */
	public List<Task> getAllTasks() {
		List<Task> tasks = new ArrayList<Task>();
		try {
			localStorage = docBuilder.parse(local);
			localStorage.getDocumentElement().normalize();

			NodeList taskNodes = localStorage.getElementsByTagName("task");
			for (int i = 0; i < taskNodes.getLength(); i++) {
				tasks.add(retrieveTaskFromFile(taskNodes.item(i)));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return tasks;
	}

	/**
	 * This method takes in a node and return the corresponding task
	 * 
	 * @param node
	 * @return task
	 */
	private Task retrieveTaskFromFile(Node node) {
		Task task = null;
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element item = (Element) node;
			try {
				int taskId = Integer.parseInt(item.getAttribute("TaskId"));
				String typeTask = getValues("type", item).get(0);
				String description = getValues("description", item).get(0);
				String googleId = getValues("googleId", item).get(0);
				String googleETag = getValues("googleETag", item).get(0);
				String taskStatus = item.getAttribute("done");
                String deleted = getValues("isDeleted", item).get(0);
                String modified = getValues("isModified", item).get(0);
                
                boolean isDeleted = false;
                boolean isModified = false;
                
                if (deleted != null) {
                    isDeleted = Boolean.parseBoolean(deleted);
                }
                
                if (modified != null) {
                    isModified = Boolean.parseBoolean(modified);
                }
                
				SimpleDateFormat dateFormatter = new SimpleDateFormat(
						DATE_FORMAT);

				if (typeTask.equalsIgnoreCase("Deadline Task")) {
					Calendar dueDate = Calendar.getInstance();
					dueDate.setTime(dateFormatter.parse(getValues("deadline",
							item).get(0)));
					task = new DeadlineTask(taskId, description, dueDate);
				} else if (typeTask.equalsIgnoreCase("Timed Task")) {
					Calendar startTime = Calendar.getInstance();
					startTime.setTime(dateFormatter.parse(getValues("start",
							item).get(0)));
					Calendar endTime = Calendar.getInstance();
					endTime.setTime(dateFormatter.parse(getValues("end", item)
							.get(0)));
					task = new TimedTask(taskId, description, startTime,
							endTime);
				} else {
					task = new Task(taskId, description);
				}

				task.setContexts(getValues("contexts", item));
				task.setCategories(getValues("categories", item));
				task.setGoogleId(googleId);
				task.setETag(googleETag);
				task.setDeleted(isDeleted);
				task.setModified(isModified);

				// A0111930W
				if (taskStatus.equalsIgnoreCase("true")) {
					task.setDone(true);
				} else {
					task.setDone(false);
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return task;
	}

	/**
	 * This method helps reconstruct Task object by returning an ArrayList of
	 * values in tags
	 * 
	 * @param tag
	 * @param item
	 * @return ArrayList<String>
	 */
	//@author A0113022
	private static List<String> getValues(String tag, Element item) {
		List<String> contents = new ArrayList<String>();
		NodeList nodes = item.getElementsByTagName(tag);
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = (Node) nodes.item(i);
			contents.add(node.getTextContent());
		}
		return contents;
	}

	public void addGoogleId(int taskId, String googleId) {
		Node node = getTaskNode(taskId);
		node.appendChild(getElement(localStorage, "googleId", googleId));
		writeToFile();
	}

	public void close() {

	}

	public static int generateId() {
		idGenerator++;
		return idGenerator;
	}
}
