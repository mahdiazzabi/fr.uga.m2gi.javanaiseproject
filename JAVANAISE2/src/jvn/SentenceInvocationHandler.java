package jvn;

import annotation.Read;
import annotation.Write;
import irc.Sentence;
import irc.SentenceInterface;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class SentenceInvocationHandler implements InvocationHandler, Serializable {

    private JvnObject jvnObject;

    private SentenceInvocationHandler(String jon) throws JvnException {
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

        Object invokeResult = method.invoke(this.jvnObject.jvnGetObjectState(), args);

        this.jvnObject.jvnUnLock();

        return invokeResult;
    }
}
