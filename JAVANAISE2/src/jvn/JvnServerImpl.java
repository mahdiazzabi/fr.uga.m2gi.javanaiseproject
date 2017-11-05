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
import java.rmi.server.UnicastRemoteObject;
import java.io.*;
import java.util.HashMap;

public class JvnServerImpl extends UnicastRemoteObject implements JvnLocalServer, JvnRemoteServer {

	// A JVN server is managed as a singleton
	private static JvnServerImpl js = null;

	// the remote reference of the JVNServer
	private static JvnRemoteCoord jsCoord = null;

	private HashMap<Integer, JvnObject> jvnObjects;

	/**
	 * Default constructor
	 * 
	 * @throws JvnException
	 **/
	private JvnServerImpl() throws Exception {
		super();

		this.jvnObjects = new HashMap<Integer, JvnObject>();

		while (!connectToCordinator()) {
			System.err.println("Connection Lost trying to reconnect ...");
		}

	}

	private Boolean connectToCordinator() {
		try {
			jsCoord = (JvnRemoteCoord) Naming.lookup("rmi://localhost:2049/refcoord");
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Static method allowing an application to get a reference to a JVN server
	 * instance
	 * 
	 * @throws JvnException
	 **/
	public static JvnServerImpl jvnGetServer() {
		if (js == null) {
			synchronized (JvnServerImpl.class) {
				if (js == null) {
					try {
						js = new JvnServerImpl();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		return js;
	}

	/**
	 * The JVN service is not used anymore
	 * 
	 * @throws JvnException
	 **/
	public void jvnTerminate() throws jvn.JvnException {
		try {
			jsCoord.jvnTerminate(this);
		} catch (ConnectException ex) {
			while (!connectToCordinator()) {
				System.err.println("Connection Lost trying to reconnect ...");
			}
			jvnTerminate();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * creation of a JVN object
	 * 
	 * @param o
	 *            : the JVN object state
	 * @throws JvnException
	 **/
	public JvnObject jvnCreateObject(Serializable o) throws jvn.JvnException {
		try {

			JvnObjectImpl jvnObjectImpl = new JvnObjectImpl(o, jsCoord.jvnGetObjectId());
			return jvnObjectImpl;
		} catch (ConnectException ex) {
			while (!connectToCordinator()) {
				System.err.println("Connection Lost trying to reconnect ...");
			}
			return jvnCreateObject(o);
		} catch (Exception e) {
			throw new JvnException("JvnServerImpl:jvnCreateObject Error : " + e.getMessage());
		}
	}

	/**
	 * Associate a symbolic name with a JVN object
	 * 
	 * @param jon
	 *            : the JVN object name
	 * @param jo
	 *            : the JVN object
	 * @throws JvnException
	 **/
	public void jvnRegisterObject(String jon, JvnObject jo) throws jvn.JvnException {
		try {
			jsCoord.jvnRegisterObject(jon, jo, js);
			jvnObjects.put(jo.jvnGetObjectId(), jo);

			System.out.println("JvnServerImpl:jvnRegisterObject : jo " + jo.jvnGetObjectId() + " resgistered");
		} catch (ConnectException ex) {
			while (!connectToCordinator()) {
				System.err.println("Connection Lost trying to reconnect ...");
			}
			jvnRegisterObject(jon, jo);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Provide the reference of a JVN object beeing given its symbolic name
	 * 
	 * @param jon
	 *            : the JVN object name
	 * @return the JVN object
	 * @throws JvnException
	 **/
	public JvnObject jvnLookupObject(String jon) throws jvn.JvnException {
		try {
			JvnObject object = jsCoord.jvnLookupObject(jon, this);

			if (object != null) {
				jvnObjects.put(object.jvnGetObjectId(), object);
			}

			return object;
		} catch (ConnectException ex) {
			while (!connectToCordinator()) {
				System.err.println("Connection Lost trying to reconnect ...");
			}
			return jvnLookupObject(jon);
		} catch (RemoteException e) {
			throw new JvnException("jvnLookupObject Error");
		}
	}

	/**
	 * Get a Read lock on a JVN object
	 * 
	 * @param joi
	 *            : the JVN object identification
	 * @return the current JVN object state
	 * @throws JvnException
	 **/
	public Serializable jvnLockRead(int joi) throws JvnException {
		try {
			return jsCoord.jvnLockRead(joi, js);
		} catch (ConnectException ex) {
			while (!connectToCordinator()) {
				System.err.println("Connection Lost trying to reconnect ...");
			}
			return jvnLockRead(joi);
		} catch (RemoteException e) {
			throw new JvnException("Error jvnLockRead : " + e.getMessage());
		}
	}

	/**
	 * Get a Write lock on a JVN object
	 * 
	 * @param joi
	 *            : the JVN object identification
	 * @return the current JVN object state
	 * @throws JvnException
	 **/
	public Serializable jvnLockWrite(int joi) throws JvnException {
		try {
			System.out.println("JvnServImpl:jvnLockWrite object : " + joi);
			return jsCoord.jvnLockWrite(joi, js);
		} catch (ConnectException ex) {
			while (!connectToCordinator()) {
				System.err.println("Connection Lost trying to reconnect ...");
			}
			return jvnLockWrite(joi);
		} catch (RemoteException e) {
			throw new JvnException("Error jvnLockWrite : " + e.getMessage());
		}
	}

	/**
	 * Invalidate the Read lock of the JVN object identified by id called by the
	 * JvnCoord
	 * 
	 * @param joi
	 *            : the JVN object id
	 * @return void
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public void jvnInvalidateReader(int joi) throws java.rmi.RemoteException, jvn.JvnException {
		JvnObject object = jvnObjects.get(joi);

		if (object == null) {
			throw new JvnException(
					"JvnServerImpl:jvnInvalidateReader Error : Jvn objects not find in local server joi " + joi);
		}

		try {
			object.jvnInvalidateReader();
		} catch (JvnException e) {
			throw new JvnException("JvnServerImpl:jvnInvalidateReader Error : " + e);
		}
	};

	/**
	 * Invalidate the Write lock of the JVN object identified by id
	 * 
	 * @param joi
	 *            : the JVN object id
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public Serializable jvnInvalidateWriter(int joi) throws java.rmi.RemoteException, jvn.JvnException {
		JvnObject object = jvnObjects.get(joi);

		if (object == null) {
			throw new JvnException(
					"JvnServerImpl:jvnInvalidateWriter Error :Jvn objects not find in local server joi " + joi);
		}

		return object.jvnInvalidateWriter();
	};

	/**
	 * Reduce the Write lock of the JVN object identified by id
	 * 
	 * @param joi
	 *            : the JVN object id
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public Serializable jvnInvalidateWriterForReader(int joi) throws java.rmi.RemoteException, jvn.JvnException {
		JvnObject object = jvnObjects.get(joi);

		if (object == null) {
			throw new JvnException("Jvn objects not find in local server joi " + joi);
		}

		return object.jvnInvalidateWriterForReader();
	};

}
