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

	SentenceInterface sentence;

	public static void main(String argv[]) {

		try {
			SentenceInterface sentence = (SentenceInterface) SentenceInvocationHandler.createProxyInstance("IRC");

			Random r = new Random();
			while (true) {
				int valeur = r.nextInt(2);
				if (valeur == 0) {
					sentence.read();
				} else {
					sentence.write("ForBurst");
				}

			}

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

}
