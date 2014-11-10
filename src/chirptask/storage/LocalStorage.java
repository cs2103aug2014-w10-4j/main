package chirptask.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
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

import chirptask.common.Constants;

/**
 * This class handles the tasks list in XML format. The XML file it manages
 * contains tasks id, description, contexts, categories and deadline/start time
 * - end time
 * 
 */
//@author A0113022H
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
				restartLocalStorage();
			} catch (IOException e) {
				StorageHandler.logError(String.format(Constants.ERROR_LOCAL, 
						"write to file failed"));
			}
		} else {
			restartLocalStorage();
		} 
	}

	/**
	 * Clear the file and write current session's
	 * task list to it.
	 */
	private void restartLocalStorage() {
		addRoot();
		checkSessionStorage();
		writeToFile();
		setIdGenerator(0);
	}

	/**
	 * Get the current session's task list
	 */
	private void checkSessionStorage() {
		if (StorageHandler.isSessionStorageInit() == false) {
			return;
		}
		List<Task> sessionList = StorageHandler.getAllTasks();
		if (sessionList == null || sessionList.size() == 0) {
			return;
		}
		
		for (int i = 0; i < sessionList.size(); i++) {
			this.storeNewTask(sessionList.get(i));
		}
	}

	/**
	 * Empty the file
	 * @param file
	 */
	private void clearContent(File file) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(file);
			writer.print("");
			writer.close();
		} catch (FileNotFoundException e) {
			StorageHandler.logError(String.format(Constants.ERROR_LOCAL, 
					"file does not exist"));
			return;
		}
		
		
	}

	/**
	 * This method sets up XML writer
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
		} catch (ParserConfigurationException p) {
			StorageHandler.logError(String.format(Constants.ERROR_LOCAL, 
					"error in setting up parser"));
		} catch (TransformerConfigurationException t) {
			StorageHandler.logError(String.format(Constants.ERROR_LOCAL, 
					"error in setting up transformer"));
		} catch (TransformerFactoryConfigurationError e) {
			StorageHandler.logError(String.format(Constants.ERROR_LOCAL, 
					"error in setting up transformer"));
		} catch (NullPointerException n) {
			return;
		}
	}
	
	/** 
	 * Test Stub Helper for JUnitStorage
	 * This method will overwrite the local storage document to a test storage
	 * When it is called, it will ensure the storage starts from fresh state
	 */
    public void setUpJUnitTestXmlWriter() {
        try {
            local = new File("localJUnitTest.xml");
            local.delete();
            local.createNewFile();
            docBuilder = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            localStorage = docBuilder.newDocument();
            trans = TransformerFactory.newInstance().newTransformer();
            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            restartLocalStorageNoSession();
        } catch (IOException ioException) {
        } catch (ParserConfigurationException e) {
        } catch (TransformerConfigurationException e) {
        } catch (TransformerFactoryConfigurationError e) {
        } catch (NullPointerException e) {
        }
    }
    
    /**
     * This stub is created for setUpJUnitTestXmlWriter() method
     * Normal restart local storage will try to copy all tasks in
     * SessionStorage into LocalStorage.
     * This stub is created to perform the same stuff without copying
     * the SessionStorage into LocalStorage to ensure that we get an 
     * empty JUnit Test XML Storage.
     */
    private void restartLocalStorageNoSession() {
        addRoot();
        writeToFile();
        setIdGenerator(0);
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
		} catch (NullPointerException e) {
			return;
		} catch (TransformerException t) {
			return;
		}
	}

	/**
	 * This methods writes new task to XML file
	 */
	public synchronized boolean storeNewTask(Task task) {
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
	 */
	private Element getRoot() {
		try {
			Element root = localStorage.getDocumentElement();
			return root;
		} catch (NullPointerException e) {
			return null;
		}
	}

	/**
	 * This method add a task to the XML file, one attribute at a time
	 * 
	 * @param doc represents the whole document
	 * @param taskToAdd the Task to be added
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

		List<String> contexts = taskToAdd.getHashtags();
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
			if (taskToAdd.isDone()) {
	            node.appendChild(getElement(doc, "doneDate", 
	                    taskToAdd.getDate().getTime().toString()));
			}
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
	public synchronized Task removeTask(Task task) {
		if (task == null) {
		    return null;
		}
	    Node taskNode = getTaskNode(task.getTaskId());
		Task taskToReturn = null;

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

	/**
	 * This method deletes the whitespace left after deleting tasks
	 */
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
			return;
		}

	}

	/**
	 * This methods deletes a task in XML file and write its updated version
	 * back.
	 */
	public synchronized boolean modifyTask(Task T) {
	    boolean isModified = false;
		Task toDelete = getTask(T.getTaskId());
		Task removedTask = removeTask(toDelete);
		
		if (removedTask == null) {
		    isModified = false;
		} else {
	        isModified = storeNewTask(T);
		}
		return isModified;
	}

	/**
	 * This method takes in a number (taskId) and return the corresponding task
	 * @param taskId the id of Task
	 * @return a Task object or null if the Task cannot be found 
	 */
	public Task getTask(int taskId) {
	    if (taskId < 0) {
	        return null;
	    }
		Node taskNode = getTaskNode(taskId);
		if (taskNode == null) {
			return null;
		} else {
		    Task retrievedTask = retrieveTaskFromFile(taskNode);
			return retrievedTask;
		}

	}

	/**
	 * This method takes in taskId and returns the corresponding node
	 * @param taskId the Id of the Task
	 * @return node that represents the Task
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
		} catch (IOException e) {
			StorageHandler.logError(String.format(Constants.ERROR_LOCAL, 
					"get task node failed"));
			return null;
		} catch (XPathExpressionException x) {
			return null;
		} catch (SAXException s) {
			restartLocalStorage();
			return null;
		}
		
		return taskNode;
	}

	/**
	 * This method returns a list of tasks stored in XML file
	 */
	public synchronized List<Task> getAllTasks() {
		List<Task> tasks = new ArrayList<Task>();
		try {
			localStorage = docBuilder.parse(local);
			localStorage.getDocumentElement().normalize();

			NodeList taskNodes = localStorage.getElementsByTagName("task");
			for (int i = 0; i < taskNodes.getLength(); i++) {
				tasks.add(retrieveTaskFromFile(taskNodes.item(i)));
			}
		} catch (IOException e) {
			StorageHandler.logError(String.format(Constants.ERROR_LOCAL, 
					"get task node failed"));
			return null;
		} catch (SAXException s) {
			restartLocalStorage();
			return null;
		}
		return tasks;
	}

	/**
	 * This method takes in a node and return the corresponding task
	 * @param node represents Task object
	 * @return task Task object
	 */
	private Task retrieveTaskFromFile(Node node) {
		Task task = null;
		int taskId = -1;
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element item = (Element) node;
			try {
				taskId = Integer.parseInt(item.getAttribute("TaskId"));
			} catch (NumberFormatException e) {
				return null;
			}	
			
			if (taskId < 0) {
				return null;
			}
				
			String typeTask = getValues("type", item).get(0);
			String description = getValues("description", item).get(0);
			String googleId = getValues("googleId", item).get(0);
			String googleETag = getValues("googleETag", item).get(0);
			String taskStatus = item.getAttribute("done");
            String deleted = getValues("isDeleted", item).get(0);
            String modified = getValues("isModified", item).get(0);
                
            boolean isDone = false;
            boolean isDeleted = false;
            boolean isModified = false;
                
            if (taskStatus != null) {
                if (taskStatus.equals("true")) {
                   isDone = true;
                } else {
                   isDone = false;
                }
            }
                
            if (deleted != null) {
                if (deleted.equals("true")) {
                    isDeleted = true;
                } else {
                    isDeleted = false;
                }
            }
                
            if (modified != null) {
                 if (modified.equals("true")) {
                    isModified = true;
                 } else {
                    isModified = false;
                }
            }
                
			SimpleDateFormat dateFormatter = new SimpleDateFormat(
						DATE_FORMAT);

			if (typeTask.equalsIgnoreCase("Deadline Task")) {
				Calendar dueDate = Calendar.getInstance();
				Date deadline;
				try {
					deadline = dateFormatter.parse(getValues("deadline",item).get(0));
				} catch (ParseException e) {
					return null;
				}
				dueDate.setTime(deadline);
				task = new DeadlineTask(taskId, description, dueDate);
			} else if (typeTask.equalsIgnoreCase("Timed Task")) {
				try {
					Calendar startTime = Calendar.getInstance();
					startTime.setTime(dateFormatter.parse(getValues("start",
								item).get(0)));
					Calendar endTime = Calendar.getInstance();
					endTime.setTime(dateFormatter.parse(getValues("end", item)
								.get(0)));
					task = new TimedTask(taskId, description, startTime,
								endTime);
				} catch (ParseException e) {
					return null;
				}
			} else {
				task = new Task(taskId, description);
				if (isDone) {
					Calendar doneDate = Calendar.getInstance();
					String storedDateString = getValues("doneDate",
					            item).get(0);
					if (storedDateString != null) {
					   try { 
					        Date storedDate = dateFormatter.parse(
					                storedDateString);
					        doneDate.setTime(storedDate);
					   } catch (ParseException parseException) {
					            //do nothing, just use today's date above.
					            //support for older versions.
					    }
					}
					task.setDate(doneDate);
				}
			}

			task.setHashtags(getValues("contexts", item));
			task.setCategories(getValues("categories", item));
			task.setGoogleId(googleId);
			task.setETag(googleETag);
			task.setDeleted(isDeleted);
			task.setModified(isModified);
			task.setDone(isDone);

		}
		return task;
	}

	/**
	 * This method helps reconstruct Task object by returning an ArrayList of
	 * values in tags
	 * @param tag String
	 * @param item String
	 * @return ArrayList<String> of values
	 */
	//@author A0113022H
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
