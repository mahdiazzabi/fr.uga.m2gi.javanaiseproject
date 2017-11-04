package irc;


import annotation.JvnMessage;
import annotation.JvnTerminate;

public interface SentenceInterface {
    @JvnMessage(methodeType = JvnMessage.MethodType.WRITE)
    public void write(String text);
    @JvnMessage(methodeType = JvnMessage.MethodType.READ)
    public String read();
    @JvnTerminate
    public void terminate();

    public int getId();
    public Sentence setId(int id);
}
