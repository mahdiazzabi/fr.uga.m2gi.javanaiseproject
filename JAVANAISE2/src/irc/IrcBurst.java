/***
 * Irc class : simple implementation of a chat using JAVANAISE
 * Contact: 
 *
 * Authors: 
 */

package irc;

import java.awt.*;
import java.awt.event.*;

import jvn.*;
import java.io.*;
import java.util.Random;

public class IrcBurst {
	static JvnObject sentence;

	/**
	 * main method create a JVN object nammed IRC for representing the Chat
	 * application
	 **/
	public static void main(String argv[]) {
		try {
			// initialize JVN
			JvnServerImpl js = JvnServerImpl.jvnGetServer();

			// look up the IRC object in the JVN server
			// if not found, create it, and register it in the JVN server
			JvnObject jo = js.jvnLookupObject("IRC");

			if (jo == null) {
				jo = js.jvnCreateObject((Serializable) new Sentence());
				// after creation, I have a write lock on the object
				jo.jvnUnLock();
				js.jvnRegisterObject("IRC", jo);
			}

			Random r = new Random();
			int valeur = r.nextInt(2);
			while (true) {
				if (valeur == 0) {
					int timer = 1000000;
					jo.jvnLockRead();
					while (timer > 500000) {
						timer--;
					}
				} else {
					jo.jvnLockWrite();
					((Sentence) (jo.jvnGetObjectState())).write("burst");
					jo.jvnUnLock();
				}

			}

		} catch (Exception e) {
			System.out.println("IRC problem : " + e.getMessage());
		}
	}

	/**
	 * IRC Constructor
	 * 
	 * @param jo
	 *            the JVN object representing the Chat
	 **/
	public IrcBurst(JvnObject jo) {
	}
}
