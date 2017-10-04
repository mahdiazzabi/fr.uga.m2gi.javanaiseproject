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
import java.util.HashMap;
import java.io.Serializable;
import java.net.MalformedURLException;

public class JvnCoordImpl extends UnicastRemoteObject implements JvnRemoteCoord  {

	private static JvnCoordImpl jvnCoord = null;
	private HashMap<String, JvnObject> jvnObjects;

	/**
	 * Default constructor
	 * 
	 * @throws JvnException
	 **/
	private JvnCoordImpl() throws Exception {
		super();
		jvnObjects = new HashMap<String, JvnObject>();
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
		// to be completed
		return 0;
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
	public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
			throws java.rmi.RemoteException, jvn.JvnException {
		jvnObjects.put(jon, jo);

	}

	public static void main(String argv[]) {

		try {
			Registry reg = LocateRegistry.createRegistry(2049);
			JvnCoordImpl jvnCoord = getJvnCoordImpl();
			Naming.rebind("rmi://localhost:2049/refcoord", jvnCoord);
			System.out.println("serveur coord en marche");
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
	public JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws java.rmi.RemoteException, jvn.JvnException {
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
		// to be completed
		return null;
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
		// to be completed
		return null;
	}

	/**
	 * A JVN server terminates
	 * 
	 * @param js
	 *            : the remote reference of the server
	 * @throws java.rmi.RemoteException,
	 *             JvnException
	 **/
	public void jvnTerminate(JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
		// to be completed
	}

	public HashMap<String, JvnObject> getJvnObjects() {
		return jvnObjects;
	}

	public void setJvnObjects(HashMap<String, JvnObject> jvnObjects) {
		this.jvnObjects = jvnObjects;
	}
	
}
