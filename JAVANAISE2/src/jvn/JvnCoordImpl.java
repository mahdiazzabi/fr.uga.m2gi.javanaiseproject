/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.logging.Logger;

public class JvnCoordImpl extends UnicastRemoteObject implements JvnRemoteCoord  {

	private static JvnCoordImpl jvnCoord = null;

	/**
	 *  Jvn objects
	 */
	private HashMap<String, JvnObject> jvnObjects;
	/**
	 * Jvn remote servers
	 */
	private HashMap<String, JvnRemoteServer> jvnRemoteServers;
	/**
	 * Link name and id of jvnObjects
	 */
	private HashMap<Integer, String> jvnReferences;
	/**
	 * Link id of jvnObjects tooked in write lock with JvnRemoteServer associated
	 */
	private HashMap<Integer, JvnRemoteServer> writeServer;
	/**
	 * Link id of jvnObjects tooked in read lock with JvnRemoteServer associated
	 */
	private HashMap<Integer, ArrayList<JvnRemoteServer>> readServer;

	private int id;

	/**
	 * Default constructor
	 * 
	 * @throws JvnException
	 **/
	private JvnCoordImpl() throws Exception {
		super();

		this.jvnObjects = new HashMap<String, JvnObject>();
		this.jvnReferences = new HashMap<Integer, String>();
		this.jvnRemoteServers = new HashMap<String, JvnRemoteServer>();
		this.writeServer = new HashMap<Integer, JvnRemoteServer>();
		this.readServer = new HashMap<Integer, ArrayList<JvnRemoteServer>>();
		this.id = 0;
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
		
		jvnObjects.put(jon, jo);
		jvnReferences.put(jo.jvnGetObjectId(), jon);
		jvnRemoteServers.put(jon, js);
		writeServer.put(jo.jvnGetObjectId(), js);
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
	public synchronized JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws java.rmi.RemoteException, jvn.JvnException {
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
	public synchronized Serializable jvnLockRead(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
		System.out.println("JvnCoordImpl:jvnLockRead");
		Serializable object;

		if (writeServer.containsKey(joi)) {
			System.out.println("Write server has write lock on object " + joi);
			object = writeServer.get(joi).jvnInvalidateWriterForReader(joi);
			writeServer.remove(joi);
		} else {
			readServer.get(joi).add(js);
			object = jvnObjects.get(jvnReferences.get(joi));
		}

		readServer.get(joi).add(js);

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
	public synchronized Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
		System.out.println("JvnCoordImpl:jvnLockWrite");
		Serializable object;

		if (writeServer.containsKey(joi)) {
			object = writeServer.get(joi).jvnInvalidateWriter(joi);
			writeServer.put(joi, js);
		} else {

			for (JvnRemoteServer jvnRemoteServer : readServer.get(joi)) {
				jvnRemoteServer.jvnInvalidateReader(joi);
			}

			object = jvnObjects.get(jvnReferences.get(joi));
			writeServer.put(joi, js);
		}

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
		// to be completed
	}

	public HashMap<String, JvnObject> getJvnObjects() {
		return jvnObjects;
	}

	public void setJvnObjects(HashMap<String, JvnObject> jvnObjects) {
		this.jvnObjects = jvnObjects;
	}
	
}
