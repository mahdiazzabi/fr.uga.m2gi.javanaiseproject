/***
 * Sentence class : used for representing the text exchanged between users
 * during a chat application
 * Contact: 
 *
 * Authors: 
 */

package irc;

public class Sentence implements SentenceInterface, java.io.Serializable {
	private int id;
	String data;

	public Sentence() {
		data = new String("");
	}

	public void write(String text) {
		data = text;
	}

	public String read() {
		return data;
	}

	public void terminate() {
		System.out.println("Terminate");
	}

	public int getId() {
		return id;
	}

	public Sentence setId(int id) {
		this.id = id;
		
		return this;
	}
}