package jvn;

import annotation.Read;
import annotation.Write;
import irc.Sentence;
import irc.SentenceInterface;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class JvnObjectInvocationHandler implements InvocationHandler, Serializable {

    private JvnObject jvnObject;

    private JvnObjectInvocationHandler(String jon) throws JvnException {
        try {
            JvnServerImpl js = JvnServerImpl.jvnGetServer();
            this.jvnObject = js.jvnLookupObject(jon);

            if (this.jvnObject == null) {
                this.jvnObject = js.jvnCreateObject((Serializable) new Sentence());
                this.jvnObject.jvnUnLock();

                js.jvnRegisterObject(jon, this.jvnObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new JvnException();
        }
    }

    public static Object createProxyInstance(String jon) throws Exception {
        return (SentenceInterface) Proxy.newProxyInstance(
                SentenceInterface.class.getClassLoader(),
                new Class[] {SentenceInterface.class},
                new JvnObjectInvocationHandler(jon)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if (method.isAnnotationPresent(Read.class)) {

            this.jvnObject.jvnLockRead();

            String message = ((Sentence) (this.jvnObject.jvnGetObjectState())).read();

            this.jvnObject.jvnUnLock();

            return message;
        }

        if (method.isAnnotationPresent(Write.class)) {
            this.jvnObject.jvnLockWrite();

            ((Sentence) (this.jvnObject.jvnGetObjectState())).write((String) args[0]);

            this.jvnObject.jvnUnLock();
        }


        return this.jvnObject;
    }
}
