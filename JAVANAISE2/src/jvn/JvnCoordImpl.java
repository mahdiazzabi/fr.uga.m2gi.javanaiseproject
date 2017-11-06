/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.logging.Logger;

public class JvnCoordImpl extends UnicastRemoteObject implements JvnRemoteCoord {

	private static JvnCoordImpl jvnCoord = null;

	/**
	 * Jvn objects
	 */
	private HashMap<String, JvnObject> jvnObjects = new HashMap<>();
	
	/**
	 * Link name and id of jvnObjects
	 */
	private HashMap<Integer, String> jvnReferences = new HashMap<>();
	/**
	 * Link id of jvnObjects tooked in write lock with JvnRemoteServer
	 * associated
	 */
	private HashMap<Integer, JvnRemoteServer> jvnWriteServers = new HashMap<>();
	/**
	 * Link id of jvnObjects tooked in read lock with JvnRemoteServer associated
	 */
	private HashMap<Integer, ArrayList<JvnRemoteServer>> jvnReadServers = new HashMap<>();

	private int id;

	/**
	 * Default constructor
	 * 
	 * @throws JvnException
	 **/
	private JvnCoordImpl() throws Exception {
		super();

		FileInputStream fileInputStream = null;
		ObjectInputStream objectInputStream = null;

		try {

			fileInputStream = new FileInputStream("jvnObjects.ser");
			objectInputStream = new ObjectInputStream(fileInputStream);
			jvnObjects = (HashMap<String, JvnObject>) objectInputStream.readObject();

			fileInputStream = new FileInputStream("jvnReferences.ser");
			objectInputStream = new ObjectInputStream(fileInputStream);
			jvnReferences = (HashMap<Integer, String>) objectInputStream.readObject();

			fileInputStream = new FileInputStream("jvnWriteServers.ser");
			objectInputStream = new ObjectInputStream(fileInputStream);
			jvnWriteServers =  (HashMap<Integer, JvnRemoteServer>) objectInputStream.readObject();
			
			fileInputStream = new FileInputStream("jvnReadServers.ser");
			objectInputStream = new ObjectInputStream(fileInputStream);
			jvnReadServers = (HashMap<Integer, ArrayList<JvnRemoteServer>>) objectInputStream.readObject();
		
			System.err.println("taille jvnWriteServers :" +jvnWriteServers.size());

			if (jvnWriteServers.size() == 1) {
				System.err.println(jvnWriteServers.values().toString());
			}

		} catch (FileNotFoundException e) {
			serializeFilesState(jvnObjects, "jvnObjects.ser");
			serializeFilesState(jvnReferences, "jvnReferences.ser");
			serializeFilesState(jvnWriteServers, "jvnWriteServers.ser");
			serializeFilesState(jvnReadServers, "jvnReadServers.ser");
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {

			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (objectInputStream != null) {
				try {
					objectInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
	}

	public static JvnCoordImpl getJvnCoordImpl() {
		if (jvnCoord == null) {
			synchronized (JvnCoordImpl.class) {
				if (jvnCoord == null) {
					try {
						jvnCoord = new JvnCoordImpl();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		return jvnCoord;
	}

	/**
	 * Allocate a NEW JVN object id (usually allocated to a newly created JVN
	 * object)
	 * 
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public int jvnGetObjectId() throws java.rmi.RemoteException, jvn.JvnException {
		return id++;
	}

	/**
	 * Associate a symbolic name with a JVN object
	 * 
	 * @param jon
	 *            : the JVN object name
	 * @param jo
	 *            : the JVN object
	 * @param joi
	 *            : the JVN object identification
	 * @param js
	 *            : the remote reference of the JVNServer
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public synchronized void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
			throws java.rmi.RemoteException, jvn.JvnException {
		System.out.println("");
		System.out.println("==========================");
		System.out.println("JvnCoordImpl:jvnLockRead");
		System.out.println("JvnCoordImpl:jvnRegisterObject register object : " + jon);

		jvnObjects.put(jon, jo);
		jvnReferences.put(jo.jvnGetObjectId(), jon);
		jvnWriteServers.put(jo.jvnGetObjectId(), js);
		jvnReadServers.put(jo.jvnGetObjectId(), new ArrayList<JvnRemoteServer>());

		System.out.println("==========================");
		System.out.println("");

		saveCoordState();
	}

	public static void main(String argv[]) {

		try {
			Registry reg = LocateRegistry.createRegistry(2049);
			JvnCoordImpl jvnCoord = getJvnCoordImpl();
			Naming.rebind("rmi://localhost:2049/refcoord", jvnCoord);
			System.out.println("serveur coord en marche");
		} catch (Exception e) {
			System.out.println("serveur coord erreur :");
			e.printStackTrace();
		}

	}

	/**
	 * Get the reference of a JVN object managed by a given JVN server
	 * 
	 * @param jon
	 *            : the JVN object name
	 * @param js
	 *            : the remote reference of the JVNServer
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public synchronized JvnObject jvnLookupObject(String jon, JvnRemoteServer js)
			throws java.rmi.RemoteException, jvn.JvnException {
		System.out.println("JvnCoordImpl:jvnLookupObject return object : " + jon);

		return jvnObjects.get(jon);
	}

	/**
	 * Get a Read lock on a JVN object managed by a given JVN server
	 * 
	 * @param joi
	 *            : the JVN object identification
	 * @param js
	 *            : the remote reference of the server
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException,
	 *             JvnException
	 **/
	public Serializable jvnLockRead(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
		System.out.println("");
		System.out.println("==========================");
		System.out.println("JvnCoordImpl:jvnLockRead");

		Serializable object = jvnObjects.get(jvnReferences.get(joi)).jvnGetObjectState();

		if (jvnWriteServers.containsKey(joi) && !jvnWriteServers.get(joi).equals(js)) {
			synchronized (this) {
				try {
					System.out.println("Write server has write lock on object " + joi);
					object = jvnWriteServers.get(joi).jvnInvalidateWriterForReader(joi);
					jvnWriteServers.remove(joi);
					jvnObjects.get(jvnReferences.get(joi)).updateObject(object);
				} catch (ConnectException e) {
					System.err.println(e.getMessage());
					jvnWriteServers.remove(joi);

					jvnObjects.get(jvnReferences.get(joi)).updateObject(object);
				}
				
			}
		}

		jvnReadServers.get(joi).add(js);

		System.out.println("==========================");
		System.out.println("");

		saveCoordState();

		return object;
	}

	/**
	 * Get a Write lock on a JVN object managed by a given JVN server
	 * 
	 * @param joi
	 *            : the JVN object identification
	 * @param js
	 *            : the remote reference of the server
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException,
	 *             JvnException
	 **/
	public Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
		System.out.println("");
		System.out.println("==========================");
		System.out.println("JvnCoordImpl:jvnLockWrite joi : " + joi);
		Serializable object = jvnObjects.get(jvnReferences.get(joi)).jvnGetObjectState();

		if (jvnWriteServers.containsKey(joi) && jvnWriteServers.get(joi) != js) {
			synchronized (this) {
				try {
					System.out.println("JvnCoordImpl:jvnLockWrite jvnWriteServers containsKey joi : " + joi);
					object = jvnWriteServers.get(joi).jvnInvalidateWriter(joi);
					jvnObjects.get(jvnReferences.get(joi)).updateObject(object);
				} catch (ConnectException e) {
					System.err.println(e.getMessage());
					jvnWriteServers.remove(joi);
				}
			
			}
		}

		for (int i = 0; i < jvnReadServers.get(joi).size(); i++) {
			if (!jvnReadServers.get(joi).get(i).equals(js)) {
				try {

					System.out.println("JvnCoordImpl:jvnLockWrite try to invalidateReader for joi : " + joi);
					jvnReadServers.get(joi).get(i).jvnInvalidateReader(joi);
					jvnReadServers.get(joi).remove(i);
					System.out.println("JvnCoordImpl:jvnLockWrite invalidateReader for joi : " + joi + " Ok");
					
				} catch (ConnectException e) {
					jvnReadServers.get(joi).remove(i);
				}
				
			}
		}

		jvnWriteServers.put(joi, js);
		System.out.println("==========================");
		System.out.println("");

		saveCoordState();
		return object;
	}

	/**
	 * A JVN server terminates
	 * 
	 * @param js
	 *            : the remote reference of the server
	 * @throws java.rmi.RemoteException,
	 *             JvnException
	 **/
	public synchronized void jvnTerminate(JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
		for (int i = 0; i < jvnWriteServers.size(); i++) {
			if (jvnWriteServers.get(i).equals(js)) {
				jvnWriteServers.remove(i);
			}
		}

		for (int i = 0; i < jvnReadServers.size(); i++) {
			for (JvnRemoteServer jvns : jvnReadServers.get(i)) {
				if (jvns.equals(js)) {
					jvnReadServers.get(i).remove(js);
				}
			}
		}

		saveCoordState();
	}
	
	public void saveCoordState() {
		serializeFilesState(jvnObjects, "jvnObjects.ser");
		serializeFilesState(jvnReferences, "jvnReferences.ser");
		serializeFilesState(jvnWriteServers, "jvnWriteServers.ser");
		serializeFilesState(jvnReadServers, "jvnReadServers.ser");
	}
	
	public void serializeFilesState(Object obj , String fileName) {

		FileOutputStream fout = null;
		ObjectOutputStream oos = null;

		try {

			fout = new FileOutputStream(fileName);
			oos = new ObjectOutputStream(fout);
			oos.writeObject(obj);

			System.out.println("Save file " + fileName + " Done");

		} catch (Exception ex) {

			ex.printStackTrace();

		} finally {

			if (fout != null) {
				try {
					fout.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

	public HashMap<String, JvnObject> getJvnObjects() {
		return jvnObjects;
	}

	public void setJvnObjects(HashMap<String, JvnObject> jvnObjects) {
		this.jvnObjects = jvnObjects;
	}
}
