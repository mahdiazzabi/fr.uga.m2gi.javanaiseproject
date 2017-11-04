package irc;

import annotation.Read;
import annotation.Write;

public interface SentenceInterface {
    @Write
    public void write(String text);
    @Read
    public String read();
    public int getId();
    public Sentence setId(int id);
}
