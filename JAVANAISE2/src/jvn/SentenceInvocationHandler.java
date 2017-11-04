package jvn;

import annotation.Read;
import annotation.Terminate;
import annotation.Write;
import irc.Sentence;
import irc.SentenceInterface;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class SentenceInvocationHandler implements InvocationHandler, Serializable {

    private JvnObject jvnObject;
    private JvnServerImpl jvnServer;

    private SentenceInvocationHandler(String jon) throws JvnException {
        try {
            this.jvnServer = JvnServerImpl.jvnGetServer();
            this.jvnObject = this.jvnServer.jvnLookupObject(jon);

            if (this.jvnObject == null) {
                this.jvnObject = this.jvnServer.jvnCreateObject((Serializable) new Sentence());
                this.jvnObject.jvnUnLock();

                this.jvnServer.jvnRegisterObject(jon, this.jvnObject);
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
                new SentenceInvocationHandler(jon)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if (method.isAnnotationPresent(Read.class)) {

            this.jvnObject.jvnLockRead();
        }

        if (method.isAnnotationPresent(Write.class)) {
            this.jvnObject.jvnLockWrite();
        }

        if (method.isAnnotationPresent(Terminate.class)) {
            this.jvnObject.jvnUnLock();
            this.jvnServer.jvnTerminate();
        }

        Object invokeResult = method.invoke(this.jvnObject.jvnGetObjectState(), args);

        this.jvnObject.jvnUnLock();

        return invokeResult;
    }
}
