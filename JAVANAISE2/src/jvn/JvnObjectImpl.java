package jvn;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject {
	
	private Serializable object = null;

	public JvnObjectImpl(Serializable object) {
		this.object = object;
	}
	
	public void jvnLockRead() throws JvnException {
		// TODO Auto-generated method stub
		
	}

	public void jvnLockWrite() throws JvnException {
		// TODO Auto-generated method stub
		
	}

	public void jvnUnLock() throws JvnException {
		// TODO Auto-generated method stub
		
	}

	public int jvnGetObjectId() throws JvnException {
		// TODO Auto-generated method stub
		return 0;
	}

	public Serializable jvnGetObjectState() throws JvnException {
		// TODO Auto-generated method stub
		return this.object;
	}

	public void jvnInvalidateReader() throws JvnException {
		// TODO Auto-generated method stub
		
	}

	public Serializable jvnInvalidateWriter() throws JvnException {
		// TODO Auto-generated method stub
		return null;
	}

	public Serializable jvnInvalidateWriterForReader() throws JvnException {
		// TODO Auto-generated method stub
		return null;
	}

}
