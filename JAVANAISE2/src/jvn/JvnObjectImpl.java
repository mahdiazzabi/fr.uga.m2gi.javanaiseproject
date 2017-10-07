package jvn;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject {
	
	private Serializable object = null;
	transient private LockState lock = LockState.NL;
	
	private enum LockState {
	    NL,    // no local lock
        RLC,   // read lock cached
        WLC,   // write lock cached
        RLT,   // read lock taken
        WLT,   // write lock taken
        RLTWLC; // read lock taken & write lock cached
	}

	public JvnObjectImpl(Serializable object) {
		this.object = object;
	}
	
	public void jvnLockRead() throws JvnException {
		// TODO Auto-generated method stub
		
	}

	public void jvnLockWrite() throws JvnException {
		this.setLock(LockState.WLT);
		
		System.out.println(this.lock);
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
	
	public LockState getLock() {
		return this.lock;
	}
	
	public JvnObjectImpl setLock(LockState lock) {
		this.lock = lock;
		
		return this;
	}
}
